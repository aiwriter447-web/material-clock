package app.materialclock.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

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

    // Very fast and responsive color fade animation.
    val containerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "contentColor"
    )

    val activeIcon = remember(tab) {
        when (tab) {
            Tab.ALARMS -> Icons.Rounded.Alarm
            Tab.WORLD -> Icons.Rounded.Public
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
            .padding(horizontal = if (selected) 16.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Parallel pop animation for the icon (Fast & Snappy).
        AnimatedContent(
            targetState = currentIcon,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f)))
                    .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.6f, animationSpec = tween(150)))
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
        
        // Smooth spring effect with a balanced bounce.
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(150)) + expandHorizontally(
                animationSpec = spring(
                    dampingRatio = 0.7f, 
                    stiffness = 500f
                ),
                clip = false
            ),
            exit = fadeOut(tween(100)) + shrinkHorizontally(
                animationSpec = spring(
                    dampingRatio = 0.8f, 
                    stiffness = 600f
                ),
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
        animationSpec = tween(durationMillis = 150),
        label = "addGlyph",
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -90f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
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