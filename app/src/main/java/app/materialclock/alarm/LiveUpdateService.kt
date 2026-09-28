package app.materialclock.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.materialclock.R
import app.materialclock.core.TimerState
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps Timer and Stopwatch Live Update notifications fresh while they are
 * actively running.
 */
class LiveUpdateService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        // Prevent duplicate loops
        if (job?.isActive == true) {
            return START_STICKY
        }

        // CRITICAL FIX FOR ANDROID 14/15/16:
        // Foreground services must call startForeground() IMMEDIATELY.
        // We post a secure placeholder notification instantly to prevent ForegroundServiceStartNotAllowedException crashes.
        try {
            val placeholder = NotificationCompat.Builder(this, Notifications.CHANNEL_TIMER)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Updating Clock...")
                .setSilent(true)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    Notifications.ID_TIMER,
                    placeholder,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(Notifications.ID_TIMER, placeholder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val store = ClockStore(applicationContext)

        job = scope.launch {
            var foregrounded = false

            while (true) {
                val shouldContinue = LiveUpdateCoordinator.withNotificationLock {
                    val timer = store.timer.first()
                    val sw = store.stopwatch.first()

                    val timerLive = timer != null && timer.state == TimerState.RUNNING
                    val swLive = sw.running

                    if (!timerLive && !swLive) {
                        false
                    } else {
                        // Upgrade placeholder to actual Live/Now Bar Notification
                        if (!foregrounded) {
                            val (id, notification) = if (timerLive) {
                                Notifications.ID_TIMER to Notifications.buildTimer(this@LiveUpdateService, timer!!)
                            } else {
                                Notifications.ID_STOPWATCH to Notifications.buildStopwatch(this@LiveUpdateService, sw)
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                startForeground(
                                    id,
                                    notification,
                                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                                )
                            } else {
                                startForeground(id, notification)
                            }

                            // If the active ID is Stopwatch, cancel the Timer placeholder to avoid ghost notifications
                            if (id != Notifications.ID_TIMER) {
                                NotificationManagerCompat.from(this@LiveUpdateService).cancel(Notifications.ID_TIMER)
                            }

                            foregrounded = true
                        }

                        if (timerLive) {
                            Notifications.showTimer(this@LiveUpdateService, timer!!)
                        }
                        if (swLive) {
                            Notifications.showStopwatch(this@LiveUpdateService, sw)
                        }
                        true
                    }
                }

                if (!shouldContinue) {
                    break
                }
                delay(5000L)
            }

            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun ensureRunning(context: Context) {
            try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, LiveUpdateService::class.java)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
