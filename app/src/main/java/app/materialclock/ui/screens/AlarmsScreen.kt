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
import androidx.compose.ui.text.TextUnit
import androidx.compose.ui.text.TextUnitType
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
 * Alarm list.
 *
 * One full-width alarm card per row.
 *
 * Layout:
 *
 *   Label
 *   07 15
 *      AM              S M T W T F S
 *                    [ switch ]
 *
 * The enabled state controls:
 * - label color
 * - numeral weight
 * - active repeat-day weight/color
 * - switch state
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
    val order = remember(weekStart) {
        weekStart.order(systemFirstDay())
    }

    val context = LocalContext.current

    val is24Hour = remember(context) {
        android.text.format.DateFormat.is24HourFormat(context)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = alarms,
            key = { it.id },
        ) { alarm ->
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
/* Row dimensions                                                             */
/* -------------------------------------------------------------------------- */

private val ROW_HORIZONTAL_PADDING = 32.dp
private val ROW_VERTICAL_PADDING = 16.dp

private val ROW_CORNER_RADIUS = 24.dp

private val ROW_LABEL_TO_TIME = 4.dp

private val ROW_TIME_GAP = 8.dp

/**
 * Main clock height.
 *
 * The supplied reference uses a very large clock-face treatment.
 */
private val ROW_TIME_CAP = 120.dp

private const val ROW_MINUTE_CAP_FRACTION = 0.66f
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

    /*
     * Keep the row/card background consistent with the reference.
     *
     * The ON state is communicated by the typography and switch rather than
     * changing the entire row to a different surface color.
     */
    val container = MaterialTheme.colorScheme.surfaceContainer

    /*
     * Base clock color.
     */
    val ink = MaterialTheme.colorScheme.onSurfaceVariant

    /*
     * Enabled title uses the app's primary color.
     *
     * This is the important fix for the bug where an enabled alarm's label
     * remained the same color as a disabled alarm.
     */
    val titleColor =
        if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            ink.copy(alpha = 0.70f)
        }

    /*
     * Enabled alarms use the heavy clock-face weight.
     * Disabled alarms use the lighter clock-face weight.
     */
    val numeralWeight =
        if (enabled) {
            ClockFace.WEIGHT_ON
        } else {
            ClockFace.WEIGHT_OFF
        }

    val hour12 =
        (alarm.time.hour % 12)
            .takeIf { it != 0 }
            ?: 12

    val meridiem =
        if (alarm.time.hour < 12) {
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

            /*
             * ----------------------------------------------------------------
             * LEFT
             * ----------------------------------------------------------------
             */

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {

                /*
                 * Alarm label.
                 *
                 * FIX:
                 * Enabled alarm -> primary/active color
                 * Disabled alarm -> muted color
                 */
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = titleColor,
                        maxLines = 1,
                    )

                    Spacer(
                        modifier = Modifier.height(ROW_LABEL_TO_TIME)
                    )
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

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            /*
             * ----------------------------------------------------------------
             * RIGHT
             * ----------------------------------------------------------------
             */

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
            ) {
                DayLetters(
                    alarm = alarm,
                    order = order,
                    ink = ink,
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        onToggle()
                    },
                    colors = SwitchDefaults.colors(
                        /*
                         * ON:
                         * dark/active track
                         * light thumb
                         */
                        checkedThumbColor = container,
                        checkedTrackColor = ink,
                        checkedBorderColor = Color.Transparent,

                        /*
                         * OFF:
                         * transparent inside
                         * outlined border
                         */
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
        /*
         * Large hour.
         *
         * Example:
         *
         * 07
         */
        Numerals(
            text = "%02d".format(hour12),
            capHeight = ROW_TIME_CAP,
            color = ink,
            width = ClockFace.CONDENSED,
            weight = weight,
            tracking = ClockFace.CONDENSED_TRACKING,
        )

        Spacer(
            modifier = Modifier.width(ROW_TIME_GAP)
        )

        Column {
            /*
             * Smaller minute section.
             *
             * Example:
             *
             * 15
             */
            Numerals(
                text = "%02d".format(minute),
                capHeight = ROW_TIME_CAP * ROW_MINUTE_CAP_FRACTION,
                color = ink,
                width = ClockFace.CONDENSED,
                weight = weight,
                tracking = ClockFace.CONDENSED_TRACKING,
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            /*
             * AM / PM below minutes.
             */
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

/**
 * The repeat-day block stays at a fixed width so the switch below it
 * does not move between alarms.
 */
private val DAY_BLOCK_WIDTH = 91.dp

/**
 * Desired physical tracking.
 */
private val DAY_TRACKING = 2.2.dp

private const val DAY_WEIGHT_ON = 700
private const val DAY_WEIGHT_OFF = 400

/**
 * Repeat-day letters.
 *
 * All seven letters are kept inside one AnnotatedString.
 *
 * This is important because creating seven independent Text composables
 * introduces independent layout boxes and makes the spacing inconsistent.
 */
@Composable
private fun DayLetters(
    alarm: Alarm,
    order: List<DayOfWeek>,
    ink: Color,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    /*
     * Build the two font variants once.
     */
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

    /*
     * Build the seven-letter run.
     */
    val text = remember(
        alarm.days,
        alarm.isOneShot,
        order,
        ink,
    ) {
        buildAnnotatedString {
            order.forEach { day ->
                val active =
                    !alarm.isOneShot &&
                        day in alarm.days

                withStyle(
                    SpanStyle(
                        fontFamily =
                            if (active) {
                                bold.fontFamily
                            } else {
                                light.fontFamily
                            },

                        color =
                            if (active) {
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

    /*
     * ---------------------------------------------------------------
     * FIX FOR THE PREVIOUS COMPILATION ERROR
     * ---------------------------------------------------------------
     *
     * Do NOT use:
     *
     *     something.em
     *
     * in this file.
     *
     * Instead create the TextUnit explicitly.
     *
     * TextUnitType.Em is available without relying on the `.em`
     * extension that caused the previous build failure.
     */

    val fontSizePx =
        with(density) {
            bold.fontSize.toPx()
        }

    val trackingPx =
        with(density) {
            DAY_TRACKING.toPx()
        }

    val trackingEm =
        if (fontSizePx > 0f) {
            trackingPx / fontSizePx
        } else {
            0f
        }

    val letterSpacing =
        TextUnit(
            value = trackingEm,
            type = TextUnitType.Em,
        )

    val style = bold.copy(
        letterSpacing = letterSpacing,
    )

    /*
     * Measure the actual text run.
     */
    val measuredWidth =
        measurer
            .measure(
                text = text,
                style = style,
            )
            .size
            .width

    /*
     * Convert target width from dp to px.
     */
    val targetWidth =
        with(density) {
            DAY_BLOCK_WIDTH.toPx()
        }

    /*
     * Scale only when we have a valid measured width.
     */
    val scaleX =
        if (measuredWidth > 0) {
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