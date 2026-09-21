package app.materialclock.data

import java.time.DayOfWeek
import java.util.UUID

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int = 7,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val days: Set<DayOfWeek> = emptySet(),
    val label: String = "Alarm",
    val category: String = "Routine"
) {
    companion object {
        val blankAlarm = Alarm(
            id = "",
            hour = 8,
            minute = 0,
            isEnabled = true,
            label = "",
            category = "General"
        )
    }
}
