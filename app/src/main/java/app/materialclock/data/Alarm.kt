package app.materialclock.data

import java.time.DayOfWeek
import java.util.UUID

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val days: Set<DayOfWeek> = emptySet(),
    val label: String = "Alarm",
    val category: String = "Routine"
)
