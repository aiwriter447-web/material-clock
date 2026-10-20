package app.materialclock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.ClockTimer
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import java.text.DateFormatSymbols
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The large line, and a smaller line under it, shown in the space the one-hand pull-down opens. */
class ReachText(val title: String, val detail: String? = null)

/**
 * The text that fills the space the "one-hand" pull-down opens at the top of a tab: a one-glance
 * answer to "what is this tab doing right now".
 */
@Composable
fun ReachSummary(summary: ReachText, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = summary.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
        if (summary.detail != null) {
            Text(
                text = summary.detail,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/**
 * What [ReachSummary] says for [tab].
 *
 * [minuteTick] is not used for anything except being different each time it is called: it makes the
 * caller recompute this a couple of times a minute, so "Alarm in 8 hours 12 minutes" does not go
 * stale while the screen is open.
 */
@Suppress("UNUSED_PARAMETER")
fun reachSummaryText(
    tab: Tab,
    alarms: List<Alarm>,
    timer: ClockTimer?,
    stopwatch: Stopwatch,
    is24Hour: Boolean,
    minuteTick: Int,
): ReachText {
    val now = ZonedDateTime.now()
    return when (tab) {
        Tab.ALARMS -> {
            val next = alarms.mapNotNull { it.nextFire(now) }.minOrNull()
            if (next == null) {
                ReachText("All alarms are off")
            } else {
                ReachText(
                    "Alarm in ${countdown(Duration.between(now, next))}",
                    // "Wed, 21 Oct, 6:40 am"
                    dayAndTime(next, is24Hour),
                )
            }
        }
        Tab.TIMERS -> ReachText(
            when (timer?.state) {
                TimerState.RUNNING -> "Timer running"
                TimerState.PAUSED -> "Timer paused"
                TimerState.FINISHED -> "Timer finished"
                else -> "No timer running"
            },
        )
        Tab.STOPWATCH -> ReachText(
            when {
                stopwatch.running -> "Stopwatch running"
                !stopwatch.accumulated.isZero -> "Stopwatch paused"
                else -> "Stopwatch ready"
            },
        )
        // The time here, and today's date under it.
        Tab.WORLD -> ReachText(
            timeOnly(now, is24Hour),
            now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
        )
    }
}

/** "6:40 am", or "06:40" on a 24-hour clock. */
private fun timeOnly(t: ZonedDateTime, is24Hour: Boolean): String =
    if (is24Hour) {
        t.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    } else {
        val amPm = DateFormatSymbols.getInstance(Locale.getDefault()).amPmStrings[if (t.hour < 12) 0 else 1]
        t.format(DateTimeFormatter.ofPattern("h:mm", Locale.getDefault())) + " " + amPm.lowercase()
    }

/** "Wed, 21 Oct, 6:40 am". */
private fun dayAndTime(t: ZonedDateTime, is24Hour: Boolean): String =
    t.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())) + ", " + timeOnly(t, is24Hour)

private fun plural(n: Long, unit: String) = if (n == 1L) "$n $unit" else "$n ${unit}s"

/**
 * "1 day 22 hours 43 minutes". Once it is a day or more away the count is in days, so "46 hours"
 * never appears: the parts that are zero are left out.
 */
private fun countdown(d: Duration): String {
    val days = d.toDays()
    val hours = d.toHours() % 24
    val minutes = d.toMinutes() % 60
    val parts = buildList {
        if (days > 0) add(plural(days, "day"))
        if (hours > 0) add(plural(hours, "hour"))
        if (minutes > 0) add(plural(minutes, "minute"))
    }
    return if (parts.isEmpty()) "less than a minute" else parts.joinToString(" ")
}
