package app.materialclock.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.runBlocking

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
        if (job?.isActive == true) {
            return START_STICKY
        }

        val store = ClockStore(applicationContext)
        Notifications.ensureChannels(this)

        try {
            val timer = runBlocking { store.timer.first() }
            val sw = runBlocking { store.stopwatch.first() }

            val timerLive = timer != null && timer.state == TimerState.RUNNING
            val swLive = sw.running

            if (timerLive || swLive) {
                val (id, notification) = if (timerLive) {
                    Notifications.ID_TIMER to Notifications.buildTimer(this, timer!!)
                } else {
                    Notifications.ID_STOPWATCH to Notifications.buildStopwatch(this, sw)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        id, 
                        notification, 
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(id, notification)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        job = scope.launch {
            while (true) {
                val shouldContinue = LiveUpdateCoordinator.withNotificationLock {
                    val timer = store.timer.first()
                    val sw = store.stopwatch.first()

                    val timerLive = timer != null && timer.state == TimerState.RUNNING
                    val swLive = sw.running

                    if (!timerLive && !swLive) {
                        false
                    } else {
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
                
                delay(2000L)
            }

            // GHOST NOTIFICATION FIX: 
            // Detach service from notification, then manually check if they were cancelled/reset.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false)
            }

            delay(100) // Brief pause to ensure ClockActionReceiver has updated the store
            
            val finalTimer = store.timer.first()
            val finalSw = store.stopwatch.first()
            val nm = NotificationManagerCompat.from(this@LiveUpdateService)

            // If completely cancelled, wipe it. (If just paused, it stays).
            if (finalTimer == null) {
                nm.cancel(Notifications.ID_TIMER)
            }
            if (!finalSw.running && finalSw.accumulated.isZero) {
                nm.cancel(Notifications.ID_STOPWATCH)
            }

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
