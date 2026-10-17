package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import app.materialclock.MainActivity
import app.materialclock.R
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.util.SizeF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * Keeping the widgets' pictures in step with reality.
 *
 * A widget's picture is drawn for one size, so it is only right while the launcher agrees with the
 * size we were told. At the very moment a widget is added, that size is often not settled yet:
 * the launcher binds the widget, lets the settings screen run, and only then reports the real
 * size. A picture drawn in that gap is drawn for the wrong size and looks too big or squashed
 * until something redraws it. [again] is that redraw, done a moment later, on purpose.
 */
object WidgetRefresh {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val waiting: MutableSet<Int> = ConcurrentHashMap.newKeySet()
    private val locks = ConcurrentHashMap<Int, Mutex>()

    /**
     * Runs [block] for one widget with every other draw of the same widget waiting its turn.
     *
     * While a widget is being resized the launcher reports a new size many times a second, and
     * each report starts a draw. Run side by side, the draws finish in any order, and one that
     * started early (and so used an old size) can finish last and stay on screen: the widget then
     * shows a picture made for another size, stretched. One at a time, each draw reads the size
     * only when its turn comes, so the last draw to run always uses the newest size.
     */
    suspend fun <T> serialized(appWidgetId: Int, block: suspend () -> T): T =
        locks.getOrPut(appWidgetId) { Mutex() }.withLock { block() }

    /**
     * Sends a widget's picture to the launcher. If drawing it fails for any reason, a plain
     * visible card is sent instead, because a widget whose first draw failed would otherwise stay
     * empty and look as if it had vanished.
     */
    fun push(context: Context, manager: AppWidgetManager, appWidgetId: Int, build: () -> RemoteViews) {
        val views = try {
            build()
        } catch (t: Throwable) {
            Log.e("ClockWidget", "Drawing widget $appWidgetId failed", t)
            fallback(context)
        }
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun fallback(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_loading)
        views.setOnClickPendingIntent(
            android.R.id.background,
            android.app.PendingIntent.getActivity(
                context,
                3,
                android.content.Intent(context, MainActivity::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    /** Whether the launcher has told us this widget's size yet. */
    fun sizesKnown(options: Bundle): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
            if (!sizes.isNullOrEmpty()) return true
        }
        return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0) > 0
    }

    /**
     * The widget's size options, or, while the launcher has not reported them, the size the widget
     * was added at (its entry in the picker says how many cells), which is a much better first
     * guess than a fixed square. A second draw is then scheduled to put it right.
     */
    fun optionsFor(context: Context, manager: AppWidgetManager, appWidgetId: Int): Bundle {
        val options = manager.getAppWidgetOptions(appWidgetId)
        if (sizesKnown(options)) return options

        again(context, appWidgetId)
        val info = manager.getAppWidgetInfo(appWidgetId) ?: return options
        val density = context.resources.displayMetrics.density

        // A grid cell is about 75dp on most phones.
        val cellsW = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) info.targetCellWidth else 0
        val cellsH = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) info.targetCellHeight else 0
        val w = if (cellsW > 0) cellsW * 75 else (info.minWidth / density).roundToInt().coerceAtLeast(40)
        val h = if (cellsH > 0) cellsH * 75 else (info.minHeight / density).roundToInt().coerceAtLeast(40)
        return Bundle(options).apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, w)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, w)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, h)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, h)
        }
    }

    /** Draws this widget again shortly, twice, after the launcher has had time to settle. */
    fun again(context: Context, appWidgetId: Int, delaysMs: List<Long> = listOf(700L, 2500L)) {
        // Already waiting: ignore, so a draw that asks for another draw cannot loop.
        if (!waiting.add(appWidgetId)) return
        val app = context.applicationContext
        scope.launch {
            try {
                for (ms in delaysMs) {
                    delay(ms)
                    redraw(app, appWidgetId)
                }
            } finally {
                waiting.remove(appWidgetId)
            }
        }
    }

    private suspend fun redraw(app: Context, appWidgetId: Int) {
        val manager = AppWidgetManager.getInstance(app)
        when (WidgetProviders.kindOf(app, appWidgetId)) {
            WidgetKind.ANALOG, WidgetKind.DIGITAL -> ClockWidgetRenderer.update(app, manager, appWidgetId)
            WidgetKind.WORLD -> WorldClockRenderer.update(app, manager, appWidgetId)
            WidgetKind.ALARM -> AlarmWidgetRenderer.update(app, manager, appWidgetId)
            null -> Unit
        }
    }

    /** Redraws every widget of every kind. Used whenever the app is opened. */
    fun all(context: Context) {
        ClockWidgetProvider.refreshAll(context)
        WorldClockWidgetProvider.refreshAll(context)
        AlarmWidgetProvider.refreshAll(context)
    }
}
