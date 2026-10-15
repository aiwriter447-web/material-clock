package app.materialclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.widget.RemoteViews
import app.materialclock.MainActivity
import app.materialclock.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class AlarmWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = android.content.ComponentName(context, javaClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_alarm)

            // Intent to open MainActivity when widget is clicked
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Fetch Next Upcoming Alarm
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val nextAlarm = alarmManager?.nextAlarmClock

            if (nextAlarm != null) {
                val is24Hour = DateFormat.is24HourFormat(context)
                val timePattern = if (is24Hour) "HH:mm" else "h:mm a"
                val formattedTime = SimpleDateFormat(timePattern, Locale.getDefault())
                    .format(Date(nextAlarm.triggerTime))

                views.setTextViewText(R.id.text_alarm_time, formattedTime)
                views.setTextViewText(R.id.text_alarm_label, "Upcoming Alarm")
            } else {
                views.setTextViewText(R.id.text_alarm_time, "No alarm")
                views.setTextViewText(R.id.text_alarm_label, "No active alarms set")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class AlarmWidget2x1Provider : AlarmWidgetProvider()
class AlarmWidget2x2Provider : AlarmWidgetProvider()
class AlarmWidget4x1Provider : AlarmWidgetProvider()
class AlarmWidget4x2Provider : AlarmWidgetProvider()
