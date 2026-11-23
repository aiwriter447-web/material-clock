package app.materialclock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.cos
import kotlin.math.sin

val DOCK_HEIGHT = 72.dp

// LIVING FLOW: Weighted, calm, critically-damped spring physics (zero jitter or bounce)
private fun <T> dockFluidSpring() = spring<T>(
    dampingRatio = 0.94f,
    stiffness = 210f
)

/**
 * Frosted glass treatment for the top Title Pill (no white outline).
 */
fun Modifier.titlePillFrostedGlass(
    hazeState: HazeState?,
    shape: Shape = CircleShape
): Modifier = composed {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val tintColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)

    if (hazeState != null) {
        this
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = surfaceColor,
                    tint = HazeTint(tintColor),
                    blurRadius = 42.dp,
                    noiseFactor = 0.04f
                )
            )
    } else {
        this
            .clip(shape)
            .background(tintColor)
    }
}

/**
 * Frosted glass + luminous white rim & underside highlight exclusively for the outer Dock Bar.
 */
fun Modifier.dockFrostedGlass(
    hazeState: HazeState?,
    shape: Shape = CircleShape
): Modifier = composed {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surface
    val glassFill = if (isLight) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
    }

    val topRimColor = if (isLight) {
        Color.White.copy(alpha = 0.72f)
    } else {
        Color.White.copy(alpha = 0.22f)
    }
    val bottomRimColor = if (isLight) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.52f)
    }

    val baseBlurModifier = if (hazeState != null) {
        this
            .shadow(
                elevation = if (isLight) 10.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isLight) 0.14f else 0.35f),
                spotColor = Color.Black.copy(alpha = if (isLight) 0.18f else 0.45f)
            )
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = surfaceColor,
                    tint = HazeTint(glassFill),
                    blurRadius = 42.dp,
                    noiseFactor = 0.04f
                )
            )
    } else {
        this
            .shadow(
                elevation = if (isLight) 10.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isLight) 0.14f else 0.35f),
                spotColor = Color.Black.copy(alpha = if (isLight) 0.18f else 0.45f)
            )
            .clip(shape)
            .background(glassFill)
    }

    baseBlurModifier.drawWithContent {
        drawContent()

        val baseStroke = 1.25.dp.toPx()
        val underStroke = 2.4.dp.toPx()
        if (size.width > underStroke && size.height > underStroke) {
            val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)

            // 1. Crisp continuous white border wrapping all rounded corners
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to topRimColor,
                    0.55f to bottomRimColor.copy(alpha = 0.88f),
                    1.0f to bottomRimColor
                ),
                topLeft = Offset(baseStroke / 2f, baseStroke / 2f),
                size = Size(size.width - baseStroke, size.height - baseStroke),
                cornerRadius = cornerRadius,
                style = Stroke(width = baseStroke)
            )

            // 2. Extra underside & lower-corner white highlight so it pops even on bright light themes
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.52f to Color.Transparent,
                    0.78f to bottomRimColor.copy(alpha = 0.55f),
                    1.0f to bottomRimColor
                ),
                topLeft = Offset(underStroke / 2f, underStroke / 2f),
                size = Size(size.width - underStroke, size.height - underStroke),
                cornerRadius = cornerRadius,
                style = Stroke(width = underStroke)
            )
        }
    }
}

@Composable
fun ClockDock(
    destinations: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    vertical: Boolean = false,
    hazeState: HazeState? = null,
    timerRunning: Boolean = false,
    stopwatchRunning: Boolean = false,
    stopwatchSnapshot: Any = Unit,
    modifier: Modifier = Modifier,
) {
    val containerModifier = modifier.dockFrostedGlass(hazeState = hazeState)

    if (vertical) {
        Column(
            modifier = containerModifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            destinations.forEach { tab ->
                DockItem(
                    tab = tab,
                    selected = tab == selected,
                    vertical = true,
                    timerRunning = timerRunning,
                    stopwatchRunning = stopwatchRunning,
                    stopwatchSnapshot = stopwatchSnapshot,
                    onClick = { onSelect(tab) }
                )
            }
        }
    } else {
        Row(
            modifier = containerModifier
                .height(DOCK_HEIGHT)
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { tab ->
                DockItem(
                    tab = tab,
                    selected = tab == selected,
                    vertical = false,
                    timerRunning = timerRunning,
                    stopwatchRunning = stopwatchRunning,
                    stopwatchSnapshot = stopwatchSnapshot,
                    onClick = { onSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    tab: Tab,
    selected: Boolean,
    vertical: Boolean,
    timerRunning: Boolean,
    stopwatchRunning: Boolean,
    stopwatchSnapshot: Any,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = dockFluidSpring(),
        label = "dockItemPress"
    )

    val pillColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 320),
        label = "dockPillBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 320),
        label = "dockContentTint"
    )

    val horizontalPad by animateFloatAsState(
        targetValue = if (vertical) 10f else if (selected) 18f else 13f,
        animationSpec = dockFluidSpring(),
        label = "dockHorizontalPad"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .background(pillColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(
                horizontal = horizontalPad.dp,
                vertical = if (vertical) 10.dp else 7.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DockTabIcon(
                tab = tab,
                tint = contentColor,
                timerRunning = timerRunning,
                stopwatchRunning = stopwatchRunning,
                stopwatchSnapshot = stopwatchSnapshot
            )

            if (!vertical) {
                AnimatedVisibility(
                    visible = selected,
                    enter = expandVertically(
                        animationSpec = dockFluidSpring(),
                        expandFrom = Alignment.Top
                    ) + fadeIn(animationSpec = tween(280)),
                    exit = shrinkVertically(
                        animationSpec = dockFluidSpring(),
                        shrinkTowards = Alignment.Top
                    ) + fadeOut(animationSpec = tween(180))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                letterSpacing = 0.1.sp
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockTabIcon(
    tab: Tab,
    tint: Color,
    timerRunning: Boolean,
    stopwatchRunning: Boolean,
    stopwatchSnapshot: Any
) {
    when (tab) {
        Tab.ALARMS -> AlarmClockVectorIcon(tint = tint, modifier = Modifier.size(22.dp))
        Tab.WORLD -> WorldGlobeVectorIcon(tint = tint, modifier = Modifier.size(22.dp))
        Tab.TIMERS -> LiveHourglassIcon(
            tint = tint,
            isRunning = timerRunning,
            modifier = Modifier.size(22.dp)
        )
        Tab.STOPWATCH -> LiveStopwatchIcon(
            tint = tint,
            isRunning = stopwatchRunning,
            snapshot = stopwatchSnapshot,
            modifier = Modifier.size(22.dp)
        )
    }
}

/* ── Custom Crafted Vector Icons (Consistent 1.85dp Outlined Stroke Weight) ───────── */

@Composable
private fun AlarmClockVectorIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.85.dp.toPx()
        val center = Offset(size.width / 2f, size.height * 0.54f)
        val radius = size.minDimension * 0.35f

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        drawLine(
            color = tint,
            start = Offset(size.width * 0.18f, size.height * 0.25f),
            end = Offset(size.width * 0.33f, size.height * 0.13f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.82f, size.height * 0.25f),
            end = Offset(size.width * 0.67f, size.height * 0.13f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = tint,
            start = Offset(center.x - radius * 0.62f, center.y + radius * 0.78f),
            end = Offset(center.x - radius * 0.85f, center.y + radius * 1.05f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(center.x + radius * 0.62f, center.y + radius * 0.78f),
            end = Offset(center.x + radius * 0.85f, center.y + radius * 1.05f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.52f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.42f, center.y + radius * 0.22f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WorldGlobeVectorIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.85.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.40f

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        drawLine(
            color = tint,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = stroke * 0.85f
        )

        drawOval(
            color = tint,
            topLeft = Offset(center.x - radius * 0.46f, center.y - radius),
            size = Size(radius * 0.92f, radius * 2f),
            style = Stroke(width = stroke * 0.85f)
        )
    }
}

/**
 * Upright Hourglass icon:
 * - When idle: rests upright with sand settled cleanly in the bottom chamber.
 * - When running: sand drains from the top bulb through the narrow neck into the bottom bulb,
 *   and smoothly rotates 180° at the end of each cycle.
 */
@Composable
private fun LiveHourglassIcon(
    tint: Color,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglassSandTransition")
    val rawCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hourglassCycle"
    )

    val targetRotation = if (isRunning && rawCycle > 0.86f) {
        ((rawCycle - 0.86f) / 0.14f) * 180f
    } else {
        0f
    }

    val sandProgress = if (isRunning) {
        (rawCycle / 0.86f).coerceIn(0f, 1f)
    } else {
        1f
    }

    Canvas(modifier = modifier) {
        val stroke = 1.85.dp.toPx()
        val w = size.width
        val h = size.height

        val topY = h * 0.14f
        val bottomY = h * 0.86f
        val midY = h * 0.50f
        val leftX = w * 0.24f
        val rightX = w * 0.76f
        val neckHalf = w * 0.055f

        rotate(degrees = targetRotation, pivot = Offset(w / 2f, midY)) {
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, topY),
                end = Offset(w * 0.82f, topY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, bottomY),
                end = Offset(w * 0.82f, bottomY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            val topChamber = Path().apply {
                moveTo(leftX, topY)
                lineTo(rightX, topY)
                lineTo(w / 2f + neckHalf, midY)
                lineTo(w / 2f - neckHalf, midY)
                close()
            }

            val bottomChamber = Path().apply {
                moveTo(w / 2f - neckHalf, midY)
                lineTo(w / 2f + neckHalf, midY)
                lineTo(rightX, bottomY)
                lineTo(leftX, bottomY)
                close()
            }

            val topFillFraction = (1f - sandProgress) * 0.82f
            if (topFillFraction > 0.03f) {
                clipPath(topChamber) {
                    val sandTopY = midY - (midY - topY) * topFillFraction
                    drawRect(
                        color = tint.copy(alpha = 0.88f),
                        topLeft = Offset(leftX, sandTopY),
                        size = Size(rightX - leftX, midY - sandTopY)
                    )
                }
            }

            if (isRunning && sandProgress < 0.96f) {
                val bottomPileTopY = bottomY - (bottomY - midY) * (sandProgress * 0.82f)
                drawLine(
                    color = tint.copy(alpha = 0.9f),
                    start = Offset(w / 2f, midY),
                    end = Offset(w / 2f, bottomPileTopY),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val bottomFillFraction = 0.12f + (sandProgress * 0.72f)
            clipPath(bottomChamber) {
                val sandTopY = bottomY - (bottomY - midY) * bottomFillFraction
                drawRect(
                    color = tint.copy(alpha = 0.88f),
                    topLeft = Offset(leftX, sandTopY),
                    size = Size(rightX - leftX, bottomY - sandTopY)
                )
            }

            drawPath(path = topChamber, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
            drawPath(path = bottomChamber, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
        }
    }
}

/**
 * Live Stopwatch icon:
 * - Sweeps needle 360° smoothly as seconds move while running.
 * - Freezes at exact angle when paused.
 * - Glides smoothly back to 12 o'clock (0°) when reset.
 */
@Composable
private fun LiveStopwatchIcon(
    tint: Color,
    isRunning: Boolean,
    snapshot: Any,
    modifier: Modifier = Modifier
) {
    var needleDegrees by remember { mutableFloatStateOf(0f) }
    var wasRunning by remember { mutableStateOf(false) }
    var lastPausedSnapshot by remember { mutableStateOf(snapshot) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            wasRunning = true
            var lastNanos = withFrameNanos { it }
            while (true) {
                val nowNanos = withFrameNanos { it }
                val deltaSec = (nowNanos - lastNanos) / 1_000_000_000f
                lastNanos = nowNanos
                needleDegrees = (needleDegrees + deltaSec * 6f) % 360f
            }
        } else if (wasRunning) {
            wasRunning = false
            lastPausedSnapshot = snapshot
        }
    }

    LaunchedEffect(snapshot) {
        if (!isRunning && !wasRunning && snapshot != lastPausedSnapshot) {
            lastPausedSnapshot = snapshot
            needleDegrees = 0f
        }
    }

    val animatedAngle by animateFloatAsState(
        targetValue = needleDegrees,
        animationSpec = if (isRunning) {
            tween(durationMillis = 50, easing = LinearEasing)
        } else {
            dockFluidSpring()
        },
        label = "stopwatchNeedleAngle"
    )

    Canvas(modifier = modifier) {
        val stroke = 1.85.dp.toPx()
        val center = Offset(size.width / 2f, size.height * 0.56f)
        val radius = size.minDimension * 0.35f

        drawLine(
            color = tint,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y - radius - size.height * 0.11f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(center.x - size.width * 0.13f, center.y - radius - size.height * 0.11f),
            end = Offset(center.x + size.width * 0.13f, center.y - radius - size.height * 0.11f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        val pusherAngleRad = Math.toRadians(-45.0)
        val pusherStart = Offset(
            center.x + (radius * cos(pusherAngleRad)).toFloat(),
            center.y + (radius * sin(pusherAngleRad)).toFloat()
        )
        val pusherEnd = Offset(
            center.x + ((radius + size.width * 0.09f) * cos(pusherAngleRad)).toFloat(),
            center.y + ((radius + size.width * 0.09f) * sin(pusherAngleRad)).toFloat()
        )
        drawLine(
            color = tint,
            start = pusherStart,
            end = pusherEnd,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        drawCircle(
            color = tint,
            radius = 1.6.dp.toPx(),
            center = center
        )

        rotate(degrees = animatedAngle, pivot = center) {
            drawLine(
                color = tint,
                start = center,
                end = Offset(center.x, center.y - radius * 0.68f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

/* ── Inline Circular FAB (+ Button) ────────────────────────────────────────────────── */

@Composable
fun FloatingAddButton(
    visible: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            animationSpec = dockFluidSpring(),
            expandFrom = Alignment.Start
        ) + scaleIn(
            animationSpec = dockFluidSpring(),
            initialScale = 0.72f
        ) + fadeIn(animationSpec = tween(260)),
        exit = shrinkHorizontally(
            animationSpec = dockFluidSpring(),
            shrinkTowards = Alignment.Start
        ) + scaleOut(
            animationSpec = dockFluidSpring(),
            targetScale = 0.72f
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val pressScale by animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1f,
            animationSpec = dockFluidSpring(),
            label = "fabPressScale"
        )

        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = 6.dp,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(66.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
