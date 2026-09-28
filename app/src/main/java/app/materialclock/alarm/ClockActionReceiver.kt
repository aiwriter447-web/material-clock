package app.materialclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import app.materialclock.core.ClockTimer
import app.materialclock.core.Lap
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.Duration

/**
 * Handles actions from notifications. 
 * Prevents ForegroundServiceStartNotAllowedException crashes on Android 12+ 
 * by wrapping background service starts in a try-catch block.
 */
class ClockActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pending = goAsync()

        actionScope.launch {
            try {
                LiveUpdateCoordinator.withNotificationLock {
                    val store = ClockStore(app)

                    when (intent.action) {
                        ACTION_SNOOZE -> {
                            val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L)
                            snooze(app, store, id)
                        }

                        ACTION_DISMISS -> {
                            try { AlarmService.stop(app) } catch (e: Exception) { e.printStackTrace() }
                        }

                        ACTION_TIMER_TOGGLE -> {
                            timerToggle(app, store)
                        }

                        ACTION_TIMER_ADD -> {
                            timerAdd(app, store)
                        }

                        ACTION_TIMER_CANCEL -> {
                            try { AlarmService.stop(app) } catch (e: Exception) { e.printStackTrace() }
                            store.putTimer(null)
                            try { TimerScheduler.sync(app, null) } catch (e: Exception) { e.printStackTrace() }
                            try { Notifications.hideTimer(app) } catch (e: Exception) { e.printStackTrace() }
                        }

                        ACTION_SW_TOGGLE -> {
                            stopwatchToggle(app, store)
                        }

                        ACTION_SW_LAP -> {
                            stopwatchLap(app, store)
                        }

                        ACTION_SW_RESET -> {
                            store.putStopwatch(Stopwatch())
                            try { Notifications.hideStopwatch(app) } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun snooze(context: Context, store: ClockStore, id: Long) {
        try { AlarmService.stop(context) } catch (e: Exception) { e.printStackTrace() }

        val minutes = store.settingsNow().alarms.snoozeMinutes
        val due = System.currentTimeMillis() + minutes * 60_000L

        val updated = store.alarmsNow().map {
            if (it.id == id) {
                it.copy(snoozedUntilMillis = due, enabled = true)
            } else {
                it
            }
        }

        store.putAlarms(updated)

        updated.firstOrNull { it.id == id }?.let {
            try {
                AlarmScheduler.schedule(context, it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun timerToggle(context: Context, store: ClockStore) {
        val t = store.timerNow() ?: return
        val now = SystemClock.elapsedRealtime()

        val next = when (t.state) {
            TimerState.RUNNING -> {
                t.copy(state = TimerState.PAUSED, pausedRemaining = t.remaining(now))
            }
            else -> {
                t.copy(state = TimerState.RUNNING, deadlineElapsedMillis = now + t.pausedRemaining.toMillis())
            }
        }

        store.putTimer(next)
        
        try {
            TimerScheduler.sync(context, next)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Notifications.showTimer(context, next)

        if (next.state == TimerState.RUNNING) {
            try {
                LiveUpdateService.ensureRunning(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun timerAdd(context: Context, store: ClockStore) {
        val t = store.timerNow() ?: return

        val next: ClockTimer = when (t.state) {
            TimerState.RUNNING -> {
                t.copy(
                    total = t.total.plusMinutes(1),
                    deadlineElapsedMillis = t.deadlineElapsedMillis + 60_000L,
                )
            }
            else -> {
                t.copy(
                    total = t.total.plusMinutes(1),
                    pausedRemaining = t.pausedRemaining.plusMinutes(1),
                )
            }
        }

        store.putTimer(next)
        
        try {
            TimerScheduler.sync(context, next)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Notifications.showTimer(context, next)

        if (next.state == TimerState.RUNNING) {
            try {
                LiveUpdateService.ensureRunning(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun stopwatchToggle(context: Context, store: ClockStore) {
        val sw = store.stopwatch.first()
        val now = SystemClock.elapsedRealtime()

        val next = if (sw.running) {
            sw.copy(running = false, accumulated = sw.elapsed(now))
        } else {
            sw.copy(running = true, startedAtElapsed = now)
        }

        store.putStopwatch(next)

        Notifications.showStopwatch(context, next)

        if (next.running) {
            try {
                LiveUpdateService.ensureRunning(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun stopwatchLap(context: Context, store: ClockStore) {
        val sw = store.stopwatch.first()

        if (!sw.running) return

        val total = sw.elapsed(SystemClock.elapsedRealtime())
        val previous = sw.laps.firstOrNull()?.total ?: Duration.ZERO

        val next = sw.copy(
            laps = listOf(
                Lap(sw.laps.size + 1, total.minus(previous), total)
            ) + sw.laps,
        )

        store.putStopwatch(next)
        Notifications.showStopwatch(context, next)
    }

    companion object {

        private val actionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        const val ACTION_SNOOZE = "app.materialclock.SNOOZE"
        const val ACTION_DISMISS = "app.materialclock.DISMISS"
        const val ACTION_TIMER_TOGGLE = "app.materialclock.TIMER_TOGGLE"
        const val ACTION_TIMER_ADD = "app.materialclock.TIMER_ADD"
        const val ACTION_TIMER_CANCEL = "app.materialclock.TIMER_CANCEL"
        const val ACTION_SW_TOGGLE = "app.materialclock.SW_TOGGLE"
        const val ACTION_SW_LAP = "app.materialclock.SW_LAP"
        const val ACTION_SW_RESET = "app.materialclock.SW_RESET"
    }
}
