package app.materialclock.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import app.materialclock.MainActivity
import app.materialclock.R
import app.materialclock.core.Alarm
import app.materialclock.core.ClockTimer
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import app.materialclock.core.clockFormat
import app.materialclock.core.stopwatchParts
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object Notifications {

    const val CHANNEL_ALARM = "alarm"
    const val CHANNEL_UPCOMING = "upcoming"
    const val CHANNEL_TIMER = "timer"
    const val CHANNEL_STOPWATCH = "stopwatch"
    const val CHANNEL_NEXT_ALARM = "next_alarm"

    const val ID_RINGING = 1
    const val ID_TIMER = 2
    const val ID_STOPWATCH = 3
    const val ID_NEXT_ALARM = 5

    private const val ID_UPCOMING_BASE = 4_000

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService<NotificationManager>() ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "A ringing alarm"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility =
                    android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_UPCOMING,
                "Upcoming alarms",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "A reminder before an alarm rings"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIMER,
                "Timers",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "A running timer"
                setSound(null, null)
                lockscreenVisibility =
                    android.app.Notification.VISIBILITY_PUBLIC
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_STOPWATCH,
                "Stopwatch",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "A running stopwatch"
                setSound(null, null)
                lockscreenVisibility =
                    android.app.Notification.VISIBILITY_PUBLIC
            }
        )

        // Next alarm indicator channel
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NEXT_ALARM,
                "Next alarm indicator",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows the next scheduled alarm"
                setSound(null, null)
                setShowBadge(false)
            }
        )
    }

    /* ── Next alarm indicator ─────────────────────────────────────── */

    fun refreshNextAlarmIndicator(
        context: Context,
        alarms: List<Alarm>,
        zone: ZoneId = ZoneId.systemDefault(),
    ) {
        val now = ZonedDateTime.now(zone)

        val next = alarms
            .filter { it.enabled }
            .mapNotNull { it.nextFire(now) }
            .minOrNull()

        if (next == null) {
            NotificationManagerCompat
                .from(context)
                .cancel(ID_NEXT_ALARM)
            return
        }

        val is24 =
            android.text.format.DateFormat.is24HourFormat(context)

        val hour = if (is24) {
            next.hour
        } else {
            ((next.hour % 12).takeIf { it != 0 } ?: 12)
        }

        val meridiem = if (is24) {
            ""
        } else if (next.hour < 12) {
            " AM"
        } else {
            " PM"
        }

        val timeText = "%d:%02d%s".format(
            hour,
            next.minute,
            meridiem,
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_NEXT_ALARM,
        )
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setColor(
                ContextCompat.getColor(
                    context,
                    R.color.notification_accent,
                )
            )
            .setOnlyAlertOnce(true)
            .setContentTitle("Next alarm: $timeText")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(openApp(context, TAB_ALARMS))
            .build()

        post(context, ID_NEXT_ALARM, notification)
    }

    /* ── Upcoming alarm ───────────────────────────────────────────── */

    fun showUpcoming(
        context: Context,
        alarm: Alarm,
    ) {
        post(
            context,
            ID_UPCOMING_BASE + alarm.id.toInt(),
            buildUpcoming(context, alarm),
        )
    }

    private fun buildUpcoming(
        context: Context,
        alarm: Alarm,
    ): android.app.Notification {
        val is24 =
            android.text.format.DateFormat.is24HourFormat(context)

        val hour = if (is24) {
            alarm.time.hour
        } else {
            ((alarm.time.hour % 12).takeIf { it != 0 } ?: 12)
        }

        val meridiem = if (is24) {
            ""
        } else if (alarm.time.hour < 12) {
            " AM"
        } else {
            " PM"
        }

        val timeText = "%d:%02d%s".format(
            hour,
            alarm.time.minute,
            meridiem,
        )

        val title = if (alarm.label.isNotBlank()) {
            "\"${alarm.label}\" rings at $timeText"
        } else {
            "Alarm rings at $timeText"
        }

        return NotificationCompat.Builder(
            context,
            CHANNEL_UPCOMING,
        )
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText("Tap to review it")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp(context, TAB_ALARMS))
            .build()
    }

    /* ── Timer ────────────────────────────────────────────────────── */

    fun showTimer(
        context: Context,
        timer: ClockTimer,
    ) {
        post(
            context,
            ID_TIMER,
            buildTimer(context, timer),
        )
    }

    fun buildTimer(
        context: Context,
        timer: ClockTimer,
    ): android.app.Notification {
        val running = timer.state == TimerState.RUNNING
        val now = SystemClock.elapsedRealtime()
        val remaining = timer.remaining(now)

        val b = NotificationCompat.Builder(
            context,
            CHANNEL_TIMER,
        )
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setColor(
                ContextCompat.getColor(
                    context,
                    R.color.notification_accent,
                )
            )
            .setOnlyAlertOnce(true)
            .setContentTitle(timer.label.ifBlank { "Timer" })
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )
            .setContentIntent(openApp(context, TAB_TIMERS))
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(remaining.clockFormat())
            .addAction(
                0,
                if (running) "Pause" else "Resume",
                broadcast(
                    context,
                    ClockActionReceiver.ACTION_TIMER_TOGGLE,
                    20,
                ),
            )
            .addAction(
                0,
                "+1 min",
                broadcast(
                    context,
                    ClockActionReceiver.ACTION_TIMER_ADD,
                    21,
                ),
            )
            .addAction(
                0,
                "Cancel",
                broadcast(
                    context,
                    ClockActionReceiver.ACTION_TIMER_CANCEL,
                    22,
                ),
            )
            .setStyle(
                NotificationCompat.ProgressStyle()
                    .setProgressSegments(
                        listOf(
                            NotificationCompat.ProgressStyle.Segment(100)
                        )
                    )
                    .setProgress(
                        timer.fractionLeft(now)
                            .let { (100 - (it * 100)).toInt() }
                            .coerceIn(0, 100)
                    ),
            )

        if (running) {
            val is24h =
                android.text.format.DateFormat.is24HourFormat(context)

            val setMinutes = timer.total.toMinutes()

            val durationLabel = if (setMinutes > 0) {
                "$setMinutes m"
            } else {
                "${timer.total.seconds} s"
            }

            val endInstant = LocalDateTime.now().plus(remaining)

            val endHour = if (is24h) {
                endInstant.hour
            } else {
                ((endInstant.hour % 12).takeIf { it != 0 } ?: 12)
            }

            val endMeridiem = if (is24h) {
                ""
            } else if (endInstant.hour < 12) {
                " am"
            } else {
                " pm"
            }

            val endLabel = "%d:%02d%s".format(
                endHour,
                endInstant.minute,
                endMeridiem,
            )

            b.setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(
                    System.currentTimeMillis() + remaining.toMillis()
                )
                .setShowWhen(true)
                .setContentText("$durationLabel / $endLabel")
        } else {
            b.setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(
                    "Paused · ${timer.pausedRemaining.clockFormat()}"
                )
        }

        return b.build()
    }

    fun hideTimer(context: Context) {
        NotificationManagerCompat
            .from(context)
            .cancel(ID_TIMER)
    }

    /* ── Stopwatch ────────────────────────────────────────────────── */

    fun showStopwatch(
        context: Context,
        sw: Stopwatch,
    ) {
        post(
            context,
            ID_STOPWATCH,
            buildStopwatch(context, sw),
        )
    }

    fun buildStopwatch(
        context: Context,
        sw: Stopwatch,
    ): android.app.Notification {
        val elapsed = sw.elapsed(SystemClock.elapsedRealtime())

        val b = NotificationCompat.Builder(
            context,
            CHANNEL_STOPWATCH,
        )
            .setSmallIcon(R.drawable.ic_stat_stopwatch)
            .setColor(
                ContextCompat.getColor(
                    context,
                    R.color.notification_accent,
                )
            )
            .setOnlyAlertOnce(true)
            .setContentTitle("Stopwatch")
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )
            .setContentIntent(openApp(context, TAB_STOPWATCH))
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(
                elapsed.clockFormat(withHours = true)
            )
            .setStyle(
                NotificationCompat.ProgressStyle()
                    .setProgressSegments(
                        listOf(
                            NotificationCompat.ProgressStyle.Segment(60)
                        )
                    )
                    .setProgress(
                        (elapsed.seconds % 60).toInt()
                    ),
            )
            .addAction(
                0,
                if (sw.running) "Stop" else "Start",
                broadcast(
                    context,
                    ClockActionReceiver.ACTION_SW_TOGGLE,
                    30,
                ),
            )
            .addAction(
                0,
                if (sw.running) "Lap" else "Reset",
                broadcast(
                    context,
                    if (sw.running) {
                        ClockActionReceiver.ACTION_SW_LAP
                    } else {
                        ClockActionReceiver.ACTION_SW_RESET
                    },
                    31,
                ),
            )

        if (sw.running) {
            val lapText = sw.laps.firstOrNull()?.let {
                "Lap ${it.index} · " +
                    it.split.clockFormat(withHours = true)
            }

            val (p1, p2, p3) = elapsed.stopwatchParts()

            val elapsedWithCentis = if (elapsed.toHours() > 0) {
                "$p1:$p2:$p3"
            } else {
                "$p1:$p2.$p3"
            }

            b.setUsesChronometer(true)
                .setWhen(
                    System.currentTimeMillis() - elapsed.toMillis()
                )
                .setShowWhen(true)
                .setContentText(lapText ?: elapsedWithCentis)
        } else {
            b.setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(
                    sw.accumulated.clockFormat(withHours = true)
                )
        }

        return b.build()
    }

    fun hideStopwatch(context: Context) {
        NotificationManagerCompat
            .from(context)
            .cancel(ID_STOPWATCH)
    }

    /* ── Plumbing ─────────────────────────────────────────────────── */

    const val TAB_ALARMS = "alarms"
    const val TAB_TIMERS = "timers"
    const val TAB_STOPWATCH = "stopwatch"
    const val EXTRA_TAB = "tab"

    fun openApp(
        context: Context,
        tab: String,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        tab.hashCode(),
        Intent(context, MainActivity::class.java)
            .setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            .putExtra(EXTRA_TAB, tab),
        PendingIntent.FLAG_UPDATE_CURRENT or
            PendingIntent.FLAG_IMMUTABLE,
    )

    fun broadcast(
        context: Context,
        action: String,
        requestCode: Int,
        id: Long = 0L,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, ClockActionReceiver::class.java)
                .setAction(action)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                .putExtra(
                    AlarmReceiver.EXTRA_ID,
                    id,
                ),
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE,
        )

    private fun post(
        context: Context,
        id: Int,
        n: android.app.Notification,
    ) {
        val nm = NotificationManagerCompat.from(context)

        if (!nm.areNotificationsEnabled()) return

        runCatching {
            nm.notify(id, n)
        }
    }
}