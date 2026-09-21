package app.materialclock.ui

import androidx.lifecycle.ViewModel
import app.materialclock.data.Alarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClockViewModel : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(
        listOf(
            Alarm(
                hour = 7,
                minute = 0,
                label = "Morning Walk",
                category = "Morning"
            ),
            Alarm(
                hour = 7,
                minute = 30,
                label = "Breakfast",
                category = "Morning"
            ),
            Alarm(
                hour = 9,
                minute = 0,
                label = "Team Meeting",
                category = "Work"
            ),
            Alarm(
                hour = 22,
                minute = 0,
                label = "Bedtime",
                category = "Night"
            )
        )
    )

    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _is24HourFormat = MutableStateFlow(false)
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    fun toggleTimeFormat() {
        _is24HourFormat.update { !it }
    }

    /**
     * Alarm को ON/OFF करता है।
     *
     * Signature:
     * fun toggleAlarm(alarm: Alarm)
     */
    fun toggleAlarm(alarm: Alarm) {
        _alarms.update { list ->
            list.map { currentAlarm ->
                if (currentAlarm.id == alarm.id) {
                    currentAlarm.copy(
                        isEnabled = !currentAlarm.isEnabled
                    )
                } else {
                    currentAlarm
                }
            }
        }
    }

    /**
     * किसी एक category के सभी alarms को
     * ON या OFF करता है।
     */
    fun toggleAlarmGroup(
        category: String,
        isEnabled: Boolean
    ) {
        _alarms.update { list ->
            list.map { alarm ->
                if (alarm.category == category) {
                    alarm.copy(isEnabled = isEnabled)
                } else {
                    alarm
                }
            }
        }
    }
}