package app.materialclock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.roundToInt

/**
 * The app's primary navigation: a floating pill with a detached add button, and no navigation bar.
 *
 * Same pattern as Notes' dock, and the same deliberate off-spec choice. The corpus sanctions it in
 * exactly one line (*"Floating toolbars can be used as tabs between related subsequent pages"*),
 * and it is what Google Photos and Google Chat ship. What it buys is the bottom edge: the alarm
 * grid and the world-clock list run full-bleed underneath instead of stopping at a 64 dp bar.
 *
 * **What it costs, and what this file pays.** `FloatingToolbar.kt` contains no `Role.*` and no
 * `selectableGroup()`. A screen reader would announce four unlabelled buttons. The contract that
 * `ShortNavigationBar` gave away free is therefore supplied by hand: the row is a
 * `selectableGroup()`, every destination is `selectable` with `Role.Tab`, and each carries a text
 * label rather than leaning on the icon. If this regresses, it regresses silently.
 *
 * ## Centring
 *
 * The **group** is centred, not the pill. Timers and Stopwatch have nothing to add, so when the
 * add button leaves, the row narrows and the pill glides right into the middle of the screen on
 * its own, with no second layout to maintain. That works only because [PlusButton] animates its
 * *reported width*, not just its drawing; a button that merely faded would leave the pill sitting
 * off-centre beside a hole.
 */
@Composable
fun ClockDock(
    destinations: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Read off the *resolved* scheme rather than `isSystemInDarkTheme()`: the app has its own
    // light/dark/system setting and an AMOLED variant on top of it, and only the scheme knows
    // which of those actually won.
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // [M3E-NEW] HorizontalFloatingToolbar. Material ships **two** colour sets for it and they
        // are not interchangeable between the two modes:
        //
        //  - `vibrant` is `PrimaryContainer` (`FloatingToolbarTokens.VibrantContainerColor`). In
        //    light that is a pale tinted pill that lifts cleanly off a white page.
        //  - `standard` is `SurfaceContainer` (`StandardContainerColor`), the elevated-surface
        //    role, which is what a floating thing over a dark page is supposed to be.
        //
        // In dark mode `PrimaryContainer` resolves to a mid slate: too dark to read as lit, too
        // light to read as glass, and muddy beside the accent-filled tiles it floats over. So the
        // mode picks the set, rather than one set being used for both.
        HorizontalFloatingToolbar(
            expanded = true,
            colors = if (dark) {
                // `SurfaceContainer` is the token, but it is only two tones above the page and this
                // app's dark page is near-black, so the pill disappeared into it. Since this is the
                // highest thing on the screen, at 6 dp, the role that matches is
                // `surfaceContainerHighest`, passed through the API's own override rather than
                // mixed by hand.
                FloatingToolbarDefaults.standardFloatingToolbarColors(
                    toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FloatingToolbarDefaults.vibrantFloatingToolbarColors()
            },
            // The no-FAB overload defaults both elevations to Level0, and a flat vibrant pill over
            // a wall of coral tiles has nothing separating it from what it floats above.
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
    // The selected chip is a *solid* container, not a wash of the content colour over the pill.
    // In each mode exactly one of the pill and the chip carries the accent, never both.
    //
    // Light follows the token pair outright: `VibrantButtonSelectedContainerColor` is
    // `SurfaceContainer` and `VibrantButtonSelectedIconColor` is `OnSurface`, so a quiet chip is
    // punched out of a tinted bar. Dark inverts that: the bar is neutral, so the chip is `primary`.
    //
    // Not `secondaryContainer`, which is what a navigation active indicator normally uses. On a
    // *dynamic* scheme in the expressive style the secondary family is rotated a long way off the
    // seed, and on a real wallpaper that put a navy chip inside a yellow-green app. `primary` is
    // the one accent role guaranteed to be the colour the rest of the screen is already using.
    val scheme = MaterialTheme.colorScheme
    val container = when {
        !selected -> Color.Transparent
        dark -> scheme.primary
        else -> scheme.surfaceContainer
    }
    val content = when {
        !selected -> LocalContentColor.current
        dark -> scheme.onPrimary
        else -> scheme.onSurface
    }
    Row(
        modifier = Modifier
            .height(ITEM_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            // Role.Tab and the selected state are supplied here because the component supplies
            // neither. Without these three lines the whole pattern is inaccessible.
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .defaultMinSize(minWidth = ITEM_HEIGHT)
            .padding(horizontal = if (selected) 16.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            // The label below is the accessible name when it is shown; when it is not, the icon
            // has to carry it.
            contentDescription = if (selected) null else tab.label,
            tint = content,
            modifier = Modifier.size(ICON_SIZE),
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(clip = false),
            exit = fadeOut() + shrinkHorizontally(clip = false),
        ) {
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

/**
 * The add button, now detached from the dock and floated at the top-right of the screen instead.
 *
 * It used to sit inline beside the nav pill at the bottom, sliding the pill toward centre as it
 * grew or shrank (see git history for that version). Moved because that position sat directly over
 * the last row of a full list — the alarm grid's last tile, the world-clock list's last card — so
 * the one control for adding something could end up partly hidden behind the very content it adds
 * to. Top-right is clear of every list's last row and still exactly where Material's own FAB
 * guidance expects a primary add action to float.
 *
 * A plain scale-and-fade rather than the old width-reporting `Layout`: there is nothing beside it
 * to make room for any more, so there is nothing for the width to choreograph.
 */
@Composable
fun FloatingAddButton(visible: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val grow by animateFloatAsState(if (visible) 1f else 0f, spec, label = "addGrow")
    val glyph by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 110),
        label = "addGlyph",
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
                .graphicsLayer { scaleX = glyph; scaleY = glyph; alpha = glyph },
        )
    }
}

/**
 * Bigger than `FloatingToolbarTokens.ContainerHeight`, which is 64 dp, and deliberately.
 *
 * 64 dp with 8 dp of leading and trailing space leaves 48 dp items at exactly the accessibility
 * *minimum*, which is the floor for a target you can hit, not a comfortable one. A floating pill is
 * also harder to hit than a docked bar: it has no screen edge behind it to catch an overshoot, so a
 * miss lands on the content underneath instead of on the nearest item. Eight more dp puts the items
 * at 56 and buys that margin back.
 *
 * The FAB matches the pill's height so the two read as one row rather than as two sizes.
 */
val DOCK_HEIGHT = 72.dp
private val ITEM_HEIGHT = 56.dp
private val ICON_SIZE = 25.dp

private val FAB_SIZE = DOCK_HEIGHT
private val FAB_CORNER = 24.dp