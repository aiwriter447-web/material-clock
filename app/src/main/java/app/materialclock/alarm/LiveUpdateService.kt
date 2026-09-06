package app.materialclock.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import app.materialclock.core.TimerState
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The one exception to "no service" in [Notifications]'s class doc.
 *
 * A chronometer ticks on its own; a [androidx.core.app.NotificationCompat.ProgressStyle] bar and a
 * status-bar chip's `shortCriticalText` do not, and there is no platform tween for either — so
 * making them move live means something reposting the notification every second while a timer or
 * stopwatch is actually running. This is that something, and nothing else: it holds no state of
 * its own, only rereads the current timer/stopwatch from [ClockStore] each tick and reposts
 * whichever is active.
 *
 * Its own foreground notification is simply the first tick's timer/stopwatch notification — there
 * is no separate "service is running" notification sitting above the one the person actually
 * cares about.
 *
 * It stops itself the instant neither is running, so the cost is exactly "while something is
 * actively counting", never a poll left ticking after the countdown ends or the app is closed —
 * [ClockViewModel] only ever has to *start* this; it never has to remember to stop it.
 */
class LiveUpdateService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // A second start (e.g. the timer and the stopwatch both becoming active) should not spin
        // up a second ticking loop — one loop already reads and reposts both.
        if (job?.isActive == true) return START_STICKY

        val store = ClockStore(applicationContext)
        job = scope.launch {
            var foregrounded = false
            while (true) {
                val timer = store.timer.first()
                val sw = store.stopwatch.first()
                val timerLive = timer != null && timer.state == TimerState.RUNNING
                val swLive = sw.running

                if (!timerLive && !swLive) break

                // Whichever is live becomes the foreground notification on the first tick; if both
                // are, the timer wins the foreground slot (it is the one with an end to reach) and
                // the stopwatch is still posted right after, just as an ordinary second notification.
                if (!foregrounded) {
                    val (id, notif) = if (timerLive) {
                        Notifications.ID_TIMER to Notifications.buildTimer(this@LiveUpdateService, timer!!)
                    } else {
                        Notifications.ID_STOPWATCH to Notifications.buildStopwatch(this@LiveUpdateService, sw)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(id, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } else {
                        startForeground(id, notif)
                    }
                    foregrounded = true
                }

                if (timerLive) Notifications.showTimer(this@LiveUpdateService, timer!!)
                if (swLive) Notifications.showStopwatch(this@LiveUpdateService, sw)

                delay(1000)
            }
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    companion object {
        /**
         * Safe to call whenever a timer resumes or a stopwatch starts, even if the service is
         * already running — `onStartCommand` treats a second call as a no-op once its loop is
         * live, and the loop itself stops the service once nothing is left to tick.
         */
        fun ensureRunning(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, LiveUpdateService::class.java))
        }
    }
}
