package app.materialclock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.sheets.systemFirstDay
import app.materialclock.ui.theme.CapText
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import app.materialclock.ui.theme.StretchedCaps
import java.time.DayOfWeek

/**
 * Alarm list.
 *
 * Layout:
 * - "Alarm Groups" at the top: a wrapped row of summary cards, two per row, one per group — its
 *   name, a one-tap switch that arms or disarms every alarm inside it, and how many of its alarms
 *   are set versus currently armed. No card for an empty group; per [AlarmGroup]'s own doc, that
 *   is not a case to special-case, only a card to skip drawing.
 * - Every alarm below, in one flat list — not sectioned under its group's card. The group cards
 *   are the dashboard; this is still the same list of every alarm there is, grouped or not,
 *   exactly as it read before groups existed.
 * - One full-width alarm card per row: optional label above the time, large clock-face numerals,
 *   repeat days over the switch on the right.
 */
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    groups: List<AlarmGroup>,
    weekStart: WeekStart,
    onToggle: (Long) -> Unit,
    onToggleGroup: (Long, Boolean) -> Unit,
    onEdit: (Alarm) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val order = remember(weekStart) {
        weekStart.order(systemFirstDay())
    }

    val context = LocalContext.current

    val is24Hour = remember(context) {
        android.text.format.DateFormat.is24HourFormat(context)
    }

    val byGroup = remember(alarms) { alarms.groupBy { it.groupId } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (groups.isNotEmpty()) {
            item(key = "group-cards") {
                GroupCardsSection(
                    groups = groups,
                    byGroup = byGroup,
                    onToggleGroup = onToggleGroup,
                )
            }
        }

        items(alarms, key = { it.id }) { alarm ->
            AlarmRow(
                alarm = alarm,
                order = order,
                is24Hour = is24Hour,
                onToggle = {
                    onToggle(alarm.id)
                },
                onEdit = {
                    onEdit(alarm)
                },
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Group cards                                                                */
/* -------------------------------------------------------------------------- */

private val GROUP_SECTION_PADDING_H = 4.dp
private val GROUP_CARD_GAP = 10.dp
private val GROUP_CARD_PADDING = 16.dp
private val GROUP_CARD_CORNER = 22.dp

/** The "Alarm Groups" dashboard: one summary card per group, two to a row, wrapping as needed. */
@Composable
private fun GroupCardsSection(
    groups: List<AlarmGroup>,
    byGroup: Map<Long?, List<Alarm>>,
    onToggleGroup: (Long, Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = GROUP_SECTION_PADDING_H),
        verticalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
    ) {
        Text(
            "Alarm Groups",
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
        )
        groups.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
            ) {
                pair.forEach { group ->
                    val groupAlarms = byGroup[group.id].orEmpty()
                    GroupCard(
                        group = group,
                        total = groupAlarms.size,
                        armed = groupAlarms.count { it.enabled },
                        // Not "any armed": a mixed group reads as off until every alarm in it
                        // agrees, which is the least surprising state for a switch that is about
                        // to make them all match each other. An empty group is always off — there
                        // is nothing in it for the switch to mean "on".
                        checked = groupAlarms.isNotEmpty() && groupAlarms.all { it.enabled },
                        onToggle = { onToggleGroup(group.id, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // An odd group out still gets a half-width card, matching every other row.
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: AlarmGroup,
    total: Int,
    armed: Int,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (checked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val ink = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(GROUP_CARD_CORNER),
        modifier = modifier,
    ) {
        Column(Modifier.padding(GROUP_CARD_PADDING)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = checked, onCheckedChange = onToggle)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GroupCount(icon = Icons.Outlined.Alarm, count = total, ink = ink)
                Spacer(Modifier.width(16.dp))
                GroupCount(icon = Icons.Filled.Alarm, count = armed, ink = ink)
            }
        }
    }
}

/** [total] alarms in the group beside [Icons.Outlined.Alarm]; how many are currently armed beside
 * the filled glyph — an outline for "exists" and a filled one for "is doing something" reads the
 * same way it does everywhere else a bell icon draws a ringer's on/off state. */
@Composable
private fun GroupCount(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, ink: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ink.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(count.toString(), style = MaterialTheme.typography.labelLarge, color = ink)
    }
}

/* -------------------------------------------------------------------------- */
/* Row dimensions                                                             */
/* -------------------------------------------------------------------------- */

/**
 * Horizontal inset of the card content.
 *
 * The screenshots have a relatively compact inset, leaving the clock face
 * dominant inside the card.
 */
private val ROW_HORIZONTAL_PADDING = 16.dp

/** Vertical inset of the card content. */
private val ROW_VERTICAL_PADDING = 16.dp

private val ROW_CORNER_RADIUS = 24.dp

/** Gap between an alarm label such as "Morning" and its time. */
private val ROW_LABEL_TO_TIME = 4.dp

/** Gap between the large hour and the minutes block in 12-hour mode. */
private val ROW_TIME_GAP = 8.dp

/**
 * Main time cap height.
 *
 * This is deliberately much larger than the old 40.dp value. The supplied
 * reference screenshots use the time as the dominant visual element.
 */
private val ROW_TIME_CAP = 120.dp

/** Relative size of minutes compared with the large hour. */
private const val ROW_MINUTE_CAP_FRACTION = 0.66f

/** Relative size of AM/PM compared with the large hour. */
private const val ROW_MERIDIEM_CAP_FRACTION = 0.30f

/* -------------------------------------------------------------------------- */
/* Alarm row                                                                  */
/* -------------------------------------------------------------------------- */

@Composable
private fun AlarmRow(
    alarm: Alarm,
    order: List<DayOfWeek>,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    val enabled = alarm.enabled

    // Dynamically change colors based on whether the alarm is enabled. Matches the colorized UI
    // design when active, and muted grey when inactive.
    val container = if (enabled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val ink = if (enabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val numeralWeight = if (enabled) {
        ClockFace.WEIGHT_ON
    } else {
        ClockFace.WEIGHT_OFF
    }

    val hour12 = (alarm.time.hour % 12).takeIf { it != 0 } ?: 12

    val meridiem = if (alarm.time.hour < 12) {
        "AM"
    } else {
        "PM"
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ROW_HORIZONTAL_PADDING,
                    vertical = ROW_VERTICAL_PADDING,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // LEFT SIDE — optional label above the time, in either the 24-hour or 12-hour shape.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = ink,
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(ROW_LABEL_TO_TIME))
                }

                if (is24Hour) {
                    RowTime24(
                        hour = alarm.time.hour,
                        minute = alarm.time.minute,
                        ink = ink,
                        weight = numeralWeight,
                    )
                } else {
                    RowTime12(
                        hour12 = hour12,
                        minute = alarm.time.minute,
                        meridiem = meridiem,
                        ink = ink,
                        weight = numeralWeight,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT SIDE — day letters over the switch.
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
            ) {
                DayLetters(
                    alarm = alarm,
                    order = order,
                    ink = ink,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Material 3 Switch is intentionally kept here rather than replacing it with a
                // custom control, so its appearance continues to follow the app's Material theme.
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        onToggle()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = container,
                        checkedTrackColor = ink,
                        checkedBorderColor = Color.Transparent,

                        uncheckedThumbColor = ink,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = ink,

                        disabledCheckedThumbColor = container,
                        disabledCheckedTrackColor = ink,
                        disabledUncheckedThumbColor = ink,
                        disabledUncheckedTrackColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 24-hour time                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun RowTime24(
    hour: Int,
    minute: Int,
    ink: Color,
    weight: Int,
) {
    Numerals(
        text = "%02d:%02d".format(hour, minute),
        capHeight = ROW_TIME_CAP,
        color = ink,
        width = ClockFace.CONDENSED,
        weight = weight,
        tracking = ClockFace.CONDENSED_TRACKING,
    )
}

/* -------------------------------------------------------------------------- */
/* 12-hour time                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun RowTime12(
    hour12: Int,
    minute: Int,
    meridiem: String,
    ink: Color,
    weight: Int,
) {
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        // Large hour: 07
        Numerals(
            text = "%02d".format(hour12),
            capHeight = ROW_TIME_CAP,
            color = ink,
            width = ClockFace.CONDENSED,
            weight = weight,
            tracking = ClockFace.CONDENSED_TRACKING,
        )

        Spacer(modifier = Modifier.width(ROW_TIME_GAP))

        Column {
            // Smaller minutes: 15
            Numerals(
                text = "%02d".format(minute),
                capHeight = ROW_TIME_CAP * ROW_MINUTE_CAP_FRACTION,
                color = ink,
                width = ClockFace.CONDENSED,
                weight = weight,
                tracking = ClockFace.CONDENSED_TRACKING,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // AM / PM below minutes.
            CapText(
                text = meridiem,
                capHeight = ROW_TIME_CAP * ROW_MERIDIEM_CAP_FRACTION,
                color = ink,
                tracking = ClockFace.CONDENSED_TRACKING,
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Repeat-day letters                                                         */
/* -------------------------------------------------------------------------- */

private val DAY_CAP = 23.dp
private val DAY_BLOCK_WIDTH = 91.dp
private val DAY_TRACKING = 2.2.dp

private const val DAY_WEIGHT_ON = 700
private const val DAY_WEIGHT_OFF = 400

/**
 * Repeat-day letters.
 *
 * All seven letters remain in one AnnotatedString so Compose's text shaper sees them as one run
 * instead of seven independently positioned boxes. The final run is horizontally scaled to a fixed
 * width so the switch below does not move when the repeat pattern changes.
 */
@Composable
private fun DayLetters(
    alarm: Alarm,
    order: List<DayOfWeek>,
    ink: Color,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val bold = remember {
        ClockFace.capitals(
            DAY_CAP,
            ClockFace.CONDENSED,
            weight = DAY_WEIGHT_ON,
        )
    }

    val light = remember {
        ClockFace.capitals(
            DAY_CAP,
            ClockFace.CONDENSED,
            weight = DAY_WEIGHT_OFF,
        )
    }

    val text = remember(
        alarm.days,
        alarm.isOneShot,
        order,
        ink,
    ) {
        buildAnnotatedString {
            order.forEach { day ->
                val active = !alarm.isOneShot && day in alarm.days

                withStyle(
                    SpanStyle(
                        fontFamily = if (active) {
                            bold.fontFamily
                        } else {
                            light.fontFamily
                        },

                        color = if (active) {
                            ink
                        } else {
                            ink.copy(alpha = 0.30f)
                        },
                    )
                ) {
                    append(day.name.take(1))
                }
            }
        }
    }

    // Tracking is expressed in em so it scales with the cap height.
    val letterSpacing = with(density) {
        (DAY_TRACKING / bold.fontSize.toDp()).em
    }

    val style = bold.copy(
        letterSpacing = letterSpacing,
    )

    // Measure the actual seven-letter run. This avoids hard-coding a separate width for each
    // possible repeat pattern. The target block remains fixed.
    val measuredWidth = measurer
        .measure(
            text = text,
            style = style,
        )
        .size
        .width

    val targetWidth = with(density) {
        DAY_BLOCK_WIDTH.toPx()
    }

    val scaleX = if (measuredWidth > 0) {
        targetWidth / measuredWidth
    } else {
        1f
    }

    StretchedCaps(
        text = text,
        style = style,
        capHeight = DAY_CAP,
        scaleX = scaleX,
    )
}
