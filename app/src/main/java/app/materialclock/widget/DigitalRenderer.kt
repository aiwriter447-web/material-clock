package app.materialclock.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import app.materialclock.MainActivity
import app.materialclock.R
import app.materialclock.data.ClockSettings
import app.materialclock.data.WidgetBackground
import app.materialclock.data.WidgetConfig
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Builds the Digital clock widget.
 *
 * Same idea as the analog one: the launcher's own `TextClock` views show and update the time, so
 * this app does no periodic work. We only draw the rounded background and decide sizes.
 *
 * Everything that depends on the widget's size lives in [layoutFor], which is used by both the
 * real widget and the preview in the settings screen, so the two always agree.
 */
object DigitalRenderer {

    /** Height (dp) from which the date is shown: a two-row widget and taller. */
    private const val DATE_MIN_HEIGHT_DP = 100f

    /** Width (dp) from which the long date ("Tuesday, 13 October") is used. */
    private const val LONG_DATE_MIN_WIDTH_DP = 200f

    /** A one-row widget at least this wide (dp) puts the date beside the time. */
    private const val ROW_MIN_WIDTH_DP = 200f

    private class Layout(
        val timePx: Float,
        val ampmPx: Float,
        val gapPx: Float,
        val datePx: Float,
        val showDate: Boolean,
        val longDate: Boolean,
        val rowLayout: Boolean,
        val is24: Boolean,
        val radiusPx: Float,
    )

    private fun layoutFor(context: Context, config: WidgetConfig, w: Int, h: Int): Layout {
        val d = context.resources.displayMetrics.density
        val hDp = h / d
        val wDp = w / d
        val oneRow = hDp < DATE_MIN_HEIGHT_DP
        val rowLayout = config.digitalDate && oneRow && wDp >= ROW_MIN_WIDTH_DP
        val showDate = config.digitalDate && (!oneRow || rowLayout)
        val is24 = DateFormat.is24HourFormat(context)

        // Rough width of the time string, in multiples of the text size: "12:45" is about 2.8.
        val timeWidthEm = (if (config.digitalSeconds) 4.6f else 2.95f) + (if (is24) 0f else 1.4f)
        val longRowDate = wDp >= 280f
        val byWidth = if (rowLayout) w * 0.54f / timeWidthEm else w * 0.88f / timeWidthEm
        val byHeight = if (rowLayout) h * 0.58f else h * (if (showDate) 0.44f else 0.56f)
        val time = min(byWidth, byHeight)
        // In the one-row layout the date has its own two lines beside the time.
        val dateRowPx = min(h * 0.32f, w * 0.34f / (if (longRowDate) 5.4f else 3.4f))

        // One-row widgets are pills; taller ones get a Material-style rounded card.
        val radius = if (hDp < DATE_MIN_HEIGHT_DP) h / 2f else min(36f * d, min(w, h) / 2f)

        return Layout(
            timePx = time,
            // AM/PM: a little bigger than before, with a clear gap after the time.
            ampmPx = time * 0.46f,
            gapPx = time * 0.10f,
            datePx = if (rowLayout) dateRowPx else time * 0.34f,
            showDate = showDate,
            longDate = if (rowLayout) longRowDate else wDp >= LONG_DATE_MIN_WIDTH_DP,
            rowLayout = rowLayout,
            is24 = is24,
            radiusPx = radius,
        )
    }

    private fun timePattern(config: WidgetConfig, is24: Boolean): String = when {
        is24 && config.digitalSeconds -> "HH:mm:ss"
        is24 -> "HH:mm"
        config.digitalSeconds -> "h:mm:ss"
        else -> "h:mm"
    }

    private fun datePattern(context: Context, longDate: Boolean): String =
        DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            if (longDate) "EEEEdMMMM" else "EEEdMMM",
        )

    /** The real widget, kept a little inside its cell (see [WidgetInset]). */
    fun build(
        context: Context,
        config: WidgetConfig,
        settings: ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        val m = WidgetInset.px(context)
        val views = buildInner(context, config, settings, (w - 2 * m).coerceAtLeast(1), (h - 2 * m).coerceAtLeast(1))
        views.setViewPadding(android.R.id.background, m, m, m, m)
        return views
    }

    private fun buildInner(
        context: Context,
        config: WidgetConfig,
        settings: ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        val colours = WidgetColours.resolve(context, config, settings)
        val l = layoutFor(context, config, w, h)
        val views = RemoteViews(
            context.packageName,
            if (l.rowLayout) R.layout.widget_digital_row else R.layout.widget_digital,
        )

        if (config.background == WidgetBackground.NONE) {
            // No card: the time sits straight on the wallpaper.
            views.setViewVisibility(R.id.digital_bg, View.GONE)
        } else {
            views.setViewVisibility(R.id.digital_bg, View.VISIBLE)
            views.setImageViewIcon(R.id.digital_bg, background(colours.face, w, h, l.radiusPx).toIcon())
        }

        // Time. 12-hour and 24-hour patterns are both set; the launcher picks by system setting.
        views.setCharSequence(R.id.digital_time, "setFormat12Hour", timePattern(config, false))
        views.setCharSequence(R.id.digital_time, "setFormat24Hour", timePattern(config, true))
        views.setTextColor(R.id.digital_time, colours.onFace)
        views.setTextViewTextSize(R.id.digital_time, TypedValue.COMPLEX_UNIT_PX, l.timePx)

        // AM / PM, small, beside the time. Hidden in 24-hour mode.
        if (l.is24) {
            views.setViewVisibility(R.id.digital_ampm, View.GONE)
        } else {
            views.setViewVisibility(R.id.digital_ampm, View.VISIBLE)
            views.setCharSequence(R.id.digital_ampm, "setFormat12Hour", "a")
            views.setCharSequence(R.id.digital_ampm, "setFormat24Hour", "a")
            views.setTextColor(R.id.digital_ampm, colours.onFace)
            views.setTextViewTextSize(R.id.digital_ampm, TypedValue.COMPLEX_UNIT_PX, l.ampmPx)
            // The gap between the time and AM/PM.
            views.setViewPadding(R.id.digital_ampm, l.gapPx.roundToInt(), 0, 0, 0)
        }

        // Date.
        if (l.showDate) {
            // Beside the time the date gets two lines ("Wed," over "14 Oct").
            val pattern = datePattern(context, l.longDate).let {
                if (l.rowLayout) it.replaceFirst(Regex("\\s+"), "'\n'") else it
            }
            views.setViewVisibility(R.id.digital_date, View.VISIBLE)
            views.setCharSequence(R.id.digital_date, "setFormat12Hour", pattern)
            views.setCharSequence(R.id.digital_date, "setFormat24Hour", pattern)
            views.setTextColor(R.id.digital_date, colours.accent)
            views.setTextViewTextSize(R.id.digital_date, TypedValue.COMPLEX_UNIT_PX, l.datePx)
        } else {
            views.setViewVisibility(R.id.digital_date, View.GONE)
        }

        views.setOnClickPendingIntent(
            android.R.id.background,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    /** A still picture for the settings screen, drawn with the same sizes as the widget. */
    fun preview(
        context: Context,
        config: WidgetConfig,
        settings: ClockSettings,
        w: Int,
        h: Int,
    ): Bitmap {
        val colours = WidgetColours.resolve(context, config, settings)
        val l = layoutFor(context, config, w, h)
        val bmp = if (config.background == WidgetBackground.NONE) {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } else {
            background(colours.face, w, h, l.radiusPx)
        }
        val canvas = Canvas(bmp)
        val typeface = ResourcesCompat.getFont(context, R.font.google_sans_flex)

        fun paint(color: Int, size: Float, weight: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            this.typeface = typeface
            fontVariationSettings = "'wght' $weight"
        }

        val now = Date()
        val timeText = SimpleDateFormat(timePattern(config, l.is24), Locale.getDefault()).format(now)
        val ampmText = if (l.is24) "" else SimpleDateFormat("a", Locale.getDefault()).format(now)

        val timePaint = paint(colours.onFace, l.timePx, 800)
        val ampmPaint = paint(colours.onFace, l.ampmPx, 800)
        val datePaint = paint(colours.accent, l.datePx, 700)

        val timeW = timePaint.measureText(timeText)
        val ampmW = if (ampmText.isEmpty()) 0f else ampmPaint.measureText(ampmText)
        val gap = if (ampmText.isEmpty()) 0f else l.gapPx
        val rowW = timeW + gap + ampmW
        val digitH = l.timePx * 0.72f   // cap height of digits, roughly

        val dateGap = if (l.showDate) l.datePx * 1.3f else 0f
        val blockH = digitH + dateGap
        val top = (h - blockH) / 2f

        val baseline = top + dateGap + digitH
        val x = (w - rowW) / 2f
        canvas.drawText(timeText, x, baseline, timePaint)
        if (ampmText.isNotEmpty()) canvas.drawText(ampmText, x + timeW + gap, baseline, ampmPaint)

        if (l.showDate) {
            val dateText = SimpleDateFormat(datePattern(context, l.longDate), Locale.getDefault()).format(now)
            val dw = datePaint.measureText(dateText)
            canvas.drawText(dateText, (w - dw) / 2f, top + l.datePx * 0.9f, datePaint)
        }
        return bmp
    }

    private fun background(color: Int, w: Int, h: Int, radius: Float): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        Canvas(bmp).drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), radius, radius, paint)
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
