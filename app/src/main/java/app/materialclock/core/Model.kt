package app.materialclock.core

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * One alarm.
 */
data class Alarm(
    val id: Long,
    val time: LocalTime,
    val label: String = "",
    val days: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val snoozedUntilMillis: Long? = null,
    val groupId: Long? = null,
) {
    val isOneShot: Boolean get() = days.isEmpty()

    fun nextFire(now: ZonedDateTime): ZonedDateTime? {
        if (!enabled) return null
        snoozedUntilMillis?.let { at ->
            val snooze = java.time.Instant.ofEpochMilli(at).atZone(now.zone)
            if (snooze.isAfter(now)) return snooze
        }
        val todayAt = now.with(time).withSecond(0).withNano(0)
        if (isOneShot) return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
        return (0..7).asSequence()
            .map { todayAt.plusDays(it.toLong()) }
            .firstOrNull { it.isAfter(now) && it.dayOfWeek in days }
    }

    fun repeatLabel(now: LocalDate = LocalDate.now()): String = when {
        isOneShot -> "Once"
        days.size == 7 -> "Every day"
        days == WEEKDAYS -> "Weekdays"
        days == WEEKENDS -> "Weekends"
        else -> DayOfWeek.entries.filter { it in days }.joinToString(", ") {
            it.name.lowercase().replaceFirstChar(Char::uppercase).take(3)
        }
    }

    companion object {
        val WEEKDAYS: Set<DayOfWeek> = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        )
        val WEEKENDS: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}

/**
 * A named bucket of alarms — "Morning", "Night shift"
 */
data class AlarmGroup(val id: Long, val name: String)

fun humanUntil(from: ZonedDateTime, to: ZonedDateTime): String {
    val d = Duration.between(from, to)
    val days = d.toDays()
    val hours = d.toHours() % 24
    val mins = d.toMinutes() % 60
    return when {
        days > 0 -> "${days} d ${hours} h"
        hours > 0 -> "${hours} h ${mins} min"
        mins > 0 -> "${mins} min"
        else -> "less than a minute"
    }
}

/**
 * A city on the world clock.
 */
data class WorldCity(
    val zone: ZoneId,
    val city: String,
    val region: String,
    val country: String = "",
    val pinnedAt: Long? = null,
) {
    fun timeAt(nowUtcMillis: Long): ZonedDateTime =
        java.time.Instant.ofEpochMilli(nowUtcMillis).atZone(zone)

    fun offsetHours(home: ZoneId, nowUtcMillis: Long): Double {
        val inst = java.time.Instant.ofEpochMilli(nowUtcMillis)
        val a = zone.rules.getOffset(inst).totalSeconds
        val b = home.rules.getOffset(inst).totalSeconds
        return (a - b) / 3600.0
    }

    fun offsetLabel(home: ZoneId, nowUtcMillis: Long): String {
        val inst = java.time.Instant.ofEpochMilli(nowUtcMillis)
        val diffSeconds = zone.rules.getOffset(inst).totalSeconds - home.rules.getOffset(inst).totalSeconds
        val sign = if (diffSeconds >= 0) "+" else "−"
        val totalMinutes = kotlin.math.abs(diffSeconds) / 60
        val m = (totalMinutes % 60).toString().padStart(2, '0')
        return "$sign${totalMinutes / 60}:$m h"
    }

    fun utcCode(nowUtcMillis: Long): String {
        val inst = java.time.Instant.ofEpochMilli(nowUtcMillis)
        val seconds = zone.rules.getOffset(inst).totalSeconds
        val sign = if (seconds >= 0) "+" else "−"
        val totalMinutes = kotlin.math.abs(seconds) / 60
        val h = (totalMinutes / 60).toString().padStart(2, '0')
        val m = (totalMinutes % 60).toString().padStart(2, '0')
        return "UTC$sign$h:$m"
    }

    fun isNight(nowUtcMillis: Long): Boolean =
        timeAt(nowUtcMillis).hour.let { it < 6 || it >= 20 }

    fun dayFraction(nowUtcMillis: Long): Float {
        val t = timeAt(nowUtcMillis).toLocalTime()
        return (t.toSecondOfDay() / 86400f)
    }
}

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

/**
 * A named, reusable timer length.
 */
data class TimerPreset(val id: Long, val name: String, val totalSeconds: Int)

/**
 * A countdown.
 */
data class ClockTimer(
    val id: Long,
    val label: String,
    val total: Duration,
    val state: TimerState = TimerState.IDLE,
    val deadlineElapsedMillis: Long = 0L,
    val pausedRemaining: Duration = total,
) {
    fun remaining(nowElapsedMillis: Long): Duration = when (state) {
        TimerState.RUNNING ->
            Duration.ofMillis((deadlineElapsedMillis - nowElapsedMillis).coerceAtLeast(0L))
        TimerState.IDLE -> total
        else -> pausedRemaining
    }

    fun fractionLeft(nowElapsedMillis: Long): Float {
        val t = total.toMillis().coerceAtLeast(1L)
        return (remaining(nowElapsedMillis).toMillis().toFloat() / t).coerceIn(0f, 1f)
    }
}

data class Lap(val index: Int, val split: Duration, val total: Duration)

/**
 * The stopwatch.
 */
data class Stopwatch(
    val running: Boolean = false,
    val startedAtElapsed: Long = 0L,
    val accumulated: Duration = Duration.ZERO,
    val laps: List<Lap> = emptyList(),
) {
    fun elapsed(nowElapsedMillis: Long): Duration =
        if (running) accumulated.plusMillis(nowElapsedMillis - startedAtElapsed) else accumulated

    val fastest: Int? get() = laps.minByOrNull { it.split }?.index?.takeIf { laps.size > 1 }
    val slowest: Int? get() = laps.maxByOrNull { it.split }?.index?.takeIf { laps.size > 1 }
}

fun Duration.clockFormat(withHours: Boolean = false): String {
    val total = seconds.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0 || withHours) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun Duration.stopwatchParts(): Triple<String, String, String> {
    val ms = toMillis().coerceAtLeast(0)
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    val s = (ms % 60_000) / 1000
    val cs = (ms % 1000) / 10
    return if (h > 0) {
        Triple("%02d".format(h), "%02d".format(m), "%02d".format(s))
    } else {
        Triple("%02d".format(m), "%02d".format(s), "%02d".format(cs))
    }
}

fun LocalDateTime.parts(use24h: Boolean): Triple<String, String, String?> {
    val h = if (use24h) hour else ((hour % 12).takeIf { it != 0 } ?: 12)
    val ap = if (use24h) null else if (hour < 12) "AM" else "PM"
    return Triple("%02d".format(h), "%02d".format(minute), ap)
}
