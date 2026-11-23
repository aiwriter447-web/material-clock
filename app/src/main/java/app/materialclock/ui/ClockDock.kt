package app.materialclock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

val DOCK_HEIGHT = 68.dp

// LIVING FLOW: Calm, critically-damped fluid motion without jitter or bounce
private fun <T> dockFluidSpring() = spring<T>(
    dampingRatio = 0.92f,
    stiffness = 240f
)

/**
 * Frosted glass treatment for the top Title Pill (no white border).
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
 * Frosted glass + delicate white border & underside rim highlight exclusively on the outer Dock Bar.
 */
fun Modifier.dockFrostedGlass(
    hazeState: HazeState?,
    shape: Shape = CircleShape
): Modifier = composed {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surface
    val glassFill = if (isLight) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
    }

    val topRimColor = if (isLight) {
        Color.White.copy(alpha = 0.45f)
    } else {
        Color.White.copy(alpha = 0.16f)
    }
    val bottomRimColor = if (isLight) {
        Color.White.copy(alpha = 0.95f)
    } else {
        Color.White.copy(alpha = 0.46f)
    }

    val baseBlurModifier = if (hazeState != null) {
        this
            .shadow(
                elevation = if (isLight) 7.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isLight) 0.12f else 0.35f),
                spotColor = Color.Black.copy(alpha = if (isLight) 0.16f else 0.45f)
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
                elevation = if (isLight) 7.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isLight) 0.12f else 0.35f),
                spotColor = Color.Black.copy(alpha = if (isLight) 0.16f else 0.45f)
            )
            .clip(shape)
            .background(glassFill)
    }

    baseBlurModifier.drawWithContent {
        drawContent()

        val strokePx = 1.15.dp.toPx()
        if (size.width > strokePx && size.height > strokePx) {
            val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to topRimColor,
                    0.48f to topRimColor.copy(alpha = 0.65f),
                    0.78f to bottomRimColor.copy(alpha = 0.85f),
                    1.0f to bottomRimColor
                ),
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(size.width - strokePx, size.height - strokePx),
                cornerRadius = cornerRadius,
                style = Stroke(width = strokePx)
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
                    onClick = { onSelect(tab) }
                )
            }
        }
    } else {
        Row(
            modifier = containerModifier
                .height(DOCK_HEIGHT)
                .padding(horizontal = 7.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { tab ->
                DockItem(
                    tab = tab,
                    selected = tab == selected,
                    vertical = false,
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
        animationSpec = tween(durationMillis = 280),
        label = "dockPillBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 280),
        label = "dockContentTint"
    )

    val targetHorizontalPad = when {
        vertical -> 10f
        selected && tab == Tab.WORLD -> 13f
        selected -> 16f
        else -> 11f
    }

    val horizontalPad by animateFloatAsState(
        targetValue = targetHorizontalPad,
        animationSpec = dockFluidSpring(),
        label = "dockHorizontalPad"
    )

    val iconVector = when (tab) {
        Tab.ALARMS -> if (selected) AlarmFilledCustom else AlarmOutlinedCustom
        Tab.WORLD -> if (selected) WorldFilledCustom else WorldOutlinedCustom
        Tab.TIMERS -> if (selected) TimerFilledCustom else TimerOutlinedCustom
        Tab.STOPWATCH -> if (selected) StopwatchFilledCustom else StopwatchOutlinedCustom
    }

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
                vertical = if (vertical) 10.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            if (!vertical) {
                AnimatedVisibility(
                    visible = selected,
                    enter = expandVertically(
                        animationSpec = dockFluidSpring(),
                        expandFrom = Alignment.Top
                    ) + fadeIn(animationSpec = tween(250)),
                    exit = shrinkVertically(
                        animationSpec = dockFluidSpring(),
                        shrinkTowards = Alignment.Top
                    ) + fadeOut(animationSpec = tween(160))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.sp
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
            initialScale = 0.75f
        ) + fadeIn(animationSpec = tween(240)),
        exit = shrinkHorizontally(
            animationSpec = dockFluidSpring(),
            shrinkTowards = Alignment.Start
        ) + scaleOut(
            animationSpec = dockFluidSpring(),
            targetScale = 0.75f
        ) + fadeOut(animationSpec = tween(180)),
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
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shadowElevation = 6.dp,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(62.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = label,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/* ── Custom Google Clock Material 3 Vector Icons (Filled & Outlined) ───────────────── */

private val AlarmFilledCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "AlarmFilledCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Left & right alarm bell ears
        path(fill = SolidColor(Color.Black)) {
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineTo(3.29f, 7.24f)
            lineTo(7.88f, 3.39f)
            close()
            moveTo(22f, 5.72f)
            lineTo(17.4f, 1.86f)
            lineTo(16.11f, 3.39f)
            lineTo(20.71f, 7.25f)
            lineTo(22f, 5.72f)
            close()
        }
        // Solid clock face with EvenOdd cutout hands
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.03f, 22f, 12f, 22f)
            curveTo(16.97f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 8.03f, 16.97f, 4f, 12f, 4f)
            close()
            moveTo(12.5f, 8f)
            lineTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(15.75f, 16.85f)
            lineTo(16.5f, 15.62f)
            lineTo(12.5f, 13.25f)
            lineTo(12.5f, 8f)
            close()
        }
    }.build()
}

private val AlarmOutlinedCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "AlarmOutlinedCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Bells
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineTo(3.29f, 7.24f)
            lineTo(7.88f, 3.39f)
            close()
            moveTo(22f, 5.72f)
            lineTo(17.4f, 1.86f)
            lineTo(16.11f, 3.39f)
            lineTo(20.71f, 7.25f)
            lineTo(22f, 5.72f)
            close()
            // Hands
            moveTo(12.5f, 8f)
            lineTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(15.75f, 16.85f)
            lineTo(16.5f, 15.62f)
            lineTo(12.5f, 13.25f)
            lineTo(12.5f, 8f)
            close()
            // Outer & inner clock ring
            moveTo(12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.03f, 22f, 12f, 22f)
            curveTo(16.97f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 8.03f, 16.97f, 4f, 12f, 4f)
            close()
            moveTo(12f, 20f)
            curveTo(8.13f, 20f, 5f, 16.87f, 5f, 13f)
            curveTo(5f, 9.13f, 8.13f, 6f, 12f, 6f)
            curveTo(15.87f, 6f, 19f, 9.13f, 19f, 13f)
            curveTo(19f, 16.87f, 15.87f, 20f, 12f, 20f)
            close()
        }
    }.build()
}

private val WorldFilledCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "WorldFilledCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            // Outer globe sphere
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            // Americas continent cutout
            moveTo(11f, 19.93f)
            curveTo(7.05f, 19.44f, 4f, 16.08f, 4f, 12f)
            curveTo(4f, 11.38f, 4.08f, 10.79f, 4.21f, 10.21f)
            lineTo(9f, 15f)
            lineTo(9f, 16f)
            curveTo(9f, 17.1f, 9.9f, 18f, 11f, 18f)
            lineTo(11f, 19.93f)
            close()
            // Eurasia & Africa continent cutout
            moveTo(17.9f, 17.39f)
            curveTo(17.64f, 16.58f, 16.9f, 16f, 16f, 16f)
            lineTo(15f, 16f)
            lineTo(15f, 13f)
            curveTo(15f, 12.45f, 14.55f, 12f, 14f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 10f)
            lineTo(10f, 10f)
            curveTo(10.55f, 10f, 11f, 9.55f, 11f, 9f)
            lineTo(11f, 7f)
            lineTo(13f, 7f)
            curveTo(14.1f, 7f, 15f, 6.1f, 15f, 5f)
            lineTo(15f, 4.59f)
            curveTo(17.93f, 5.78f, 20f, 8.65f, 20f, 12f)
            curveTo(20f, 14.08f, 19.2f, 15.97f, 17.9f, 17.39f)
            close()
        }
    }.build()
}

private val WorldOutlinedCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "WorldOutlinedCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(11f, 19.93f)
            curveTo(7.05f, 19.44f, 4f, 16.08f, 4f, 12f)
            curveTo(4f, 11.38f, 4.08f, 10.79f, 4.21f, 10.21f)
            lineTo(9f, 15f)
            lineTo(9f, 16f)
            curveTo(9f, 17.1f, 9.9f, 18f, 11f, 18f)
            lineTo(11f, 19.93f)
            close()
            moveTo(17.9f, 17.39f)
            curveTo(17.64f, 16.58f, 16.9f, 16f, 16f, 16f)
            lineTo(15f, 16f)
            lineTo(15f, 13f)
            curveTo(15f, 12.45f, 14.55f, 12f, 14f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 10f)
            lineTo(10f, 10f)
            curveTo(10.55f, 10f, 11f, 9.55f, 11f, 9f)
            lineTo(11f, 7f)
            lineTo(13f, 7f)
            curveTo(14.1f, 7f, 15f, 6.1f, 15f, 5f)
            lineTo(15f, 4.59f)
            curveTo(17.93f, 5.78f, 20f, 8.65f, 20f, 12f)
            curveTo(20f, 14.08f, 19.2f, 15.97f, 17.9f, 17.39f)
            close()
        }
    }.build()
}

private val TimerFilledCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "TimerFilledCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            lineTo(6f, 8f)
            lineTo(10f, 12f)
            lineTo(6f, 16f)
            lineTo(6f, 22f)
            lineTo(18f, 22f)
            lineTo(18f, 16f)
            lineTo(14f, 12f)
            lineTo(18f, 8f)
            lineTo(18f, 2f)
            lineTo(6f, 2f)
            close()
        }
    }.build()
}

private val TimerOutlinedCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "TimerOutlinedCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            lineTo(6f, 8f)
            lineTo(10f, 12f)
            lineTo(6f, 16f)
            lineTo(6f, 22f)
            lineTo(18f, 22f)
            lineTo(18f, 16f)
            lineTo(14f, 12f)
            lineTo(18f, 8f)
            lineTo(18f, 2f)
            lineTo(6f, 2f)
            close()
            moveTo(16f, 16.5f)
            lineTo(16f, 20f)
            lineTo(8f, 20f)
            lineTo(8f, 16.5f)
            lineTo(12f, 12.5f)
            lineTo(16f, 16.5f)
            close()
            moveTo(12f, 11.5f)
            lineTo(8f, 7.5f)
            lineTo(8f, 4f)
            lineTo(16f, 4f)
            lineTo(16f, 7.5f)
            lineTo(12f, 11.5f)
            close()
        }
    }.build()
}

private val StopwatchFilledCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "StopwatchFilledCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Top crown bar
        path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 1f)
            lineTo(15f, 1f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            close()
        }
        // Solid body + side pusher with EvenOdd hand cutout
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(19.03f, 7.39f)
            lineTo(20.45f, 5.97f)
            curveTo(20.02f, 5.46f, 19.55f, 4.98f, 19.04f, 4.56f)
            lineTo(17.62f, 5.98f)
            curveTo(16.07f, 4.74f, 14.12f, 4f, 12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.02f, 22f, 12f, 22f)
            curveTo(16.98f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
            close()
            moveTo(11f, 8f)
            lineTo(13f, 8f)
            lineTo(13f, 14f)
            lineTo(11f, 14f)
            close()
        }
    }.build()
}

private val StopwatchOutlinedCustom: ImageVector by lazy {
    ImageVector.Builder(
        name = "StopwatchOutlinedCustom",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Top bar
            moveTo(9f, 1f)
            lineTo(15f, 1f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            close()
            // Hand
            moveTo(11f, 8f)
            lineTo(13f, 8f)
            lineTo(13f, 14f)
            lineTo(11f, 14f)
            close()
            // Outer & inner ring + side pusher
            moveTo(19.03f, 7.39f)
            lineTo(20.45f, 5.97f)
            curveTo(20.02f, 5.46f, 19.55f, 4.98f, 19.04f, 4.56f)
            lineTo(17.62f, 5.98f)
            curveTo(16.07f, 4.74f, 14.12f, 4f, 12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.02f, 22f, 12f, 22f)
            curveTo(16.98f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
            close()
            moveTo(12f, 20f)
            curveTo(8.13f, 20f, 5f, 16.87f, 5f, 13f)
            curveTo(5f, 9.13f, 8.13f, 6f, 12f, 6f)
            curveTo(15.87f, 6f, 19f, 9.13f, 19f, 13f)
            curveTo(19f, 16.87f, 15.87f, 20f, 12f, 20f)
            close()
        }
    }.build()
}
