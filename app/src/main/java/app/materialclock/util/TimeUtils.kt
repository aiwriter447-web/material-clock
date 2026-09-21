package app.materialclock.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    fun formatAlarmTime(hour: Int, minute: Int, is24HourFormat: Boolean): String {
        val time = LocalTime.of(hour, minute)
        val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
}
