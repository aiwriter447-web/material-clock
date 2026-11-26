package app.materialclock.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import app.materialclock.core.Lap
import app.materialclock.core.Stopwatch
import app.materialclock.core.stopwatchParts
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration
import kotlinx.coroutines.delay

// -----------------------------------------------------------------
// LIVING FLOW: FLUID ANIMATION CONFIGURATION
// -----------------------------------------------------------------

private val FluidSpring = spring<Float>(
    dampingRatio = 0.65f,
    stiffness = 250f
)

private val FluidPlacementSpring = spring<IntOffset>(
    dampingRatio = 0.68f,
    stiffness = 260f
)

private val JellySpring = spring<Float>(
    dampingRatio = 0.44f,
    stiffness = 520f
)

/**
 * Generates 108 unique, non-repeating shades (36 distinct hues x 3 tonal tiers)
 * using a coprime stride (41 mod 108) so consecutive laps also stay visually distinct.
 */
@Composable
private fun getLapColors(index: Int): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    val safeIndex = (index - 1).coerceAtLeast(0)
    val slot = (safeIndex * 41) % 108
    val hueBucket = slot % 36
    val shadeTier = slot / 36

    val hue = (hueBucket * 10f) % 360f

    val saturation: Float
    val lightnessBadge: Float
    val lightnessText: Float

    if (isDark) {
        when (shadeTier) {
            0 -> {
                saturation = 0.78f
                lightnessBadge = 0.34f
                lightnessText = 0.96f
            }
            1 -> {
                saturation = 0.72f
                lightnessBadge = 0.44f
                lightnessText = 0.98f
            }
            else -> {
                saturation = 0.85f
                lightnessBadge = 0.26f
                lightnessText = 0.94f
            }
        }
    } else {
        when (shadeTier) {
            0 -> {
                saturation = 0.76f
                lightnessBadge = 0.88f
                lightnessText = 0.18f
            }
            1 -> {
                saturation = 0.84f
                lightnessBadge = 0.80f
                lightnessText = 0.14f
            }
            else -> {
                saturation = 0.66f
                lightnessBadge = 0.92f
                lightnessText = 0.22f
            }
        }
    }

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

    var readoutKickTrigger by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()
    LaunchedEffect(stopwatch.laps.size) {
        if (stopwatch.laps.isNotEmpty()) listState.animateScrollToItem(0)
    }

    val layoutDirection = LocalLayoutDirection.current
    val showList = stopwatch.running || !elapsed.isZero || stopwatch.laps.isNotEmpty()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availH = maxHeight - contentPadding.calculateTopPadding() - contentPadding.calculateBottomPadding()
        val wide = maxWidth > maxHeight || maxWidth >= 600.dp
        val horizontalCap = minOf(
            HORIZONTAL_READOUT_CAP,
            ((if (wide) maxWidth * 0.48f else maxWidth - 36.dp) / 4.6f)
        ).coerceAtLeast(34.dp)

        val lapsViewport: @Composable (Modifier) -> Unit = { listModifier ->
            if (showList) {
                LazyColumn(
                    state = listState,
                    modifier = listModifier
                        .fillMaxWidth()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.04f to Color.Black,
                                    0.92f to Color.Black,
                                    1.0f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        },
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 12.dp,
                        bottom = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(LAP_GAP),
                ) {
                    val isIdle = !stopwatch.running && elapsed.isZero
                    if (!isIdle) {
                        item(key = "current_lap") {
                            val currentSplit = elapsed.minus(stopwatch.laps.firstOrNull()?.total ?: Duration.ZERO)
                            val currentLap = Lap(
                                index = (stopwatch.laps.firstOrNull()?.index ?: 0) + 1,
                                split = currentSplit,
                                total = elapsed
                            )
                            LapRow(
                                lap = currentLap,
                                isFastest = false,
                                isSlowest = false,
                                isCurrent = true,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(240, easing = EaseOutSine),
                                    placementSpec = FluidPlacementSpring
                                )
                            )
                        }
                    }

                    items(stopwatch.laps, key = { it.index }) { lap ->
                        LapRow(
                            lap = lap,
                            isFastest = fastest == lap.index,
                            isSlowest = slowest == lap.index,
                            isCurrent = false,
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(240, easing = EaseOutSine),
                                placementSpec = FluidPlacementSpring
                            )
                        )
                    }
                }
            } else {
                Spacer(modifier = listModifier)
            }
        }

        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 28.dp,
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    FluidEntrance(delayMillis = 0) {
                        LivingHorizontalReadout(
                            minutes = a,
                            seconds = b,
                            hundredths = c,
                            running = stopwatch.running,
                            kickTrigger = readoutKickTrigger,
                            capHeight = horizontalCap,
                            label = "Stopwatch, $a minutes $b seconds $c hundredths" +
                                if (stopwatch.running) ", running" else ", stopped",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    lapsViewport(Modifier.weight(1f))
                    Spacer(Modifier.height(8.dp))
                    FluidEntrance(delayMillis = 70) {
                        StopwatchControls(
                            idle = !stopwatch.running && elapsed.isZero,
                            running = stopwatch.running,
                            onToggle = {
                                readoutKickTrigger++
                                onToggle()
                            },
                            onLap = {
                                readoutKickTrigger++
                                onLap()
                            },
                            onReset = {
                                readoutKickTrigger++
                                onReset()
                            },
                        )
                    }
                }
            }
        } else {
            val topOffset = minOf(128.dp, availH * 0.18f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding() + 54.dp,
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection)
                    )
            ) {
                Spacer(Modifier.height(topOffset))

                FluidEntrance(delayMillis = 0) {
                    LivingHorizontalReadout(
                        minutes = a,
                        seconds = b,
                        hundredths = c,
                        running = stopwatch.running,
                        kickTrigger = readoutKickTrigger,
                        capHeight = horizontalCap,
                        label = "Stopwatch, $a minutes $b seconds $c hundredths" +
                            if (stopwatch.running) ", running" else ", stopped",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                lapsViewport(Modifier.weight(1f))

                Spacer(Modifier.height(8.dp))

                FluidEntrance(delayMillis = 70) {
                    StopwatchControls(
                        idle = !stopwatch.running && elapsed.isZero,
                        running = stopwatch.running,
                        onToggle = {
                            readoutKickTrigger++
                            onToggle()
                        },
                        onLap = {
                            readoutKickTrigger++
                            onLap()
                        },
                        onReset = {
                            readoutKickTrigger++
                            onReset()
                        },
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// LIVING FLOW: STAGGERED ENTRANCE & CLEAN READOUT
// -----------------------------------------------------------------

@Composable
private fun FluidEntrance(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 340, easing = EaseOutSine),
        label = "fluidEntranceAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.90f,
        animationSpec = FluidSpring,
        label = "fluidEntranceScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 32f,
        animationSpec = FluidSpring,
        label = "fluidEntranceTranslation"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
            this.translationY = translationY
        }
    ) {
        content()
    }
}

@Composable
private fun LivingHorizontalReadout(
    minutes: String,
    seconds: String,
    hundredths: String,
    running: Boolean,
    kickTrigger: Int,
    capHeight: Dp,
    label: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    var kickActive by remember { mutableStateOf(false) }

    LaunchedEffect(kickTrigger) {
        if (kickTrigger > 0) {
            kickActive = true
            delay(95)
            kickActive = false
        }
    }

    val infinite = rememberInfiniteTransition(label = "stopwatchLivingPulse")
    val rawBreath by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rawBreath"
    )

    val activeWeight by animateFloatAsState(
        targetValue = if (running) 1f else 0f,
        animationSpec = FluidSpring,
        label = "activeReadoutWeight"
    )

    val interactionScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            kickActive -> 1.045f
            else -> 1f
        },
        animationSpec = JellySpring,
        label = "readoutInteractionScale"
    )

    val breathingScale = 1f + (rawBreath * 0.016f * activeWeight)
    val combinedScale = breathingScale * interactionScale
    val primary = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clearAndSetSemantics { contentDescription = label }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    isPressed = false
                }
            }
            .graphicsLayer {
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .padding(vertical = 4.dp)
    ) {
        Numerals(
            text = "$minutes:$seconds:$hundredths",
            capHeight = capHeight,
            color = primary,
            width = ClockFace.TIMER_WIDTH,
            weight = ClockFace.TIMER_WEIGHT,
            slashedZero = true
        )
    }
}

// -----------------------------------------------------------------
// LIVING FLOW: REAL-TIME INTERACTIVE JELLY CONTROLS
// -----------------------------------------------------------------

@Composable
private fun JellyActionWrapper(
    onClick: () -> Unit,
    hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    pulseActive: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable (onTriggeredClick: () -> Unit) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "controlPulse")
    val rawPulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "controlPulseScale"
    )

    val targetScale = when {
        isPressed -> 0.88f
        pulseActive -> rawPulse
        else -> 1f
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = JellySpring,
        label = "jellyControlScale"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isPressed = true
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    isPressed = false
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        content {
            haptic.performHapticFeedback(hapticType)
            onClick()
        }
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
        animationSpec = FluidSpring,
        label = "expand",
    )

    val label by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "label",
    )

    val split by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 220f
        ),
        label = "split",
    )

    val e = expand.coerceIn(0f, 1f)

    val startStop: @Composable (Boolean) -> Unit = { compact ->
        val containerColor by animateColorAsState(
            targetValue = if (running) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 220f),
            label = "startStopContainer"
        )
        val contentColor by animateColorAsState(
            targetValue = if (running) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 220f),
            label = "startStopContent"
        )

        JellyActionWrapper(
            onClick = onToggle,
            hapticType = HapticFeedbackType.LongPress,
            pulseActive = running
        ) { triggeredClick ->
            WidePill(
                text = if (running) "Stop" else "Start",
                icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                onClick = triggeredClick,
                container = containerColor,
                content = contentColor,
                height = ROW_HEIGHT,
                compact = compact,
            )
        }
    }

    SubcomposeLayout(
        Modifier.fillMaxWidth().height(ROW_HEIGHT),
    ) { constraints ->
        val full = constraints.maxWidth
        val h = ROW_HEIGHT.roundToPx()

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

        // Start button horizontal length matches Pause / +10s button proportions
        val collapsedW = ((full - IDLE_PADDING_H.roundToPx() * 2) / 2)
            .coerceAtLeast(natural)
            .coerceAtMost(full)
        val collapsedX = (full - collapsedW) / 2

        val expandedW = full - 40.dp.roundToPx()
        val expandedX = 20.dp.roundToPx()

        val w = lerp(collapsedW, expandedW, e)
        val x = lerp(collapsedX, expandedX, e)

        val single = if (split < 1f) {
            subcompose("single") {
                Box(Modifier.graphicsLayer { alpha = 1f - split }) {
                    JellyActionWrapper(
                        onClick = onToggle,
                        hapticType = HapticFeedbackType.LongPress
                    ) { triggeredClick ->
                        WidePill(
                            text = "Start",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = triggeredClick,
                            container = MaterialTheme.colorScheme.primaryContainer,
                            content = MaterialTheme.colorScheme.onPrimaryContainer,
                            height = ROW_HEIGHT,
                            compact = true,
                            contentPaddingH = IDLE_PADDING_H,
                            contentAlpha = label,
                        )
                    }
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
                            JellyActionWrapper(
                                onClick = onLap,
                                hapticType = HapticFeedbackType.TextHandleMove
                            ) { triggeredClick ->
                                WidePill(
                                    text = "Lap",
                                    onClick = triggeredClick,
                                    height = ROW_HEIGHT,
                                    compact = true
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            JellyActionWrapper(
                                onClick = onReset,
                                hapticType = HapticFeedbackType.LongPress
                            ) { triggeredClick ->
                                WidePill(
                                    text = "Reset",
                                    onClick = triggeredClick,
                                    outlined = true,
                                    content = MaterialTheme.colorScheme.onSurface,
                                    height = ROW_HEIGHT,
                                    compact = true,
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) { startStop(true) }
                    }
                }
            }.first().measure(Constraints.fixed(expandedW, h))
        } else {
            null
        }

        layout(full, h) {
            single?.place(x, 0)
            trio?.place(expandedX, 0)
        }
    }
}

private val IDLE_PADDING_H = 48.dp
private val HORIZONTAL_READOUT_CAP = 54.dp
private val GAP = 8.dp
private val ROW_HEIGHT = 64.dp
private val LAP_ROW_HEIGHT = 56.dp
private val LAP_GAP = 6.dp

@Composable
private fun LapRow(
    lap: Lap,
    isFastest: Boolean,
    isSlowest: Boolean,
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeFg) = getLapColors(lap.index)
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = JellySpring,
        label = "lapScale"
    )

    val targetContainerColor = when {
        isPressed -> MaterialTheme.colorScheme.surfaceContainerHighest
        isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 240f),
        label = "lapContainerColor"
    )

    val targetTimeColor = when {
        isFastest -> MaterialTheme.colorScheme.tertiary
        isSlowest -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val timeColor by animateColorAsState(
        targetValue = targetTimeColor,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 240f),
        label = "timeColor"
    )

    val (m, s, cs) = lap.split.stopwatchParts()
    val (tm, ts, tcs) = lap.total.stopwatchParts()

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier
            .fillMaxWidth()
            .height(LAP_ROW_HEIGHT)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    isPressed = false
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
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
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                    )
                }
            }
            Text(
                "$m:$s.$cs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = timeColor,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(
                "$tm:$ts.$tcs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
