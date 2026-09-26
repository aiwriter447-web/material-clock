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
import androidx.core.content.getSystemService
import app.materialclock.MainActivity
import app.materialclock.R
import app.materialclock.core.ClockTimer
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import app.materialclock.core.clockFormat
import app.materialclock.core.parts
import app.materialclock.core.stopwatchParts
import java.time.LocalDateTime

/**
 * Every channel and every notification the app posts.
 *
 * ## The timer and stopwatch notifications carry no service
 *
 * They do not need one. A countdown is a *deadline*, not a process: the remaining time is
 * `deadline − elapsedRealtime()`, correct whether or not this app has run since. So the ongoing
 * notification is posted once with `setUsesChronometer` and the system renders the ticking digits
 * itself, at zero cost to the app, for as long as the notification exists. Notifications outlive
 * the process that posted them. A foreground service would burn a wakelock to recompute a number
 * the platform can derive from a timestamp.
 *
 * `setChronometerCountDown(true)` is what makes the timer count *down* rather than up; it needs
 * API 24 and the app's floor is 26. The expiry itself is an exact alarm, so nothing has to be
 * awake to notice it.
 *
 * The ringer is the one thing that does take a foreground service, because looping audio is real
 * ongoing work (see [AlarmService]).
 *
 * ## Promoted ongoing (Android 16 Now Bar / Live Updates)
 *
 * `setRequestPromotedOngoing` and `POST_PROMOTED_NOTIFICATIONS` in the manifest are what let the
 * timer and stopwatch reach the Now Bar / status-bar chip on API 36+; `androidx.core` 1.17 makes
 * both calls safe no-ops below that, so there is no version check here to get wrong.
 *
 * Unlike the chronometer digits, the progress bar and the status-bar chip's `shortCriticalText` do
 * not animate on their own — there is no platform-side tween for either, so making them move live
 * needs something reposting the notification every second while a timer or stopwatch is actually
 * running. That something is [LiveUpdateService], the one deliberate exception to "no service"
 * above: a real, if small, battery cost, taken on purpose and only while something is actually
 * counting, in exchange for the chip and bar being genuinely live rather than a frozen snapshot.
 */
object Notifications {

    const val CHANNEL_ALARM = "alarm"
    const val CHANNEL_UPCOMING = "upcoming"
    const val CHANNEL_TIMER = "timer"
    const val CHANNEL_STOPWATCH = "stopwatch"

    const val ID_RINGING = 1
    const val ID_TIMER = 2
    const val ID_STOPWATCH = 3

    /** Offset so an upcoming-alarm notice never collides with [ID_RINGING]/[ID_TIMER]/[ID_STOPWATCH]. */
    private const val ID_UPCOMING_BASE = 4_000

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService<NotificationManager>() ?: return

        // No sound on the channel: the ringer plays its own looping audio, and a channel sound
        // would fire a second, one-shot copy over the top of it.
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "A ringing alarm"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
        )

        nm.createNotificationChannel(
            // DEFAULT, not HIGH: this is a heads-up notice, not the ring — it should announce
            // itself once and sit in the shade, not compete with the alarm channel's own urgency.
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
            }
        )
    }

    /* ── Upcoming alarm ─────────────────────────────────────────────────────────────────────── */

    /**
     * The heads-up notice [app.materialclock.data.AlarmSettings.upcomingNotificationMinutes]
     * schedules ahead of the ring — see [AlarmReceiver.upcoming]. Auto-cancelling and non-ongoing,
     * unlike the ringer: this is only ever a notice, never the thing standing between the user and
     * turning the alarm off.
     */
    fun showUpcoming(context: Context, alarm: app.materialclock.core.Alarm) {
        post(
            context,
            ID_UPCOMING_BASE + alarm.id.toInt(),
            buildUpcoming(context, alarm),
        )
    }

    private fun buildUpcoming(
        context: Context,
        alarm: app.materialclock.core.Alarm,
    ): android.app.Notification {
        val is24 = android.text.format.DateFormat.is24HourFormat(context)

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

        return NotificationCompat.Builder(context, CHANNEL_UPCOMING)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText("Tap to review it")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp(context, TAB_ALARMS))
            .build()
    }

    /* ── Timer ──────────────────────────────────────────────────────────────────────────────── */

    fun showTimer(context: Context, timer: ClockTimer) {
        post(
            context,
            ID_TIMER,
            buildTimer(context, timer),
        )
    }

    /** Split out of [showTimer] so [LiveUpdateService] can hand the same notification to `startForeground`. */
    fun buildTimer(
        context: Context,
        timer: ClockTimer,
    ): android.app.Notification {
        val running = timer.state == TimerState.RUNNING
        val now = SystemClock.elapsedRealtime()
        val remaining = timer.remaining(now)

        val b = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(timer.label.ifBlank { "Timer" })
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openApp(context, TAB_TIMERS))
            // A no-op below API 36 (androidx.core 1.17+ handles the version split), and what
            // actually reaches the Now Bar / status-bar chip on it — see the class doc.
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
                // One segment the length of the whole timer; the filled point is how much of it
                // has elapsed. [LiveUpdateService] is what keeps this moving every second instead
                // of it being a snapshot — see that class's doc.
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
            // The platform ticks this on its own between our once-a-second reposts, so it never
            // looks stale even at the edges of that cadence. `setContentText` is set too, even
            // though it duplicates the chronometer: testing showed the tap-to-expand Now Bar
            // popup renders contentText but not the chronometer field, so leaving it unset there
            // was why that surface showed the title and actions but no time at all.
            //
            // Subtitle format ("10 m / 2:05 pm") matches Samsung Clock's own timer card: the
            // duration originally set, and the wall-clock time the timer will actually go off --
            // more useful at a glance than a bare "left" countdown, since the chronometer digits
            // above already show that.
            val is24h = android.text.format.DateFormat.is24HourFormat(context)

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

    /* ── Stopwatch ──────────────────────────────────────────────────────────────────────────── */

    fun showStopwatch(context: Context, sw: Stopwatch) {
        post(
            context,
            ID_STOPWATCH,
            buildStopwatch(context, sw),
        )
    }

    /** Split out of [showStopwatch] so [LiveUpdateService] can hand the same notification to `startForeground`. */
    fun buildStopwatch(
        context: Context,
        sw: Stopwatch,
    ): android.app.Notification {
        val elapsed = sw.elapsed(SystemClock.elapsedRealtime())

        val b = NotificationCompat.Builder(context, CHANNEL_STOPWATCH)
            .setSmallIcon(R.drawable.ic_stat_stopwatch)
            .setContentTitle("Stopwatch")
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openApp(context, TAB_STOPWATCH))
            // See buildTimer for why this is safe pre-36. Standard style alone (the compat
            // default, with no .setStyle call) is a documented promotable style, so this was left
            // without a ProgressStyle at first — a stopwatch has no total to be a fraction of.
            // Testing showed otherwise: Timer (which has ProgressStyle) reached the Now Bar and
            // Stopwatch (identical promoted flags, no ProgressStyle) did not. So this is
            // a segment representing the *current minute* rather than the open-ended total — an
            // indicator that fills and resets every 60 s, not a "percent complete" that doesn't
            // exist for a stopwatch — purely so the style itself matches Timer's.
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
            // Counting up: `when` is the instant it started, which the platform subtracts from now.
            // `setContentText`: see buildTimer's comment on why this duplicates the chronometer.
            // The latest lap, since a lap is the one thing about a running stopwatch worth a line
            // of its own; before this, laps were tracked in-app but never surfaced here at all.
            //
            // When there's no lap to show, centiseconds ("mm:ss.cs", matching Samsung Clock's own
            // stopwatch card) replace the plain elapsed time -- the chronometer digits above only
            // tick whole seconds, so this is the one place on the card finer resolution is visible
            // at all. It is a snapshot from this repost, not a live tween: [LiveUpdateService]
            // reposts once a second, so the hundredths hold still between ticks rather than
            // spinning continuously the way a real stopwatch face would.
            val lapText = sw.laps.firstOrNull()?.let {
                "Lap ${it.index} · ${it.split.clockFormat(withHours = true)}"
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
                .setContentText(
                    lapText ?: elapsedWithCentis
                )
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

    /* ── Plumbing ───────────────────────────────────────────────────────────────────────────── */

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
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
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
            Intent(
                context,
                ClockActionReceiver::class.java,
            )
                .setAction(action)
                .putExtra(
                    AlarmReceiver.EXTRA_ID,
                    id,
                ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Posts, unless the user said no.
     *
     * `POST_NOTIFICATIONS` is a runtime permission from API 33 and every one of these calls sits
     * on a path that must not throw, whether a receiver, a service or a coroutine with no UI.
     * Checking here once is cheaper than a try/catch at every call site and it fails the way
     * it should: the alarm still rings, it is just not announced.
     */
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