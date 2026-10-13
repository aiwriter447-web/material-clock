package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Alarm widget's presence in this app's process.
 *
 * It redraws when the widget is resized, when the clock, time zone or language changes, and
 * whenever the app saves its alarms ([ClockStore.putAlarms] calls [refreshAll]): editing an
 * alarm, switching one, or one ringing and rescheduling itself.
 */
class AlarmWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ids.forEach { AlarmWidgetRenderer.update(app, manager, it) }
            } finally {
                pending.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                AlarmWidgetRenderer.update(app, manager, appWidgetId)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> refreshAll(context)
        }
    }

    companion object {
        fun component(context: Context) = ComponentName(context, AlarmWidgetProvider::class.java)

        /** Redraw every placed Alarm widget. */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(component(app))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.Default).launch {
                ids.forEach { AlarmWidgetRenderer.update(app, manager, it) }
            }
        }
    }
}
