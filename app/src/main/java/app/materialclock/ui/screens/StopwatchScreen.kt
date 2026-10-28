package app.materialclock.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import app.materialclock.core.Lap
import app.materialclock.core.Stopwatch
import app.materialclock.core.stopwatchParts
import java.time.Duration

/**
 * Generate 44 different vibrant lap colors.
 */
@Composable
private fun getLapColors(index: Int): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()

    val hue = ((index - 1) * (360f / 44f)) % 360f

    val saturation = if (isDark) 0.75f else 0.7f
    val lightnessBadge = if (isDark) 0.45f else 0.85f
    val lightnessText = if (isDark) 0.95f else 0.2f

    val badgeBg = Color.hsl(
        hue,
        saturation,
        lightnessBadge
    )

    val badgeFg = Color.hsl(
        hue,
        saturation,
        lightnessText
    )

    return Pair(badgeBg, badgeFg)
}

@Composable
fun StopwatchScreen(
    stopwatch: Stopwatch,
    nowElapsedMillis: Long,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val elapsed = stopwatch.elapsed(nowElapsedMillis)

    val (a, b, c) = elapsed.stopwatchParts()

    val fastest = stopwatch.fastest
    val slowest = stopwatch.slowest

    val listState = rememberLazyListState()

    LaunchedEffect(stopwatch.laps.size) {
        if (stopwatch.laps.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Apply only Scaffold top padding.
    // Bottom padding is handled by LazyColumn.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding()
            )
    ) {

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        StackedDigits(
            parts = listOf(a, b, c),
            capHeight = READOUT_CAP,
            color = MaterialTheme.colorScheme.primary,
            leading = Icons.Rounded.Timer,
            label = "Stopwatch, $b seconds $c hundredths" +
                if (stopwatch.running) {
                    ", running"
                } else {
                    ", stopped"
                },
            modifier = Modifier.padding(
                horizontal = 20.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(STACK_GAP)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            StopwatchControls(
                idle = !stopwatch.running && elapsed.isZero,
                running = stopwatch.running,
                onToggle = onToggle,
                onLap = onLap,
                onReset = onReset,
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // Lap list extends behind bottom navigation.
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(
                    LAP_GAP
                ),
            ) {

                val isIdle = !stopwatch.running && elapsed.isZero

                if (!isIdle) {

                    item(key = "current_lap") {

                        val currentSplit = elapsed.minus(
                            stopwatch.laps.firstOrNull()?.total
                                ?: Duration.ZERO
                        )

                        val currentLap = Lap(
                            index = (
                                stopwatch.laps.firstOrNull()?.index ?: 0
                            ) + 1,
                            split = currentSplit,
                            total = elapsed
                        )

                        LapRow(
                            lap = currentLap,
                            isFastest = false,
                            isSlowest = false,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                items(
                    items = stopwatch.laps,
                    key = { it.index }
                ) { lap ->

                    LapRow(
                        lap = lap,
                        isFastest = fastest == lap.index,
                        isSlowest = slowest == lap.index,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

/**
 * Animated stopwatch controls.
 *
 * Idle:
 * Single Start button.
 *
 * Active:
 * Lap, Reset and Start/Stop buttons.
 */
@Composable
private fun StopwatchControls(
    idle: Boolean,
    running: Boolean,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    val expanded = !idle

    // Spring animation for expanding controls.
    val expand by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expand",
    )

    // Label fade animation.
    val label by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "label",
    )

    // Split animation for three buttons.
    val split by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "split",
    )

    val e = expand.coerceIn(0f, 1f)

    val startStop: @Composable (Boolean) -> Unit = { compact ->

        WidePill(
            text = if (running) "Stop" else "Start",
            icon = if (running) {
                Icons.Rounded.Pause
            } else {
                Icons.Rounded.PlayArrow
            },
            onClick = onToggle,
            container = if (running) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            content = if (running) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            height = ROW_HEIGHT,
            compact = compact,
        )
    }

    SubcomposeLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .padding(horizontal = 28.dp), // Butons width controlled via side padding
    ) { constraints ->

        val full = constraints.maxWidth

        val h = ROW_HEIGHT.roundToPx()

        val inset = ROW_INK_INSET.roundToPx()

        // Measure digit row width.
        val rowWidth = subcompose("rowProbe") {

            DigitRow(
                part = "00",
                capHeight = READOUT_CAP,
                color = MaterialTheme.colorScheme.primary,
                separator = true,
            )

        }.first().measure(
            Constraints()
        ).width

        // Measure natural Start button width.
        val natural = subcompose("probe") {

            WidePill(
                text = "Start",
                icon = Icons.Rounded.PlayArrow,
                onClick = {},
                height = ROW_HEIGHT,
                compact = true,
                fillWidth = false,
                contentPaddingH = IDLE_PADDING_H,
            )

        }.first().measure(
            Constraints(
                minHeight = h,
                maxHeight = h
            )
        ).width

        val collapsedW = maxOf(
            rowWidth - inset,
            natural
        ).coerceAtMost(full)

        val collapsedX = (
            (full - rowWidth) / 2 + rowWidth - collapsedW
        ).coerceIn(0, full - collapsedW)

        val w = lerp(
            collapsedW,
            full,
            e
        )

        val x = lerp(
            collapsedX,
            0,
            e
        )

        // Single Start button.
        val single = if (split < 1f) {

            subcompose("single") {

                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f - split
                    }
                ) {

                    WidePill(
                        text = "Start",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = onToggle,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        height = ROW_HEIGHT,
                        compact = true,
                        contentPaddingH = IDLE_PADDING_H,
                        contentAlpha = label,
                    )
                }

            }.first().measure(
                Constraints.fixed(w, h)
            )

        } else {
            null
        }

        // Expanded Lap, Reset and Start/Stop controls.
        val trio = if (split > 0f) {

            subcompose("trio") {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = split
                        }
                ) {

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(
                            GAP * split
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {

                            WidePill(
                                text = "Lap",
                                onClick = onLap,
                                height = ROW_HEIGHT,
                                compact = true
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {

                            WidePill(
                                text = "Reset",
                                onClick = onReset,
                                outlined = true,
                                content = MaterialTheme.colorScheme.onSurface,
                                height = ROW_HEIGHT,
                                compact = true,
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {

                            startStop(true)
                        }
                    }
                }

            }.first().measure(
                Constraints.fixed(full, h)
            )

        } else {
            null
        }

        layout(full, h) {

            single?.place(x, 0)

            trio?.place(0, 0)
        }
    }
}

// Layout constants.
private const val EXPAND_LEAD_MS = 95
private const val SPLIT_MS = 150
private const val LABEL_FADE_MS = 70

private val IDLE_PADDING_H = 48.dp
private val STACK_GAP = 20.dp       // Reduced gap between readout and controls
private val READOUT_CAP = 56.dp     // Slightly smaller readout size (digits, icon, colons)
private val GAP = 6.dp
private val ROW_HEIGHT = 76.dp
private val LAP_ROW_HEIGHT = 56.dp
private val LAP_GAP = 6.dp

/**
 * Individual lap row with animated colors
 * and press feedback.
 */
@Composable
private fun LapRow(
    lap: Lap,
    isFastest: Boolean,
    isSlowest: Boolean,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeFg) = getLapColors(
        lap.index
    )

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press animation.
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "lapScale"
    )

    val targetTimeColor = when {
        isFastest -> MaterialTheme.colorScheme.tertiary
        isSlowest -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Smooth color transition.
    val timeColor by animateColorAsState(
        targetValue = targetTimeColor,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "timeColor"
    )

    val (m, s, cs) = lap.split.stopwatchParts()

    val (tm, ts, tcs) = lap.total.stopwatchParts()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier
            .fillMaxWidth()
            .height(LAP_ROW_HEIGHT)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // Lap number badge.
            Surface(
                color = badgeBg,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = lap.index.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                    )
                }
            }

            // Lap split time.
            Text(
                text = "$m:$s.$cs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = timeColor,
                modifier = Modifier.weight(1f),
            )

            // Total elapsed time.
            Text(
                text = "$tm:$ts.$tcs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
