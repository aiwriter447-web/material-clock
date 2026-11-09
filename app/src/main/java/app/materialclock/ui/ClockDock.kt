package app.materialclock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.LaunchedEffect
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

// --- 100% UNTOUCHED ICONS (Shape guaranteed) ---

private val AlarmFilledCustom: ImageVector
    get() {
        if (_alarmFilledCustom != null) return _alarmFilledCustom!!
        _alarmFilledCustom = ImageVector.Builder(
            name = "AlarmFilledCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4f)
            curveTo(16.97f, 4f, 21f, 8.03f, 21f, 13f)
            curveTo(21f, 17.97f, 16.97f, 22f, 12f, 22f)
            curveTo(7.03f, 22f, 3f, 17.97f, 3f, 13f)
            curveTo(3f, 8.03f, 7.03f, 4f, 12f, 4f)
            close()
            moveTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(15.25f, 16.55f)
            lineTo(16f, 15.32f)
            lineTo(12.5f, 13.25f)
            lineTo(12.5f, 8f)
            close()
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineTo(3.29f, 7.25f)
            close()
            moveTo(22f, 5.72f)
            lineTo(17.4f, 1.86f)
            lineTo(16.11f, 3.39f)
            lineTo(20.71f, 7.25f)
            close()
        }.build()
        return _alarmFilledCustom!!
    }
private var _alarmFilledCustom: ImageVector? = null

private val AlarmOutlinedCustom: ImageVector
    get() {
        if (_alarmOutlinedCustom != null) return _alarmOutlinedCustom!!
        _alarmOutlinedCustom = ImageVector.Builder(
            name = "AlarmOutlinedCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4f)
            curveTo(16.97f, 4f, 21f, 8.03f, 21f, 13f)
            curveTo(21f, 17.97f, 16.97f, 22f, 12f, 22f)
            curveTo(7.03f, 22f, 3f, 17.97f, 3f, 13f)
            curveTo(3f, 8.03f, 7.03f, 4f, 12f, 4f)
            close()
            moveTo(12f, 6f)
            curveTo(8.13f, 6f, 5f, 9.13f, 5f, 13f)
            curveTo(5f, 16.87f, 8.13f, 20f, 12f, 20f)
            curveTo(15.87f, 20f, 19f, 16.87f, 19f, 13f)
            curveTo(19f, 9.13f, 15.87f, 6f, 12f, 6f)
            close()
            moveTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(15.25f, 16.55f)
            lineTo(16f, 15.32f)
            lineTo(12.5f, 13.25f)
            lineTo(12.5f, 8f)
            close()
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineTo(3.29f, 7.25f)
            close()
            moveTo(22f, 5.72f)
            lineTo(17.4f, 1.86f)
            lineTo(16.11f, 3.39f)
            lineTo(20.71f, 7.25f)
            close()
        }.build()
        return _alarmOutlinedCustom!!
    }
private var _alarmOutlinedCustom: ImageVector? = null

private val TimerFilledCustom: ImageVector
    get() {
        if (_timerFilledCustom != null) return _timerFilledCustom!!
        _timerFilledCustom = ImageVector.Builder(
            name = "TimerFilledCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(4f, 2f)
            lineTo(20f, 2f)
            lineTo(20f, 4f)
            lineTo(18f, 4f)
            lineTo(18f, 7.5f)
            curveTo(18f, 9.8f, 16f, 11.5f, 13.5f, 12f)
            curveTo(16f, 12.5f, 18f, 14.2f, 18f, 16.5f)
            lineTo(18f, 20f)
            lineTo(20f, 20f)
            lineTo(20f, 22f)
            lineTo(4f, 22f)
            lineTo(4f, 20f)
            lineTo(6f, 20f)
            lineTo(6f, 16.5f)
            curveTo(6f, 14.2f, 8f, 12.5f, 10.5f, 12f)
            curveTo(8f, 11.5f, 6f, 9.8f, 6f, 7.5f)
            lineTo(6f, 4f)
            lineTo(4f, 4f)
            close()
            moveTo(8f, 4f)
            lineTo(16f, 4f)
            lineTo(16f, 7.5f)
            curveTo(16f, 9.2f, 14.5f, 10.5f, 12f, 10.5f)
            curveTo(9.5f, 10.5f, 8f, 9.2f, 8f, 7.5f)
            close()
        }.build()
        return _timerFilledCustom!!
    }
private var _timerFilledCustom: ImageVector? = null

private val TimerOutlinedCustom: ImageVector
    get() {
        if (_timerOutlinedCustom != null) return _timerOutlinedCustom!!
        _timerOutlinedCustom = ImageVector.Builder(
            name = "TimerOutlinedCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(4f, 2f)
            lineTo(20f, 2f)
            lineTo(20f, 4f)
            lineTo(18f, 4f)
            lineTo(18f, 7.5f)
            curveTo(18f, 9.8f, 16f, 11.5f, 13.5f, 12f)
            curveTo(16f, 12.5f, 18f, 14.2f, 18f, 16.5f)
            lineTo(18f, 20f)
            lineTo(20f, 20f)
            lineTo(20f, 22f)
            lineTo(4f, 22f)
            lineTo(4f, 20f)
            lineTo(6f, 20f)
            lineTo(6f, 16.5f)
            curveTo(6f, 14.2f, 8f, 12.5f, 10.5f, 12f)
            curveTo(8f, 11.5f, 6f, 9.8f, 6f, 7.5f)
            lineTo(6f, 4f)
            lineTo(4f, 4f)
            close()
            moveTo(8f, 4f)
            lineTo(16f, 4f)
            lineTo(16f, 7.5f)
            curveTo(16f, 9.2f, 14.5f, 10.5f, 12f, 10.5f)
            curveTo(9.5f, 10.5f, 8f, 9.2f, 8f, 7.5f)
            close()
            moveTo(8f, 20f)
            lineTo(16f, 20f)
            lineTo(16f, 16.5f)
            curveTo(16f, 14.8f, 14.5f, 13.5f, 12f, 13.5f)
            curveTo(9.5f, 13.5f, 8f, 14.8f, 8f, 16.5f)
            close()
        }.build()
        return _timerOutlinedCustom!!
    }
private var _timerOutlinedCustom: ImageVector? = null


private val WorldFilledCustom: ImageVector
    get() {
        if (_worldFilledCustom != null) return _worldFilledCustom!!
        _worldFilledCustom = ImageVector.Builder(
            name = "WorldFilledCustom",
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
        return _worldFilledCustom!!
    }
private var _worldFilledCustom: ImageVector? = null

private val WorldOutlinedCustom: ImageVector
    get() {
        if (_worldOutlinedCustom != null) return _worldOutlinedCustom!!
        _worldOutlinedCustom = ImageVector.Builder(
            name = "WorldOutlinedCustom",
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
            moveTo(12f, 20f)
            curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
            curveTo(4f, 11.45f, 4.06f, 10.92f, 4.16f, 10.4f)
            lineTo(8.5f, 14.74f)
            lineTo(8.5f, 16f)
            curveTo(8.5f, 16.55f, 8.95f, 17f, 9.5f, 17f)
            lineTo(10.5f, 17f)
            lineTo(10.5f, 19.82f)
            curveTo(10.98f, 19.94f, 11.48f, 20f, 12f, 20f)
            close()
            moveTo(17.65f, 16.14f)
            curveTo(17.15f, 15.54f, 16.18f, 15f, 15.5f, 15f)
            lineTo(14.5f, 15f)
            lineTo(14.5f, 12f)
            curveTo(14.5f, 11.45f, 14.05f, 11f, 13.5f, 11f)
            lineTo(8.5f, 11f)
            lineTo(8.5f, 9.5f)
            lineTo(10f, 9.5f)
            curveTo(10.55f, 9.5f, 11f, 9.05f, 11f, 8.5f)
            lineTo(11f, 6.5f)
            lineTo(13f, 6.5f)
            curveTo(13.83f, 6.5f, 14.5f, 5.83f, 14.5f, 5f)
            lineTo(14.5f, 4.29f)
            curveTo(17.75f, 5.65f, 20f, 8.61f, 20f, 12f)
            curveTo(20f, 13.68f, 19.4f, 15.17f, 18.42f, 16.36f)
            lineTo(17.65f, 16.14f)
            close()
        }.build()
        return _worldOutlinedCustom!!
    }
private var _worldOutlinedCustom: ImageVector? = null


private val StopwatchFilledCustom: ImageVector
    get() {
        if (_stopwatchFilledCustom != null) return _stopwatchFilledCustom!!
        _stopwatchFilledCustom = ImageVector.Builder(
            name = "StopwatchFilledCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4f)
            curveTo(16.97f, 4f, 21f, 8.03f, 21f, 13f)
            curveTo(21f, 17.97f, 16.97f, 22f, 12f, 22f)
            curveTo(7.03f, 22f, 3f, 17.97f, 3f, 13f)
            curveTo(3f, 8.03f, 7.03f, 4f, 12f, 4f)
            close()
            moveTo(9f, 1f)
            lineTo(15f, 1f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            close()
            moveTo(19.03f, 4.88f)
            lineTo(20.44f, 6.3f)
            lineTo(19.03f, 7.71f)
            lineTo(17.62f, 6.29f)
            close()
            moveTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(13f, 14f)
            lineTo(13f, 8f)
            close()
        }.build()
        return _stopwatchFilledCustom!!
    }
private var _stopwatchFilledCustom: ImageVector? = null

private val StopwatchOutlinedCustom: ImageVector
    get() {
        if (_stopwatchOutlinedCustom != null) return _stopwatchOutlinedCustom!!
        _stopwatchOutlinedCustom = ImageVector.Builder(
            name = "StopwatchOutlinedCustom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4f)
            curveTo(16.97f, 4f, 21f, 8.03f, 21f, 13f)
            curveTo(21f, 17.97f, 16.97f, 22f, 12f, 22f)
            curveTo(7.03f, 22f, 3f, 17.97f, 3f, 13f)
            curveTo(3f, 8.03f, 7.03f, 4f, 12f, 4f)
            close()
            moveTo(12f, 6f)
            curveTo(8.13f, 6f, 5f, 9.13f, 5f, 13f)
            curveTo(5f, 16.87f, 8.13f, 20f, 12f, 20f)
            curveTo(15.87f, 20f, 19f, 16.87f, 19f, 13f)
            curveTo(19f, 9.13f, 15.87f, 6f, 12f, 6f)
            close()
            moveTo(9f, 1f)
            lineTo(15f, 1f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            close()
            moveTo(19.03f, 4.88f)
            lineTo(20.44f, 6.3f)
            lineTo(19.03f, 7.71f)
            lineTo(17.62f, 6.29f)
            close()
            moveTo(11f, 8f)
            lineTo(11f, 14f)
            lineTo(13f, 14f)
            lineTo(13f, 8f)
            close()
        }.build()
        return _stopwatchOutlinedCustom!!
    }
private var _stopwatchOutlinedCustom: ImageVector? = null


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

        // Fluid Motion: Liquid Pill Indicator Slide
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
                    
                    val iconRotation = remember { Animatable(0f) }

                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            when (destination) {
                                Tab.ALARMS -> {
                                    iconRotation.animateTo(15f, tween(50))
                                    iconRotation.animateTo(-15f, tween(100))
                                    iconRotation.animateTo(15f, tween(100))
                                    iconRotation.animateTo(-15f, tween(100))
                                    iconRotation.animateTo(0f, tween(50))
                                }
                                Tab.WORLD -> {
                                    iconRotation.snapTo(0f)
                                    iconRotation.animateTo(360f, tween(600, easing = FastOutSlowInEasing))
                                }
                                Tab.TIMERS -> {
                                    iconRotation.snapTo(0f)
                                    iconRotation.animateTo(180f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                }
                                Tab.STOPWATCH -> {
                                    iconRotation.animateTo(-15f, tween(100))
                                    iconRotation.animateTo(15f, tween(100))
                                    iconRotation.animateTo(0f, tween(100))
                                }
                            }
                        } else {
                            iconRotation.animateTo(0f, tween(300))
                        }
                    }

                    // Fluid Motion: Spongy Tap Scale
                    val tabScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.85f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                        label = "tabScale"
                    )

                    // Fluid Motion: Smooth Color Blend for Selected Text/Icon
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
                        label = "contentColorBlend"
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
                                    Tab.ALARMS -> AlarmFilledCustom
                                    Tab.WORLD -> WorldFilledCustom
                                    Tab.TIMERS -> TimerFilledCustom
                                    Tab.STOPWATCH -> StopwatchFilledCustom
                                }
                            }
                            
                            val inactiveIcon = remember(destination) {
                                when (destination) {
                                    Tab.ALARMS -> AlarmOutlinedCustom
                                    Tab.WORLD -> WorldOutlinedCustom
                                    Tab.TIMERS -> TimerOutlinedCustom
                                    Tab.STOPWATCH -> StopwatchOutlinedCustom
                                }
                            }
                            
                            val icon = if (isSelected) activeIcon else inactiveIcon

                            Icon(
                                imageVector = icon,
                                contentDescription = destination.label,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        rotationZ = iconRotation.value
                                    },
                                tint = contentColor
                            )

                            // Fluid Motion: Bouncy Text Expansion
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                                ) + fadeIn(animationSpec = tween(150)),
                                exit = shrinkHorizontally(
                                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                                ) + fadeOut(animationSpec = tween(150)),
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
