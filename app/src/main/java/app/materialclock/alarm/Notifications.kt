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
    const val CHANNEL_TIMER = "timer"
    const val CHANNEL_STOPWATCH = "stopwatch"

    const val ID_RINGING = 1
    const val ID_TIMER = 2
    const val ID_STOPWATCH = 3

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService<NotificationManager>() ?: return

        // No sound on the channel: the ringer plays its own looping audio, and a channel sound
        // would fire a second, one-shot copy over the top of it.
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALARM, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "A ringing alarm"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_TIMER, "Timers", NotificationManager.IMPORTANCE_LOW).apply {
                description = "A running timer"
                setSound(null, null)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_STOPWATCH, "Stopwatch", NotificationManager.IMPORTANCE_LOW).apply {
                description = "A running stopwatch"
                setSound(null, null)
            }
        )
    }

    /* ── Timer ──────────────────────────────────────────────────────────────────────────────── */

    fun showTimer(context: Context, timer: ClockTimer) {
        post(context, ID_TIMER, buildTimer(context, timer))
    }

    /** Split out of [showTimer] so [LiveUpdateService] can hand the same notification to `startForeground`. */
    fun buildTimer(context: Context, timer: ClockTimer): android.app.Notification {
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
                broadcast(context, ClockActionReceiver.ACTION_TIMER_TOGGLE, 20),
            )
            .addAction(0, "+1 min", broadcast(context, ClockActionReceiver.ACTION_TIMER_ADD, 21))
            .addAction(0, "Cancel", broadcast(context, ClockActionReceiver.ACTION_TIMER_CANCEL, 22))
            .setStyle(
                // One segment the length of the whole timer; the filled point is how much of it
                // has elapsed. [LiveUpdateService] is what keeps this moving every second instead
                // of it being a snapshot — see that class's doc.
                NotificationCompat.ProgressStyle()
                    .setProgressSegments(listOf(NotificationCompat.ProgressStyle.Segment(100)))
                    .setProgress(timer.fractionLeft(now).let { (100 - (it * 100)).toInt() }.coerceIn(0, 100)),
            )

        if (running) {
            // The platform ticks this on its own between our once-a-second reposts, so it never
            // looks stale even at the edges of that cadence. `setContentText` is set too, even
            // though it duplicates the chronometer: testing showed the tap-to-expand Now Bar
            // popup renders contentText but not the chronometer field, so leaving it unset there
            // was why that surface showed the title and actions but no time at all.
            b.setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(System.currentTimeMillis() + remaining.toMillis())
                .setShowWhen(true)
                .setContentText("${remaining.clockFormat()} left")
        } else {
            b.setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText("Paused · ${timer.pausedRemaining.clockFormat()}")
        }
        return b.build()
    }

    fun hideTimer(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_TIMER)
    }

    /* ── Stopwatch ──────────────────────────────────────────────────────────────────────────── */

    fun showStopwatch(context: Context, sw: Stopwatch) {
        post(context, ID_STOPWATCH, buildStopwatch(context, sw))
    }

    /** Split out of [showStopwatch] so [LiveUpdateService] can hand the same notification to `startForeground`. */
    fun buildStopwatch(context: Context, sw: Stopwatch): android.app.Notification {
        val elapsed = sw.elapsed(SystemClock.elapsedRealtime())
        val b = NotificationCompat.Builder(context, CHANNEL_STOPWATCH)
            .setSmallIcon(R.drawable.ic_stat_stopwatch)
            .setContentTitle("Stopwatch")
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openApp(context, TAB_STOPWATCH))
            // See buildTimer for why this is safe pre-36. No ProgressStyle here: a stopwatch has no
            // total to be a fraction of, so "Standard" style (the compat default) is the one of
            // the five promotable styles that actually fits an open-ended count.
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(elapsed.clockFormat(withHours = true))
            .addAction(
                0,
                if (sw.running) "Stop" else "Start",
                broadcast(context, ClockActionReceiver.ACTION_SW_TOGGLE, 30),
            )
            .addAction(
                0,
                if (sw.running) "Lap" else "Reset",
                broadcast(
                    context,
                    if (sw.running) ClockActionReceiver.ACTION_SW_LAP else ClockActionReceiver.ACTION_SW_RESET,
                    31,
                ),
            )

        if (sw.running) {
            // Counting up: `when` is the instant it started, which the platform subtracts from now.
            // `setContentText`: see buildTimer's comment on why this duplicates the chronometer.
            // The latest lap, since a lap is the one thing about a running stopwatch worth a line
            // of its own; before this, laps were tracked in-app but never surfaced here at all.
            val lapText = sw.laps.firstOrNull()?.let { "Lap ${it.index} · ${it.split.clockFormat(withHours = true)}" }
            b.setUsesChronometer(true)
                .setWhen(System.currentTimeMillis() - elapsed.toMillis())
                .setShowWhen(true)
                .setContentText(lapText ?: elapsed.clockFormat(withHours = true))
        } else {
            b.setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(sw.accumulated.clockFormat(withHours = true))
        }
        return b.build()
    }

    fun hideStopwatch(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_STOPWATCH)
    }

    /* ── Plumbing ───────────────────────────────────────────────────────────────────────────── */

    const val TAB_ALARMS = "alarms"
    const val TAB_TIMERS = "timers"
    const val TAB_STOPWATCH = "stopwatch"
    const val EXTRA_TAB = "tab"

    fun openApp(context: Context, tab: String): PendingIntent = PendingIntent.getActivity(
        context,
        tab.hashCode(),
        Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_TAB, tab),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun broadcast(context: Context, action: String, requestCode: Int, id: Long = 0L): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, ClockActionReceiver::class.java).setAction(action)
                .putExtra(AlarmReceiver.EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Posts, unless the user said no.
     *
     * `POST_NOTIFICATIONS` is a runtime permission from API 33 and every one of these calls sits
     * on a path that must not throw, whether a receiver, a service or a coroutine with no UI.
     * Checking here once is cheaper than a try/catch at every call site and it fails the way it
     * should: the alarm still rings, it is just not announced.
     */
    private fun post(context: Context, id: Int, n: android.app.Notification) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        runCatching { nm.notify(id, n) }
    }
}
