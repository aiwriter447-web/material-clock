package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.materialclock.data.ClockStore
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
open class AlarmWidgetProvider : AppWidgetProvider() {

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

    /** Also the cancelled-add path: either way, this widget's settings are no longer wanted. */
    override fun onDeleted(context: Context, ids: IntArray) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ClockStore(app).deleteWidgetConfigs(ids)
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
        // The launcher may finish reporting the new size a moment later; draw once more then.
        WidgetRefresh.again(app, appWidgetId, listOf(500L, 1500L))
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
        /**
         * Like [refreshAll], but does not return until every widget has been redrawn.
         *
         * [refreshAll] starts the work and returns at once. That is fine while the app is open,
         * but a broadcast receiver (a widget tap, an alarm ringing) is allowed to finish, and
         * the process to be put to sleep, as soon as its own work returns, so a redraw that was
         * only *started* could be cut off and the widget would keep showing the old state. Code
         * that changes something a widget shows should wait for this one.
         */
        suspend fun refreshAllNow(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            WidgetProviders.ids(app, WidgetKind.ALARM).forEach { AlarmWidgetRenderer.update(app, manager, it) }
        }

        /** Redraw every placed widget of this kind, whatever size it was added at. */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = WidgetProviders.ids(app, WidgetKind.ALARM)
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.Default).launch {
                ids.forEach { AlarmWidgetRenderer.update(app, manager, it) }
            }
        }
    }
}
