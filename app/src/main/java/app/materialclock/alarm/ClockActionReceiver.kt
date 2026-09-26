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
 * The buttons on the notifications, and nothing else.
 *
 * Notification actions are processed on a dedicated I/O coroutine and are
 * serialized with LiveUpdateService through LiveUpdateCoordinator.
 *
 * The serialization is important because LiveUpdateService reposts the
 * notification every second. Without coordination it could repost an old
 * notification immediately after the user presses Pause, Resume, Stop, Lap,
 * Cancel, etc.
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
                            val id = intent.getLongExtra(
                                AlarmReceiver.EXTRA_ID,
                                -1L,
                            )
                            snooze(app, store, id)
                        }

                        ACTION_DISMISS -> {
                            AlarmService.stop(app)
                        }

                        ACTION_TIMER_TOGGLE -> {
                            timerToggle(app, store)
                        }

                        ACTION_TIMER_ADD -> {
                            timerAdd(app, store)
                        }

                        ACTION_TIMER_CANCEL -> {
                            AlarmService.stop(app)
                            store.putTimer(null)
                            TimerScheduler.sync(app, null)

                            Notifications.hideTimer(app)
                        }

                        ACTION_SW_TOGGLE -> {
                            stopwatchToggle(app, store)
                        }

                        ACTION_SW_LAP -> {
                            stopwatchLap(app, store)
                        }

                        ACTION_SW_RESET -> {
                            store.putStopwatch(Stopwatch())
                            Notifications.hideStopwatch(app)
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun snooze(
        context: Context,
        store: ClockStore,
        id: Long,
    ) {
        AlarmService.stop(context)

        val minutes = store.settingsNow().alarms.snoozeMinutes
        val due = System.currentTimeMillis() + minutes * 60_000L

        val updated = store.alarmsNow().map {
            // Re-arming matters for a one-shot: firing disabled it, and a
            // snooze has to bring it back or the second ring never happens.
            if (it.id == id) {
                it.copy(
                    snoozedUntilMillis = due,
                    enabled = true,
                )
            } else {
                it
            }
        }

        store.putAlarms(updated)

        updated
            .firstOrNull { it.id == id }
            ?.let {
                AlarmScheduler.schedule(context, it)
            }
    }

    private suspend fun timerToggle(
        context: Context,
        store: ClockStore,
    ) {
        val t = store.timerNow() ?: return

        val now = SystemClock.elapsedRealtime()

        val next = when (t.state) {
            TimerState.RUNNING -> {
                t.copy(
                    state = TimerState.PAUSED,
                    pausedRemaining = t.remaining(now),
                )
            }

            else -> {
                t.copy(
                    state = TimerState.RUNNING,
                    deadlineElapsedMillis =
                        now + t.pausedRemaining.toMillis(),
                )
            }
        }

        // DataStore write completes before notification publishing.
        store.putTimer(next)

        // Keep the exact-alarm scheduler in sync with the new state.
        TimerScheduler.sync(context, next)

        // Immediately publish the new state rather than waiting for the
        // next LiveUpdateService tick.
        if (next.state == TimerState.RUNNING) {
            Notifications.showTimer(context, next)
            LiveUpdateService.ensureRunning(context)
        } else {
            Notifications.showTimer(context, next)
        }
    }

    private suspend fun timerAdd(
        context: Context,
        store: ClockStore,
    ) {
        val t = store.timerNow() ?: return

        val next: ClockTimer = when (t.state) {
            TimerState.RUNNING -> {
                t.copy(
                    total = t.total.plusMinutes(1),
                    deadlineElapsedMillis =
                        t.deadlineElapsedMillis + 60_000L,
                )
            }

            else -> {
                t.copy(
                    total = t.total.plusMinutes(1),
                    pausedRemaining =
                        t.pausedRemaining.plusMinutes(1),
                )
            }
        }

        store.putTimer(next)

        TimerScheduler.sync(context, next)

        Notifications.showTimer(context, next)

        if (next.state == TimerState.RUNNING) {
            LiveUpdateService.ensureRunning(context)
        }
    }

    private suspend fun stopwatchToggle(
        context: Context,
        store: ClockStore,
    ) {
        val sw = store.stopwatch.first()
        val now = SystemClock.elapsedRealtime()

        val next = if (sw.running) {
            sw.copy(
                running = false,
                accumulated = sw.elapsed(now),
            )
        } else {
            sw.copy(
                running = true,
                startedAtElapsed = now,
            )
        }

        store.putStopwatch(next)

        Notifications.showStopwatch(context, next)

        if (next.running) {
            LiveUpdateService.ensureRunning(context)
        } else {
            Notifications.showStopwatch(context, next)
        }
    }

    private suspend fun stopwatchLap(
        context: Context,
        store: ClockStore,
    ) {
        val sw = store.stopwatch.first()

        if (!sw.running) return

        val total = sw.elapsed(SystemClock.elapsedRealtime())

        val previous = sw.laps
            .firstOrNull()
            ?.total
            ?: Duration.ZERO

        val next = sw.copy(
            laps = listOf(
                Lap(
                    sw.laps.size + 1,
                    total.minus(previous),
                    total,
                )
            ) + sw.laps,
        )

        store.putStopwatch(next)

        // Immediately update the notification so the new lap appears
        // without waiting for the next one-second service tick.
        Notifications.showStopwatch(context, next)
    }

    companion object {

        private val actionScope = CoroutineScope(
            SupervisorJob() + Dispatchers.IO,
        )

        const val ACTION_SNOOZE =
            "app.materialclock.SNOOZE"

        const val ACTION_DISMISS =
            "app.materialclock.DISMISS"

        const val ACTION_TIMER_TOGGLE =
            "app.materialclock.TIMER_TOGGLE"

        const val ACTION_TIMER_ADD =
            "app.materialclock.TIMER_ADD"

        const val ACTION_TIMER_CANCEL =
            "app.materialclock.TIMER_CANCEL"

        const val ACTION_SW_TOGGLE =
            "app.materialclock.SW_TOGGLE"

        const val ACTION_SW_LAP =
            "app.materialclock.SW_LAP"

        const val ACTION_SW_RESET =
            "app.materialclock.SW_RESET"
    }
}