package app.materialclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import app.materialclock.core.Alarm
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Puts alarms into `AlarmManager` and takes them out again.
 *
 * ## Why `setAlarmClock` and not `setExactAndAllowWhileIdle`
 *
 * `setAlarmClock` is the only API that means "this is a user-visible alarm clock". It is exempt
 * from Doze and from app-standby buckets outright (no window, no batching, no deferral), and it
 * is the one that lights the alarm glyph in the status bar and feeds the system's "next alarm"
 * surfaces on the lock screen and in Assistant. `setExactAndAllowWhileIdle` gets you the timing
 * and none of the rest, and is rate-limited to roughly once every nine minutes per app, which a
 * snooze can breach.
 *
 * ## Permissions
 *
 * `USE_EXACT_ALARM` is declared and, because the app's core function genuinely is an alarm clock,
 * it is granted at install with no runtime prompt and no settings trip. `SCHEDULE_EXACT_ALARM` is
 * declared alongside it capped at API 32, where `USE_EXACT_ALARM` does not yet exist. The check in
 * [canScheduleExact] therefore only ever fails on 31–32 with the permission revoked.
 *
 * ## One PendingIntent per alarm
 *
 * The request code is the alarm's id, so rescheduling replaces rather than duplicates, and
 * cancelling needs nothing but the id. `FLAG_UPDATE_CURRENT` keeps the extras fresh when a
 * repeating alarm rolls to its next day.
 */
object AlarmScheduler {

    fun canScheduleExact(context: Context): Boolean {
        val am = context.getSystemService<AlarmManager>() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
    }

    fun schedule(context: Context, alarm: Alarm, upcomingMinutes: Int = 0, zone: ZoneId = ZoneId.systemDefault()) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val next = alarm.nextFire(ZonedDateTime.now(zone))
        if (next == null) {
            cancel(context, alarm.id)
            return
        }
        val at = next.toInstant().toEpochMilli()
        val fire = firePendingIntent(context, alarm.id)
        if (canScheduleExact(context)) {
            // The second intent is what the *system* opens when the user taps the status-bar alarm
            // chip. That is the app, not the ringer; passing the ringer here would let a tap
            // start it.
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), fire)
        } else {
            // Degraded but not silent: a window alarm still rings, just not to the second.
            am.setWindow(AlarmManager.RTC_WAKEUP, at, 60_000L, fire)
        }
        scheduleUpcoming(context, alarm, at, upcomingMinutes, zone)
    }

    fun scheduleAll(context: Context, alarms: List<Alarm>, upcomingMinutes: Int = 0) {
        alarms.forEach { schedule(context, it, upcomingMinutes) }
    }

    fun cancel(context: Context, id: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        am.cancel(firePendingIntent(context, id))
        am.cancel(upcomingPendingIntent(context, id))
    }

    /**
     * The heads-up "this alarm rings in N minutes" notice — [app.materialclock.data.AlarmSettings
     * .upcomingNotificationMinutes]. It is always re-derived from [at] rather than stored anywhere
     * of its own, the same reasoning [AlarmReceiver]'s class doc gives for the ring itself: nothing
     * has to remember a second absolute instant, only how far back from the first one to sit.
     *
     * A due-or-past notice time (an alarm inside the window, or the feature off at 0) cancels
     * rather than schedules — a "this rings in −4 minutes" notification helps no one.
     */
    private fun scheduleUpcoming(context: Context, alarm: Alarm, at: Long, upcomingMinutes: Int, zone: ZoneId) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val notifyAt = at - upcomingMinutes * 60_000L
        if (upcomingMinutes <= 0 || notifyAt <= System.currentTimeMillis()) {
            am.cancel(upcomingPendingIntent(context, alarm.id))
            return
        }
        val pi = upcomingPendingIntent(context, alarm.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, notifyAt, pi)
        }
    }

    private fun upcomingPendingIntent(context: Context, id: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            // A different request code namespace than [firePendingIntent]'s, so the two never
            // collide and cancelling one never touches the other.
            (id + UPCOMING_REQUEST_CODE_OFFSET).toInt(),
            Intent(context, AlarmReceiver::class.java)
                .setAction(AlarmReceiver.ACTION_UPCOMING)
                .putExtra(AlarmReceiver.EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun firePendingIntent(context: Context, id: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            id.toInt(),
            Intent(context, AlarmReceiver::class.java)
                .setAction(AlarmReceiver.ACTION_FIRE)
                .putExtra(AlarmReceiver.EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun showPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName)!!,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** Kept far above any real alarm id ([app.materialclock.data.ClockStore.nextId] starts at 100),
     * so `id + this` can never land on another alarm's own request code. */
    private const val UPCOMING_REQUEST_CODE_OFFSET = 1_000_000_000L
}