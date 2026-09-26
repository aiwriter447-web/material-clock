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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps Timer and Stopwatch Live Update notifications fresh while they are
 * actively running.
 *
 * Notification actions and this service share LiveUpdateCoordinator so the
 * service can never repost an old snapshot over a notification action that
 * the user has just performed.
 */
class LiveUpdateService : Service() {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO,
    )

    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {

        // A second start should not create another ticking loop.
        if (job?.isActive == true) {
            return START_STICKY
        }

        val store = ClockStore(applicationContext)

        job = scope.launch {

            var foregrounded = false

            while (true) {

                val shouldContinue =
                    LiveUpdateCoordinator.withNotificationLock {

                        val timer = store.timer.first()
                        val sw = store.stopwatch.first()

                        val timerLive =
                            timer != null &&
                                timer.state == TimerState.RUNNING

                        val swLive = sw.running

                        if (!timerLive && !swLive) {
                            false
                        } else {

                            /*
                             * The first active notification becomes the
                             * foreground-service notification.
                             *
                             * Timer gets priority because it has a definite
                             * end point.
                             */
                            if (!foregrounded) {

                                val (id, notification) =
                                    if (timerLive) {
                                        Notifications.ID_TIMER to
                                            Notifications.buildTimer(
                                                this@LiveUpdateService,
                                                timer!!,
                                            )
                                    } else {
                                        Notifications.ID_STOPWATCH to
                                            Notifications.buildStopwatch(
                                                this@LiveUpdateService,
                                                sw,
                                            )
                                    }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    startForeground(
                                        id,
                                        notification,
                                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                                    )
                                } else {
                                    startForeground(
                                        id,
                                        notification,
                                    )
                                }

                                foregrounded = true
                            }

                            /*
                             * IMPORTANT:
                             *
                             * Reading state and posting notifications happen
                             * under the same mutex used by ClockActionReceiver.
                             *
                             * Therefore an old state can no longer overwrite
                             * a freshly clicked Pause/Resume/Cancel/Stop/Lap.
                             */
                            if (timerLive) {
                                Notifications.showTimer(
                                    this@LiveUpdateService,
                                    timer!!,
                                )
                            }

                            if (swLive) {
                                Notifications.showStopwatch(
                                    this@LiveUpdateService,
                                    sw,
                                )
                            }

                            true
                        }
                    }

                if (!shouldContinue) {
                    break
                }

                delay(1000L)
            }

            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        scope.coroutineContext.cancel()
        super.onDestroy()
    }

    companion object {

        /**
         * Safe to call whenever a timer resumes/adds time or a stopwatch starts.
         *
         * If the service is already running, onStartCommand does not create
         * another loop.
         */
        fun ensureRunning(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(
                    context,
                    LiveUpdateService::class.java,
                ),
            )
        }
    }
}