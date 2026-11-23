package app.materialclock.widget

import android.text.format.DateFormat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import android.widget.RemoteViews
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.WorldCity
import app.materialclock.data.ClockSettings
import app.materialclock.data.ColourSource
import app.materialclock.data.WidgetAppearance
import app.materialclock.data.WidgetBackground
import app.materialclock.data.WidgetConfig
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

enum class WidgetOptionsKind { WORLD, ALARM }

/**
 * Settings for a World Clock or Alarm widget.
 *
 * The top part is what makes it that widget (which cities, or which alarm); the rest (background
 * and colours) is the same for both and works like the Digital clock's.
 */
@Composable
fun WidgetOptionsScreen(
    kind: WidgetOptionsKind,
    initial: WidgetConfig,
    settings: ClockSettings,
    cities: List<WorldCity>,
    alarms: List<Alarm>,
    isNew: Boolean,
    onCancel: () -> Unit,
    onSave: (WidgetConfig) -> Unit,
) {
    var config by remember(initial) { mutableStateOf(initial) }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Text(
                if (kind == WidgetOptionsKind.WORLD) "World clock widget" else "Alarm widget",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            )

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                LivePreview(kind, config, settings, cities, alarms)

                when (kind) {
                    WidgetOptionsKind.WORLD -> CityChoices(config, settings, cities) { config = it }
                    WidgetOptionsKind.ALARM -> AlarmChoices(config, alarms) { config = it }
                }

                OpacitySlider(
                    "Background opacity",
                    if (config.background == WidgetBackground.NONE) 0f else config.backgroundOpacity,
                ) {
                    config = config.copy(
                        backgroundOpacity = it,
                        background = if (it <= 0.01f) WidgetBackground.NONE else WidgetBackground.FILLED,
                    )
                }
                OptionGroup("Light or dark", config.appearance, WidgetAppearance.entries, { it.label }) {
                    config = config.copy(appearance = it)
                }
                OptionGroup("Colour", config.colour, ColourSource.entries, { it.label }) {
                    config = config.copy(colour = it)
                }
                if (config.colour == ColourSource.PALETTE) {
                    PaletteCarousel(config.palette) { config = config.copy(palette = it) }
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Cancel") }
                Button(
                    onClick = { onSave(config) },
                    modifier = Modifier.weight(1.6f).height(56.dp),
                ) { Text(if (isNew) "Add widget" else "Save") }
            }
        }
    }
}

/**
 * The widget, live, as it will look: the same drawing code the home screen uses, shown at two
 * sizes (wide and square) on a plain backdrop, so a transparent background can be judged too.
 * It redraws whenever anything on the screen changes.
 */
@Composable
private fun LivePreview(
    kind: WidgetOptionsKind,
    config: WidgetConfig,
    settings: ClockSettings,
    cities: List<WorldCity>,
    alarms: List<Alarm>,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val wideW = 300.dp
    val wideH = 110.dp
    val squareSide = 150.dp

    val wide = remember(kind, config, settings, cities, alarms) {
        previewViews(context, kind, config, settings, cities, alarms, with(density) { wideW.roundToPx() }, with(density) { wideH.roundToPx() })
    }
    val square = remember(kind, config, settings, cities, alarms) {
        previewViews(context, kind, config, settings, cities, alarms, with(density) { squareSide.roundToPx() }, with(density) { squareSide.roundToPx() })
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0B6BC0), Color(0xFF45A9EC))))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PreviewHost(wide, wideW, wideH)
        PreviewHost(square, squareSide, squareSide)
    }
}

private fun previewViews(
    context: android.content.Context,
    kind: WidgetOptionsKind,
    config: WidgetConfig,
    settings: ClockSettings,
    cities: List<WorldCity>,
    alarms: List<Alarm>,
    w: Int,
    h: Int,
): RemoteViews = when (kind) {
    WidgetOptionsKind.WORLD -> WorldClockRenderer.previewViews(context, settings, config, cities, w, h)
    WidgetOptionsKind.ALARM -> AlarmWidgetRenderer.previewViews(
        context, settings, config, AlarmWidgetRenderer.previewAlarm(alarms, config), w, h,
    )
}

/** Hosts a [RemoteViews] in the app, at exactly [width] by [height]. */
@Composable
private fun PreviewHost(views: RemoteViews, width: Dp, height: Dp) {
    AndroidView(
        modifier = Modifier.size(width, height),
        factory = { FrameLayout(it) },
        update = { frame ->
            frame.removeAllViews()
            runCatching { frame.addView(views.apply(frame.context, frame)) }
        },
    )
}

@Composable
private fun SectionTitle(text: String, hint: String? = null) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(text, style = MaterialTheme.typography.titleMedium)
        if (hint != null) {
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** One tappable row in a list of choices. [badge] is a small label on the right, e.g. "1". */
@Composable
private fun ChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (badge != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

/** World clock: tap two cities. Home counts as a choice too. */
@Composable
private fun CityChoices(
    config: WidgetConfig,
    settings: ClockSettings,
    cities: List<WorldCity>,
    onChange: (WidgetConfig) -> Unit,
) {
    val homeId = settings.world.homeZoneOverride ?: java.time.ZoneId.systemDefault().id
    val selected = config.cityKeys

    SectionTitle(
        "Cities",
        "Tap two. The first goes on the left, the second on the right. " +
            "If you pick none, the first two cities in the app are used.",
    )

    fun toggle(key: String) {
        val next = if (key in selected) selected - key else (selected + key).takeLast(2)
        onChange(config.copy(cityKeys = next))
    }

    ChoiceRow(
        title = "Home",
        subtitle = homeId,
        selected = HOME_CITY_KEY in selected,
        badge = selected.indexOf(HOME_CITY_KEY).takeIf { it >= 0 }?.let { "${it + 1}" },
    ) { toggle(HOME_CITY_KEY) }

    if (cities.isEmpty()) {
        Text(
            "No cities yet. Add some in the World Clock tab and they will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
    cities.forEach { city ->
        val key = worldCityKey(city)
        ChoiceRow(
            title = city.city,
            subtitle = listOf(city.region, city.country).filter { it.isNotBlank() }.joinToString(", "),
            selected = key in selected,
            badge = selected.indexOf(key).takeIf { it >= 0 }?.let { "${it + 1}" },
        ) { toggle(key) }
    }
    Spacer(Modifier.height(8.dp))
}

/** Alarm: pick one alarm, or leave it on "Next alarm". */
@Composable
private fun AlarmChoices(
    config: WidgetConfig,
    alarms: List<Alarm>,
    onChange: (WidgetConfig) -> Unit,
) {
    val context = LocalContext.current
    val timeFormat = if (DateFormat.is24HourFormat(context)) {
        DateTimeFormatter.ofPattern("HH:mm")
    } else {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }

    SectionTitle("Alarm", "Which alarm this widget shows and switches.")

    ChoiceRow(
        title = "Next alarm",
        subtitle = "Always shows whichever alarm rings next",
        selected = config.alarmId == null,
    ) { onChange(config.copy(alarmId = null)) }

    if (alarms.isEmpty()) {
        Text(
            "No alarms yet. Add one in the Alarm tab and it will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
    alarms.sortedBy { it.time }.forEach { alarm ->
        val days = if (alarm.days.isEmpty()) {
            "Once"
        } else {
            alarm.days.sorted().joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
        }
        val details = listOf(alarm.label, days, if (alarm.enabled) "" else "Off")
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        ChoiceRow(
            title = alarm.time.format(timeFormat),
            subtitle = details,
            selected = config.alarmId == alarm.id,
        ) { onChange(config.copy(alarmId = alarm.id)) }
    }
    Spacer(Modifier.height(8.dp))
}
