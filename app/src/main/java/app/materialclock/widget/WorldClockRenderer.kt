package app.materialclock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import app.materialclock.MainActivity
import app.materialclock.R
import app.materialclock.data.ClockSettings
import app.materialclock.data.ClockStore
import app.materialclock.data.WidgetConfig
import java.io.ByteArrayOutputStream
import java.time.ZoneId
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.first

/**
 * Draws the World Clock widget: two panels side by side (or one above the other when the widget
 * is close to square), your first city on the left and your second city on the right.
 *
 * Material 3 colours: the first panel uses `primaryContainer`, the second `primary`, so the
 * pair reads as one light and one dark half in any theme, always with enough contrast.
 */
object WorldClockRenderer {

    private const val DATE_MIN_PANEL_HEIGHT_DP = 90f
    private const val AMPM_MIN_PANEL_WIDTH_DP = 90f
    private const val ONE_ROW_MAX_HEIGHT_DP = 90f

    private class Side(val label: String, val zone: ZoneId)

    suspend fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val store = ClockStore(context)
        val settings = store.settingsNow()
        val cities = store.cities.first()
            .sortedWith(
                compareByDescending<app.materialclock.core.WorldCity> { it.pinnedAt != null }
                    .thenByDescending { it.pinnedAt ?: 0L }
                    .thenBy { it.city },
            )

        val home = settings.world.homeZoneOverride
            ?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: ZoneId.systemDefault()

        // Your first two cities from the World Clock tab (pinned ones first). If there are fewer
        // than two, the gap is filled: one city is shown next to Home, none shows Home and UTC.
        val picked = cities.take(2).map { Side(it.city, it.zone) }
        val left: Side
        val right: Side
        when (picked.size) {
            2 -> { left = picked[0]; right = picked[1] }
            1 -> { left = Side("Home", home); right = picked[0] }
            else -> { left = Side("Home", home); right = Side("UTC", ZoneId.of("UTC")) }
        }

        val options = manager.getAppWidgetOptions(appWidgetId)
        manager.updateAppWidget(appWidgetId, build(context, settings, left, right, options))
    }

    private fun build(
        context: Context,
        settings: ClockSettings,
        left: Side,
        right: Side,
        options: Bundle,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        fun px(dp: Float) = (dp * density).roundToInt().coerceAtLeast(1)

        val sizes: List<SizeF> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                ?.takeIf { it.isNotEmpty() }
                ?: emptyList()
        } else {
            emptyList()
        }

        if (sizes.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val map = sizes.associateWith { size ->
                one(context, settings, left, right, px(size.width), px(size.height))
            }
            return RemoteViews(map)
        }

        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minW)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minH)
        val portrait = one(context, settings, left, right, px(minW.toFloat()), px(maxH.toFloat()))
        val landscape = one(context, settings, left, right, px(maxW.toFloat()), px(minH.toFloat()))
        return RemoteViews(landscape, portrait)
    }

    private fun one(
        context: Context,
        settings: ClockSettings,
        left: Side,
        right: Side,
        w: Int,
        h: Int,
    ): RemoteViews {
        val d = context.resources.displayMetrics.density
        val horizontal = w >= 1.3f * h
        val panelW = if (horizontal) w / 2f else w.toFloat()
        val panelH = if (horizontal) h.toFloat() else h / 2f
        val pwDp = panelW / d
        val phDp = panelH / d

        val is24 = DateFormat.is24HourFormat(context)
        val showDate = phDp >= DATE_MIN_PANEL_HEIGHT_DP
        val showAmpm = !is24 && pwDp >= AMPM_MIN_PANEL_WIDTH_DP

        // Text sizes: the time is as big as the panel's width and height allow; city and date
        // hang off it in fixed proportions.
        val timeWidthEm = 2.8f + (if (showAmpm) 0.5f else 0f)
        val heightEm = 0.85f + 0.36f + (if (showDate) 0.34f else 0f)
        val timePx = min(panelW * 0.86f / timeWidthEm, panelH * 0.78f / heightEm)
        val cityPx = timePx * 0.30f
        val datePx = timePx * 0.26f
        val ampmPx = timePx * 0.36f

        val scheme = WidgetColours.scheme(context, WidgetConfig(), settings)
        val bgA = scheme.primaryContainer.toArgb()
        val onA = scheme.onPrimaryContainer.toArgb()
        val bgB = scheme.primary.toArgb()
        val onB = scheme.onPrimary.toArgb()

        val views = RemoteViews(
            context.packageName,
            if (horizontal) R.layout.widget_world_row else R.layout.widget_world_col,
        )

        val radius = if (h / d < ONE_ROW_MAX_HEIGHT_DP) h / 2f else min(28f * d, min(w, h) / 2f)
        views.setImageViewIcon(R.id.world_bg, background(bgA, bgB, w, h, radius, horizontal).toIcon())

        val datePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEdMMM")

        fun panel(side: Side, color: Int, city: Int, date: Int, time: Int, ampm: Int) {
            views.setTextViewText(city, side.label)
            views.setTextColor(city, color)
            views.setTextViewTextSize(city, TypedValue.COMPLEX_UNIT_PX, cityPx)

            views.setString(time, "setTimeZone", side.zone.id)
            views.setCharSequence(time, "setFormat12Hour", if (is24) "HH:mm" else "h:mm")
            views.setCharSequence(time, "setFormat24Hour", "HH:mm")
            views.setTextColor(time, color)
            views.setTextViewTextSize(time, TypedValue.COMPLEX_UNIT_PX, timePx)

            if (showAmpm) {
                views.setViewVisibility(ampm, View.VISIBLE)
                views.setString(ampm, "setTimeZone", side.zone.id)
                views.setCharSequence(ampm, "setFormat12Hour", "a")
                views.setCharSequence(ampm, "setFormat24Hour", "a")
                views.setTextColor(ampm, color)
                views.setTextViewTextSize(ampm, TypedValue.COMPLEX_UNIT_PX, ampmPx)
            } else {
                views.setViewVisibility(ampm, View.GONE)
            }

            if (showDate) {
                views.setViewVisibility(date, View.VISIBLE)
                views.setString(date, "setTimeZone", side.zone.id)
                views.setCharSequence(date, "setFormat12Hour", datePattern)
                views.setCharSequence(date, "setFormat24Hour", datePattern)
                views.setTextColor(date, color)
                views.setTextViewTextSize(date, TypedValue.COMPLEX_UNIT_PX, datePx)
            } else {
                views.setViewVisibility(date, View.GONE)
            }
        }
        panel(left, onA, R.id.p1_city, R.id.p1_date, R.id.p1_time, R.id.p1_ampm)
        panel(right, onB, R.id.p2_city, R.id.p2_date, R.id.p2_time, R.id.p2_ampm)

        views.setOnClickPendingIntent(
            android.R.id.background,
            PendingIntent.getActivity(
                context,
                1,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    /**
     * Two rounded halves in one rounded outline. The whole shape is drawn in the first colour,
     * then the second half is drawn over it with a plain rectangle clip, so the outer corners
     * stay smooth.
     */
    private fun background(a: Int, b: Int, w: Int, h: Int, radius: Float, horizontal: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = a })
        canvas.save()
        if (horizontal) canvas.clipRect(w / 2f, 0f, w.toFloat(), h.toFloat())
        else canvas.clipRect(0f, h / 2f, w.toFloat(), h.toFloat())
        canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = b })
        canvas.restore()
        return bmp
    }

    /** Lossless WEBP wrapped as data, same trick (and reason) as in ClockWidgetRenderer. */
    private fun Bitmap.toIcon(): Icon {
        val out = ByteArrayOutputStream(64 * 1024)
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        compress(format, 100, out)
        val bytes = out.toByteArray()
        return Icon.createWithData(bytes, 0, bytes.size)
    }
}
