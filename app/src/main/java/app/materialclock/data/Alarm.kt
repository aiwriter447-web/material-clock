package app.materialclock.core

import java.time.DayOfWeek

data class Alarm(
    val id: Long = 0L,
    val hour: Int = 7,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val days: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val category: String = "Routine",
    val soundUri: String? = null,
    val vibrate: Boolean = true,
    val snoozedUntilEpochMillis: Long? = null
) {
    companion object {
        fun blankAlarm() = Alarm(
            id = 0L,
            hour = 8,
            minute = 0,
            isEnabled = true,
            label = "",
            category = "General"
        )
    }
}
