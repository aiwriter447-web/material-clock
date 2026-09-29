package app.materialclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import app.materialclock.MainActivity
import app.materialclock.core.Alarm
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Puts alarms into `AlarmManager` and takes them out again.
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
            // सिस्टम को एक स्टैंडर्ड ऐप लॉन्च इंटेंट दिया गया है ताकि स्टेटस बार आइकॉन इनेबल हो सके
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), fire)
        } else {
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

    private fun showPendingIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private const val UPCOMING_REQUEST_CODE_OFFSET = 1_000_000_000L
}
