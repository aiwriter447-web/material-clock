package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The World Clock widget's presence in this app's process.
 *
 * Like the analog widget there is no periodic update: the launcher's own `TextClock` views tick
 * the times. We only redraw when something the picture depends on changes: the widget's size,
 * the time zone or locale, a new app version, or the city list / theme (the app calls
 * [refreshAll] for those).
 */
open class WorldClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        background { ids.forEach { id -> WorldClockRenderer.update(context.applicationContext, manager, id) } }
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
        background { WorldClockRenderer.update(context.applicationContext, manager, appWidgetId) }
        // The launcher may finish reporting the new size a moment later; draw once more then.
        WidgetRefresh.again(context, appWidgetId, listOf(500L, 1500L))
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

    /** `finish()` must be reached on every path, or the receiver leaks. */
    private fun BroadcastReceiver.PendingResult?.safeFinish() {
        this?.finish()
    }

    private fun background(work: suspend () -> Unit) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                work()
            } finally {
                pending.safeFinish()
            }
        }
    }

    companion object {
        /** Redraw every placed widget of this kind, whatever size it was added at. */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = WidgetProviders.ids(app, WidgetKind.WORLD)
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.Default).launch {
                ids.forEach { WorldClockRenderer.update(app, manager, it) }
            }
        }
    }
}
