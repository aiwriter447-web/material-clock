package app.materialclock.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

// --- pure Kotlin code vectors for solid Alarm and World icons ---

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
            moveTo(22f, 5.72f)
            lineTo(17.4f, 1.86f)
            lineTo(16.11f, 3.39f)
            lineTo(20.71f, 7.25f)
            close()
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineTo(3.29f, 7.25f)
            close()
            moveTo(12.5f, 8f)
            horizontalLineTo(11f)
            verticalLineTo(14f)
            lineTo(15.25f, 16.55f)
            lineTo(16f, 15.32f)
            lineTo(12.5f, 13.25f)
            close()
            moveTo(12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.03f, 22f, 12f, 22f)
            curveTo(16.97f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 8.03f, 16.97f, 4f, 12f, 4f)
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

// --- main components ---

@Composable
fun ClockDock(
    destinations: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = if (dark) {
                FloatingToolbarDefaults.standardFloatingToolbarColors(
                    toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FloatingToolbarDefaults.vibrantFloatingToolbarColors()
            },
            expandedShadowElevation = 6.dp,
            collapsedShadowElevation = 6.dp,
            modifier = Modifier.selectableGroup().height(DOCK_HEIGHT),
        ) {
            destinations.forEach { d ->
                DockItem(tab = d, selected = d == selected, dark = dark, onClick = { onSelect(d) })
            }
        }
    }
}

@Composable
private fun DockItem(tab: Tab, selected: Boolean, dark: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    val targetContainer = when {
        !selected -> Color.Transparent
        dark -> scheme.primary
        else -> scheme.surfaceContainer
    }
    val targetContent = when {
        !selected -> scheme.onSurfaceVariant
        dark -> scheme.onPrimary
        else -> scheme.onSurface
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
        label = "contentColor"
    )

    // Animated padding prevents sudden layout jumps that cause the earthquake glitch
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 14.dp,
        animationSpec = tween(250),
        label = "paddingAnim"
    )

    val activeIcon = remember(tab) {
        when (tab) {
            Tab.ALARMS -> AlarmFilledIcon
            Tab.WORLD -> WorldFilledIcon
            Tab.TIMERS -> Icons.Rounded.HourglassBottom
            Tab.STOPWATCH -> Icons.Rounded.Timer
        }
    }
    val currentIcon = if (selected) activeIcon else tab.icon

    Row(
        modifier = Modifier
            .height(ITEM_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .defaultMinSize(minWidth = ITEM_HEIGHT)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AnimatedContent(
            targetState = currentIcon,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)))
                    .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)))
            },
            label = "iconAnim"
        ) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = if (selected) null else tab.label,
                tint = contentColor,
                modifier = Modifier.size(ICON_SIZE),
            )
        }

        // Switched from spring to tween to remove the bouncy push-pull earthquake effect
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(250)) + expandHorizontally(
                animationSpec = tween(durationMillis = 250),
                clip = false
            ),
            exit = fadeOut(tween(200)) + shrinkHorizontally(
                animationSpec = tween(durationMillis = 200),
                clip = false
            ),
        ) {
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
fun FloatingAddButton(visible: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

    if (grow <= 0.001f) return

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(lerp(FAB_SIZE / 2, FAB_CORNER, grow)),
        modifier = modifier
            .size(FAB_SIZE)
            .graphicsLayer { scaleX = grow; scaleY = grow },
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
private val ITEM_HEIGHT = 56.dp
private val ICON_SIZE = 25.dp

private val FAB_SIZE = DOCK_HEIGHT
private val FAB_CORNER = 24.dp
