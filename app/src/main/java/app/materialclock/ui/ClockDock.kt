package app.materialclock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

private val AlarmFilledIcon: ImageVector
    get() {
        if (_alarmFilledIcon != null) return _alarmFilledIcon!!
        _alarmFilledIcon = ImageVector.Builder(
            name = "AlarmFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(12f, 22f)
            curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
            curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
            curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
            curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
            close()
            moveTo(12.5f, 12.8f)
            lineTo(16.5f, 16.8f)
            lineTo(15.1f, 18.2f)
            lineTo(10.5f, 13.6f)
            verticalLineTo(7f)
            horizontalLineTo(12.5f)
            verticalLineTo(12.8f)
            close()
            moveTo(4.6f, 5.8f)
            lineTo(1.8f, 2.9f)
            lineTo(3.2f, 1.5f)
            lineTo(6f, 4.4f)
            lineTo(4.6f, 5.8f)
            close()
            moveTo(19.4f, 5.8f)
            lineTo(18f, 4.4f)
            lineTo(20.8f, 1.5f)
            lineTo(22.2f, 2.9f)
            lineTo(19.4f, 5.8f)
            close()
        }.build()
        return _alarmFilledIcon!!
    }
private var _alarmFilledIcon: ImageVector? = null

private val WorldFilledIcon: ImageVector
    get() {
        if (_worldFilledIcon != null) return _worldFilledIcon!!
        _worldFilledIcon = ImageVector.Builder(
            name = "WorldFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
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
            verticalLineTo(16f)
            curveTo(9f, 17.1f, 9.9f, 18f, 11f, 18f)
            verticalLineTo(19.93f)
            close()
            moveTo(17.9f, 17.39f)
            curveTo(17.64f, 16.58f, 16.9f, 16f, 16f, 16f)
            horizontalLineTo(15f)
            verticalLineTo(13f)
            curveTo(15f, 12.45f, 14.55f, 12f, 14f, 12f)
            horizontalLineTo(8f)
            verticalLineTo(10f)
            horizontalLineTo(10f)
            curveTo(10.55f, 10f, 11f, 9.55f, 11f, 9f)
            verticalLineTo(7f)
            horizontalLineTo(13f)
            curveTo(14.1f, 7f, 15f, 6.1f, 15f, 5f)
            verticalLineTo(4.59f)
            curveTo(17.93f, 5.77f, 20f, 8.65f, 20f, 12f)
            curveTo(20f, 14.08f, 19.2f, 15.97f, 17.9f, 17.39f)
            close()
        }.build()
        return _worldFilledIcon!!
    }
private var _worldFilledIcon: ImageVector? = null

private val TimerFilledIcon: ImageVector
    get() {
        if (_timerFilledIcon != null) return _timerFilledIcon!!
        _timerFilledIcon = ImageVector.Builder(
            name = "TimerFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 22f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            verticalLineToRelative(-3.5f)
            curveToRelative(0f, -0.53f, 0.21f, -1.04f, 0.59f, -1.41f)
            lineTo(10.17f, 11.5f)
            lineTo(6.59f, 7.91f)
            curveTo(6.21f, 7.54f, 6f, 7.03f, 6f, 6.5f)
            verticalLineTo(3f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            horizontalLineToRelative(8f)
            curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
            verticalLineToRelative(3.5f)
            curveToRelative(0f, 0.53f, -0.21f, 1.04f, -0.59f, 1.41f)
            lineTo(13.83f, 11.5f)
            lineToRelative(3.58f, 3.59f)
            curveToRelative(0.38f, 0.37f, 0.59f, 0.88f, 0.59f, 1.41f)
            verticalLineTo(20f)
            curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
            horizontalLineTo(8f)
            close()
        }.build()
        return _timerFilledIcon!!
    }
private var _timerFilledIcon: ImageVector? = null

private val StopwatchFilledIcon: ImageVector
    get() {
        if (_stopwatchFilledIcon != null) return _stopwatchFilledIcon!!
        _stopwatchFilledIcon = ImageVector.Builder(
            name = "StopwatchFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(12f, 22f)
            curveTo(7.03f, 22f, 3f, 17.97f, 3f, 13f)
            curveTo(3f, 8.03f, 7.03f, 4f, 12f, 4f)
            curveTo(16.97f, 4f, 21f, 8.03f, 21f, 13f)
            curveTo(21f, 17.97f, 16.97f, 22f, 12f, 22f)
            close()
            moveTo(9f, 1f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(2f)
            horizontalLineTo(9f)
            close()
            moveTo(11f, 13f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(5f)
            close()
        }.build()
        return _stopwatchFilledIcon!!
    }
private var _stopwatchFilledIcon: ImageVector? = null

@Composable
fun ClockDock(
    destinations: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = destinations.indexOf(selected).takeIf { it >= 0 } ?: 0

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val buttonBounds = remember { mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>() }
        val targetRect = buttonBounds[selectedIndex]
        val button0Rect = buttonBounds[0]
        val pillRelativeX = (targetRect?.left ?: 0f) - (button0Rect?.left ?: 0f)

        val pillAnimatedX by animateFloatAsState(
            targetValue = pillRelativeX,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "pillX",
        )
        val pillAnimatedWidth by animateFloatAsState(
            targetValue = targetRect?.width ?: 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "pillWidth",
        )
        val pillColor = MaterialTheme.colorScheme.primary

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .height(56.dp)
                    .drawBehind {
                        if (pillAnimatedWidth > 0f) {
                            drawRoundRect(
                                color = pillColor,
                                topLeft = Offset(pillAnimatedX, 0f),
                                size = Size(pillAnimatedWidth, size.height),
                                cornerRadius = CornerRadius(size.height / 2f),
                            )
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = destination == selected
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    
                    val tabScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.90f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                        label = "tabScale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .graphicsLayer {
                                scaleX = tabScale
                                scaleY = tabScale
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelect(destination) }
                            )
                            .onGloballyPositioned { coords ->
                                buttonBounds[index] = coords.boundsInParent()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val activeIcon = remember(destination) {
                                when (destination) {
                                    Tab.ALARMS -> AlarmFilledIcon
                                    Tab.WORLD -> WorldFilledIcon
                                    Tab.TIMERS -> TimerFilledIcon
                                    Tab.STOPWATCH -> StopwatchFilledIcon
                                }
                            }
                            val icon = if (isSelected) activeIcon else destination.icon
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

                            Icon(
                                imageVector = icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(24.dp),
                                tint = contentColor
                            )

                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(
                                    animationSpec = tween(300, easing = LinearOutSlowInEasing)
                                ),
                                exit = shrinkHorizontally(
                                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                                ),
                            ) {
                                Text(
                                    text = destination.label,
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingAddButton(
    visible: Boolean, 
    label: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val grow by animateFloatAsState(if (visible) 1f else 0f, spec, label = "addGrow")

    val glyph by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "addGlyph",
    )

    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -90f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "addRotate"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "fabPressScale"
    )

    if (grow <= 0.001f) return

    FloatingActionButton(
        onClick = onClick,
        interactionSource = interactionSource,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(lerp(FAB_SIZE / 2, FAB_CORNER, grow)),
        modifier = modifier
            .size(FAB_SIZE)
            .graphicsLayer { 
                scaleX = grow * pressScale
                scaleY = grow * pressScale 
            },
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = label,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = glyph
                    scaleY = glyph
                    alpha = glyph
                    rotationZ = rotation
                },
        )
    }
}

val DOCK_HEIGHT = 72.dp
private val FAB_SIZE = DOCK_HEIGHT
private val FAB_CORNER = 24.dp
