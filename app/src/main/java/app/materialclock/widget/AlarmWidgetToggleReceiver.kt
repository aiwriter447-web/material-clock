package app.materialclock.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.materialclock.alarm.AlarmScheduler
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Turns an alarm on or off when its round button in the Alarm widget is tapped.
 *
 * It does exactly what the app's own switch does (flip `enabled`, clear any snooze, then schedule
 * or cancel the real alarm), so the widget and the app can never disagree.
 */
class AlarmWidgetToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id < 0) return

        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val store = ClockStore(app)
                val next = store.alarmsNow().map {
                    if (it.id == id) it.copy(enabled = !it.enabled, snoozedUntilMillis = null) else it
                }
                // Saving also redraws every Alarm widget.
                store.putAlarms(next)
                next.firstOrNull { it.id == id }?.let { alarm ->
                    try {
                        if (alarm.enabled) AlarmScheduler.schedule(app, alarm) else AlarmScheduler.cancel(app, id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "app.materialclock.action.WIDGET_ALARM_TOGGLE"
        const val EXTRA_ID = "alarm_id"
    }
}
