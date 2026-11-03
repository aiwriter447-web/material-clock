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
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
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
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 22f)
            curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
            curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
            curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
            curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
            close()
            moveTo(12.5f, 8.6f)
            lineTo(8.9f, 12.2f)
            lineTo(7.5f, 10.8f)
            lineTo(12f, 6.3f)
            lineTo(12.5f, 6.3f)
            lineTo(12.5f, 8.6f)
            close()
            moveTo(4.15f, 5.3f)
            lineTo(2.45f, 3.6f)
            curveTo(2.17f, 3.32f, 2.17f, 2.9f, 2.45f, 2.62f)
            curveTo(2.73f, 2.34f, 3.15f, 2.34f, 3.43f, 2.62f)
            lineTo(5.13f, 4.32f)
            curveTo(5.41f, 4.6f, 5.41f, 5.02f, 5.13f, 5.3f)
            curveTo(4.85f, 5.58f, 4.43f, 5.58f, 4.15f, 5.3f)
            close()
            moveTo(19.85f, 5.3f)
            curveTo(19.57f, 5.58f, 19.15f, 5.58f, 18.87f, 5.3f)
            curveTo(18.59f, 5.02f, 18.59f, 4.6f, 18.87f, 4.32f)
            lineTo(20.57f, 2.62f)
            curveTo(20.85f, 2.34f, 21.27f, 2.34f, 21.55f, 2.62f)
            curveTo(21.83f, 2.9f, 21.83f, 3.32f, 21.55f, 3.6f)
            lineTo(19.85f, 5.3f)
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
            curveTo(7.45f, 22f, 6.98f, 21.8f, 6.59f, 21.41f)
            curveTo(6.2f, 21.02f, 6f, 20.55f, 6f, 20f)
            lineTo(6f, 16.85f)
            curveTo(6f, 16.42f, 6.13f, 16.01f, 6.38f, 15.65f)
            curveTo(6.63f, 15.29f, 6.96f, 15.01f, 7.35f, 14.8f)
            lineTo(10f, 13f)
            lineTo(10f, 11f)
            lineTo(7.35f, 9.2f)
            curveTo(6.96f, 8.99f, 6.63f, 8.71f, 6.38f, 8.35f)
            curveTo(6.13f, 7.99f, 6f, 7.58f, 6f, 7.15f)
            lineTo(6f, 4f)
            curveTo(6f, 3.45f, 6.2f, 2.98f, 6.59f, 2.59f)
            curveTo(6.98f, 2.2f, 7.45f, 2f, 8f, 2f)
            lineTo(16f, 2f)
            curveTo(16.55f, 2f, 17.02f, 2.2f, 17.41f, 2.59f)
            curveTo(17.8f, 2.98f, 18f, 3.45f, 18f, 4f)
            lineTo(18f, 7.15f)
            curveTo(18f, 7.58f, 17.87f, 7.99f, 17.62f, 8.35f)
            curveTo(17.37f, 8.71f, 17.04f, 8.99f, 16.65f, 9.2f)
            lineTo(14f, 11f)
            lineTo(14f, 13f)
            lineTo(16.65f, 14.8f)
            curveTo(17.04f, 15.01f, 17.37f, 15.29f, 17.62f, 15.65f)
            curveTo(17.87f, 16.01f, 18f, 16.42f, 18f, 16.85f)
            lineTo(18f, 20f)
            curveTo(18f, 20.55f, 17.8f, 21.02f, 17.41f, 21.41f)
            curveTo(17.02f, 21.8f, 16.55f, 22f, 16f, 22f)
            lineTo(8f, 22f)
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
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 22f)
            curveTo(9.23f, 22f, 6.88f, 21.02f, 4.93f, 19.07f)
            curveTo(2.98f, 17.12f, 2f, 14.77f, 2f, 12f)
            curveTo(2f, 9.23f, 2.98f, 6.88f, 4.93f, 4.93f)
            curveTo(6.88f, 2.98f, 9.23f, 2f, 12f, 2f)
            curveTo(14.77f, 2f, 17.12f, 2.98f, 19.07f, 4.93f)
            curveTo(21.02f, 6.88f, 22f, 9.23f, 22f, 12f)
            curveTo(22f, 14.77f, 21.02f, 17.12f, 19.07f, 19.07f)
            curveTo(17.12f, 21.02f, 14.77f, 22f, 12f, 22f)
            close()
            moveTo(12.5f, 7f)
            lineTo(11.5f, 7f)
            lineTo(11.5f, 12.5f)
            lineTo(15f, 16f)
            lineTo(15.7f, 15.3f)
            lineTo(12.5f, 12.1f)
            lineTo(12.5f, 7f)
            close()
            moveTo(9f, 1f)
            lineTo(15f, 1f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            lineTo(9f, 1f)
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
                            
                            val inactiveIcon = remember(destination) {
                                when (destination) {
                                    Tab.ALARMS -> Icons.Rounded.Alarm
                                    Tab.WORLD -> Icons.Rounded.Public
                                    Tab.TIMERS -> Icons.Rounded.HourglassEmpty
                                    Tab.STOPWATCH -> Icons.Rounded.Timer
                                }
                            }
                            
                            val icon = if (isSelected) activeIcon else inactiveIcon
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
