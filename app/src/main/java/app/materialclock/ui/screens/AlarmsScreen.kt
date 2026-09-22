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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.materialclock.core.Alarm
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.sheets.systemFirstDay
import app.materialclock.ui.theme.CapText
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import app.materialclock.ui.theme.StretchedCaps
import java.time.DayOfWeek

/**
 * The alarm list.
 *
 * One row per alarm, full width, rather than the two-column grid this screen used to be. A row is
 * what leaves room for the alarm's own [Alarm.label] above the time — "Morning", "Gym" — which a
 * 184 dp-wide tile never had the width for, and it is what a repeat visitor coming from any other
 * clock app already expects an alarm list to look like.
 *
 * ## Two time layouts, not one
 *
 * [android.text.format.DateFormat.is24HourFormat] decides which of [RowTime24] or [RowTime12] a row
 * draws. A 24-hour clock has no meridiem to place, so its time is one continuous run — `07:15` — set
 * with [Numerals] exactly the way [app.materialclock.alarm.AlarmRingActivity]'s own ringing screen
 * already sets a 24-hour time. A 12-hour clock keeps the grid's old stacked shape, hour over nothing
 * against minutes-over-meridiem, because that is what carries the AM/PM without a third line.
 */
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    weekStart: WeekStart,
    onToggle: (Long) -> Unit,
    onEdit: (Alarm) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // The setting reaches all the way into the row, not only into the editor. An app that starts
    // the week on Monday in the picker and on Sunday in the row is worse than one with no setting.
    val order = remember(weekStart) { weekStart.order(systemFirstDay()) }
    val context = LocalContext.current
    val is24Hour = remember(context) { android.text.format.DateFormat.is24HourFormat(context) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(alarms, key = { it.id }) { alarm ->
            AlarmRow(
                alarm = alarm,
                order = order,
                is24Hour = is24Hour,
                onToggle = { onToggle(alarm.id) },
                onEdit = { onEdit(alarm) },
            )
        }
    }
}

/** How far the row's own content sits from the card's edges. */
private val ROW_PADDING_H = 20.dp
private val ROW_PADDING_V = 18.dp

@Composable
private fun AlarmRow(
    alarm: Alarm,
    order: List<DayOfWeek>,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    val armed = alarm.enabled
    val container =
        if (armed) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    val ink =
        if (armed) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant
    val weight = if (armed) ClockFace.WEIGHT_ON else ClockFace.WEIGHT_OFF

    val hour12 = (alarm.time.hour % 12).takeIf { it != 0 } ?: 12
    val meridiem = if (alarm.time.hour < 12) "AM" else "PM"

    Surface(
        color = container,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            // One announcement for the whole row. Otherwise a screen reader finds a bare Switch
            // beside a time it reads out digit by digit, and seven unlabelled day letters.
            .clearAndSetSemantics {
                contentDescription = buildString {
                    if (alarm.label.isNotBlank()) append("${alarm.label}, ")
                    append(
                        if (is24Hour) "%02d:%02d".format(alarm.time.hour, alarm.time.minute)
                        else "%d:%02d %s".format(hour12, alarm.time.minute, meridiem)
                    )
                    append(", ${alarm.repeatLabel()}")
                    append(if (armed) ", on" else ", off")
                    append(", double tap to edit")
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = ink.copy(alpha = if (armed) 0.85f else 0.7f),
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (is24Hour) {
                    RowTime24(alarm.time.hour, alarm.time.minute, ink, weight)
                } else {
                    RowTime12(hour12, alarm.time.minute, meridiem, ink, weight)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                DayLetters(alarm, order, ink)
                Spacer(Modifier.height(8.dp))
                Switch(
                    checked = armed,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = container,
                        checkedTrackColor = ink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = ink,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = ink,
                    ),
                )
            }
        }
    }
}

/**
 * The cap height a row's time is set at. Fixed, unlike the grid's — a row's own width is not the
 * numeral's constraint the way a 184 dp tile's was, so there is nothing here to solve for.
 */
private val ROW_HOUR_CAP = 40.dp

/**
 * `07:15`, one run, the same way [app.materialclock.alarm.AlarmRingActivity]'s ringing screen
 * already sets a 24-hour time. There is no meridiem to place, so there is nothing to stack.
 */
@Composable
private fun RowTime24(hour: Int, minute: Int, ink: Color, weight: Int) {
    Numerals(
        text = "%02d:%02d".format(hour, minute),
        capHeight = ROW_HOUR_CAP,
        color = ink,
        width = ClockFace.CONDENSED,
        weight = weight,
        tracking = ClockFace.CONDENSED_TRACKING,
    )
}

/** Measured against [ROW_HOUR_CAP] the same way the old grid tile measured against its own hour cap. */
private const val ROW_MINUTE_CAP_FRACTION = 0.66f
private const val ROW_MERIDIEM_CAP_FRACTION = 0.30f
private val ROW_HOUR_MINUTE_GAP = 6.dp

/**
 * Hour on its own baseline, minutes over meridiem beside it — the old grid tile's shape, carried
 * over because a 12-hour time needs somewhere to put AM/PM and this is the shape that already does
 * it without a third line under the hour.
 */
@Composable
private fun RowTime12(hour12: Int, minute: Int, meridiem: String, ink: Color, weight: Int) {
    Row(verticalAlignment = Alignment.Top) {
        Numerals(
            text = "%02d".format(hour12),
            capHeight = ROW_HOUR_CAP,
            color = ink,
            width = ClockFace.CONDENSED,
            weight = weight,
            tracking = ClockFace.CONDENSED_TRACKING,
        )
        Spacer(Modifier.width(ROW_HOUR_MINUTE_GAP))
        Column {
            Numerals(
                text = "%02d".format(minute),
                capHeight = ROW_HOUR_CAP * ROW_MINUTE_CAP_FRACTION,
                color = ink,
                width = ClockFace.CONDENSED,
                weight = weight,
                tracking = ClockFace.CONDENSED_TRACKING,
            )
            Spacer(Modifier.height(2.dp))
            CapText(
                text = meridiem,
                capHeight = ROW_HOUR_CAP * ROW_MERIDIEM_CAP_FRACTION,
                color = ink,
                tracking = ClockFace.CONDENSED_TRACKING,
            )
        }
    }
}

/** A little shorter than the switch's 32 dp track, because level with it reads as shouting. */
private const val DAY_CAP_DP = 23f
/** Fixed, so the block is one width whatever the repeat pattern and the switch never shifts. */
private const val DAY_BLOCK_DP = 91f
/** The constant slice of space between letters. The scale is derived from it, not tuned. */
private const val DAY_TRACKING_DP = 2.2f

/**
 * The repeat days, on their own line above the switch.
 *
 * ## One text run, so the spacing is the font's
 *
 * Two earlier attempts gave each letter its own fixed-width box: first every box the same, then
 * each box sized to its own advance. Both are the same mistake in different clothes. A box
 * boundary is a wall the shaper cannot see across, so the side bearings the type designer drew get
 * overridden by an invented cell width and no kerning pair can apply. Equal boxes put 6.9 dp beside
 * `F` and 2.3 dp beside `T`; advance-sized boxes still left slack around every inactive letter,
 * because a light glyph is narrower than the bold cell it was centred in.
 *
 * So all seven are **one** [androidx.compose.ui.text.AnnotatedString], each letter a span carrying
 * its own weight and colour. The text engine then spaces them exactly as the font says to, which is
 * the only definition of "even" that the eye agrees with.
 *
 * ## And the block still cannot change width
 *
 * The switch sits under this and must land in the same place on every row, so the run is squeezed
 * to [DAY_BLOCK_DP] by a scale *derived* from its own measured width rather than tuned.
 *
 * [DAY_TRACKING_DP] is inside that squeeze, so tightening it hands the space straight to the
 * letters: block width fixed, tracking down, glyphs wider.
 */
@Composable
private fun DayLetters(alarm: Alarm, order: List<DayOfWeek>, ink: Color) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val bold = ClockFace.capitals(DAY_CAP_DP.dp, ClockFace.CONDENSED, weight = DAY_WEIGHT_ON)
    val light = ClockFace.capitals(DAY_CAP_DP.dp, ClockFace.CONDENSED, weight = DAY_WEIGHT_OFF)

    val text = remember(alarm.days, alarm.isOneShot, order, ink, bold, light) {
        buildAnnotatedString {
            order.forEach { day ->
                val on = !alarm.isOneShot && day in alarm.days
                withStyle(
                    SpanStyle(
                        // Weight is the state here too, matching the numerals above.
                        fontFamily = (if (on) bold else light).fontFamily,
                        color = if (on) ink else ink.copy(alpha = 0.30f),
                    )
                ) {
                    append(day.name.take(1))
                }
            }
        }
    }

    // Tracking in em, so it rides the type size the way the rest of the face's tracking does.
    val style = bold.copy(
        letterSpacing = with(density) { (DAY_TRACKING_DP.dp / bold.fontSize.toDp()).em },
    )
    val scaleX = with(density) {
        DAY_BLOCK_DP.dp.toPx() / measurer.measure(text, style).size.width
    }

    StretchedCaps(text = text, style = style, capHeight = DAY_CAP_DP.dp, scaleX = scaleX)
}

/** Weight is the state, the same way it is on the numerals above. Not a second colour ramp. */
private const val DAY_WEIGHT_ON = 700
private const val DAY_WEIGHT_OFF = 400
