package app.materialclock.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.ClockTimer
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import java.time.Duration
import java.time.ZonedDateTime

/**
 * The large line of text that fills the space the "one-hand" pull-down opens at the top of the
 * Alarms, Timers and Stopwatch tabs: a one-glance answer to "what is this tab doing right now".
 */
@Composable
fun ReachSummary(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
}

/**
 * What [ReachSummary] says for [tab].
 *
 * [minuteTick] is not used for anything except being different each time it is called: it makes the
 * caller recompute this a couple of times a minute, so "Next alarm in 8 h 12 m" does not go stale
 * while the screen is open.
 */
@Suppress("UNUSED_PARAMETER")
fun reachSummaryText(
    tab: Tab,
    alarms: List<Alarm>,
    timer: ClockTimer?,
    stopwatch: Stopwatch,
    minuteTick: Int,
): String = when (tab) {
    Tab.ALARMS -> {
        val now = ZonedDateTime.now()
        val next = alarms.mapNotNull { it.nextFire(now) }.minOrNull()
        if (next == null) "All alarms are off" else "Next alarm in ${countdown(Duration.between(now, next))}"
    }
    Tab.TIMERS -> when (timer?.state) {
        TimerState.RUNNING -> "Timer running"
        TimerState.PAUSED -> "Timer paused"
        TimerState.FINISHED -> "Timer finished"
        else -> "No timer running"
    }
    Tab.STOPWATCH -> when {
        stopwatch.running -> "Stopwatch running"
        !stopwatch.accumulated.isZero -> "Stopwatch paused"
        else -> "Stopwatch ready"
    }
    // The World Clock tab has its own large clock at the top and does not use this.
    Tab.WORLD -> ""
}

/** "2 d 3 h", "8 h 12 m", "12 m", or "less than a minute". */
private fun countdown(d: Duration): String {
    val days = d.toDays()
    val hours = d.toHours() % 24
    val minutes = d.toMinutes() % 60
    return when {
        days > 0 -> "$days d $hours h"
        hours > 0 -> "$hours h $minutes m"
        minutes > 0 -> "$minutes m"
        else -> "less than a minute"
    }
}
