package app.materialclock.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import app.materialclock.core.Lap
import app.materialclock.core.Stopwatch
import app.materialclock.core.stopwatchParts

/**
 * Logic for generating 44 different vibrant colors
 */
@Composable
private fun getLapColors(index: Int): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    val hue = ((index - 1) * (360f / 44f)) % 360f 
    
    // Increased saturation and lightness for Dark mode to avoid dull/grey colors
    val saturation = if (isDark) 0.75f else 0.7f
    val lightnessBadge = if (isDark) 0.45f else 0.85f
    val lightnessText = if (isDark) 0.95f else 0.2f
    
    val badgeBg = Color.hsl(hue, saturation, lightnessBadge)
    val badgeFg = Color.hsl(hue, saturation, lightnessText)
    
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
        if (stopwatch.laps.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        Spacer(Modifier.height(12.dp))

        StackedDigits(
            parts = listOf(a, b, c),
            capHeight = READOUT_CAP,
            color = MaterialTheme.colorScheme.primary,
            leading = Icons.Rounded.Timer,
            label = "Stopwatch, $a minutes $b seconds $c hundredths" +
                if (stopwatch.running) ", running" else ", stopped",
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(STACK_GAP))

        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            StopwatchControls(
                idle = !stopwatch.running && elapsed.isZero,
                running = stopwatch.running,
                onToggle = onToggle,
                onLap = onLap,
                onReset = onReset,
            )
            Spacer(Modifier.height(14.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(LAP_GAP),
            ) {
                items(stopwatch.laps, key = { it.index }) { lap ->
                    LapRow(lap, fastest == lap.index, slowest == lap.index)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun StopwatchControls(
    idle: Boolean,
    running: Boolean,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit,
) {
    val expanded = !idle
    val expand by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "expand",
    )
    val label by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = tween(durationMillis = LABEL_FADE_MS),
        label = "label",
    )
    val split by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = SPLIT_MS,
            delayMillis = if (expanded) EXPAND_LEAD_MS else 0,
        ),
        label = "split",
    )
    val e = expand.coerceIn(0f, 1f)

    val startStop: @Composable (Boolean) -> Unit = { compact ->
        WidePill(
            text = if (running) "Stop" else "Start",
            icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
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
        Modifier.fillMaxWidth().height(ROW_HEIGHT).padding(horizontal = 20.dp),
    ) { constraints ->
        val full = constraints.maxWidth
        val h = ROW_HEIGHT.roundToPx()

        val inset = ROW_INK_INSET.roundToPx()
        val rowWidth = subcompose("rowProbe") {
            DigitRow(
                part = "00",
                capHeight = READOUT_CAP,
                color = MaterialTheme.colorScheme.primary,
                separator = true,
            )
        }.first().measure(Constraints()).width

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
        }.first().measure(Constraints(minHeight = h, maxHeight = h)).width

        val collapsedW = maxOf(rowWidth - inset, natural).coerceAtMost(full)
        val collapsedX = ((full - rowWidth) / 2 + rowWidth - collapsedW).coerceAtLeast(0)

        val w = lerp(collapsedW, full, e)
        val x = lerp(collapsedX, 0, e)

        val single = if (split < 1f) {
            subcompose("single") {
                Box(Modifier.graphicsLayer { alpha = 1f - split }) {
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
            }.first().measure(Constraints.fixed(w, h))
        } else {
            null
        }

        val trio = if (split > 0f) {
            subcompose("trio") {
                Box(Modifier.graphicsLayer { alpha = split }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(GAP * split)) {
                        Box(Modifier.weight(1f)) {
                            WidePill(text = "Lap", onClick = onLap, height = ROW_HEIGHT, compact = true)
                        }
                        Box(Modifier.weight(1f)) {
                            WidePill(
                                text = "Reset",
                                onClick = onReset,
                                outlined = true,
                                content = MaterialTheme.colorScheme.onSurface,
                                height = ROW_HEIGHT,
                                compact = true,
                            )
                        }
                        Box(Modifier.weight(1f)) { startStop(true) }
                    }
                }
            }.first().measure(Constraints.fixed(full, h))
        } else {
            null
        }

        layout(full, h) {
            single?.place(x, 0)
            trio?.place(0, 0)
        }
    }
}

private const val EXPAND_LEAD_MS = 95
private const val SPLIT_MS = 150
private const val LABEL_FADE_MS = 70
private val IDLE_PADDING_H = 48.dp // Increased width for the single Start button
private val STACK_GAP = 35.dp
private val READOUT_CAP = 66.dp
private val GAP = 6.dp
private val ROW_HEIGHT = 76.dp // Increased height for all main buttons
private val LAP_ROW_HEIGHT = 56.dp // Slightly increased for better touch target
private val LAP_GAP = 6.dp

@Composable
private fun LapRow(lap: Lap, isFastest: Boolean, isSlowest: Boolean) {
    val (badgeBg, badgeFg) = getLapColors(lap.index)
    
    val timeColor = when {
        isFastest -> MaterialTheme.colorScheme.tertiary
        isSlowest -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val (m, s, cs) = lap.split.stopwatchParts()
    val (tm, ts, tcs) = lap.total.stopwatchParts()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier.fillMaxWidth().height(LAP_ROW_HEIGHT),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(color = badgeBg, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        lap.index.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = badgeFg,
                    )
                }
            }
            Text(
                "$m:$s.$cs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = timeColor,
                modifier = Modifier.weight(1f),
            )
            Text(
                "$tm:$ts.$tcs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
