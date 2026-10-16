package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
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
