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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.SizeF
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import app.materialclock.MainActivity
import app.materialclock.alarm.Notifications
import app.materialclock.ui.Tab
import app.materialclock.R
import app.materialclock.core.Alarm
import app.materialclock.data.ClockSettings
import app.materialclock.data.ClockStore
import app.materialclock.data.WidgetBackground
import app.materialclock.data.WidgetConfig
import app.materialclock.data.order
import java.io.ByteArrayOutputStream
import java.text.DateFormatSymbols
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Draws the Alarm widget: the next alarm's time, the weekdays it repeats on (a dot over each
 * active day), and a round button that turns it on or off.
 *
 * The whole picture is drawn into one bitmap, so the same code makes all four sizes: 2x1 and 4x1
 * (one row), 2x2 (square) and 4x2 (wide and tall). Only the position of things changes.
 * Colours are Material 3: `primaryContainer` for the card, `primary` for the button when the
 * alarm is on.
 */
object AlarmWidgetRenderer {

    private const val ONE_ROW_MAX_HEIGHT_DP = 100f
    private const val WIDE_MIN_WIDTH_DP = 270f

    private class Scene(val bitmap: Bitmap, val button: RectF)

    suspend fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) =
        WidgetRefresh.serialized(appWidgetId) {
        val store = ClockStore(context)
        val settings = store.settingsNow()
        val config = store.widgetConfigNow(appWidgetId) ?: WidgetConfig()
        val alarms = store.alarmsNow()
        // The alarm chosen in this widget's settings; if none was chosen (or it was deleted), the
        // next one to ring.
        val alarm = alarms.firstOrNull { it.id == config.alarmId } ?: pick(alarms)
        val options = WidgetRefresh.optionsFor(context, manager, appWidgetId)
        WidgetRefresh.push(context, manager, appWidgetId) { build(context, settings, config, alarm, options) }
    }

    /** The alarm that rings next; if none is switched on, the earliest one of the day (shown as off). */
    private fun pick(alarms: List<Alarm>): Alarm? {
        val now = ZonedDateTime.now()
        val next = alarms
            .filter { it.enabled }
            .mapNotNull { a -> a.nextFire(now)?.let { a to it } }
            .minByOrNull { it.second }
            ?.first
        return next ?: alarms.minByOrNull { it.time }
    }

    private fun build(context: Context, settings: ClockSettings, config: WidgetConfig, alarm: Alarm?, options: Bundle): RemoteViews {
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
            val map = sizes.associateWith { size -> one(context, settings, config, alarm, px(size.width), px(size.height)) }
            return RemoteViews(map)
        }

        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minW)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minH)
        val portrait = one(context, settings, config, alarm, px(minW.toFloat()), px(maxH.toFloat()))
        val landscape = one(context, settings, config, alarm, px(maxW.toFloat()), px(minH.toFloat()))
        return RemoteViews(landscape, portrait)
    }

    /** One size, kept a little inside its cell (see [WidgetInset]). */
    private fun one(context: Context, settings: ClockSettings, config: WidgetConfig, alarm: Alarm?, w: Int, h: Int): RemoteViews {
        val m = WidgetInset.px(context)
        val views = oneInner(context, settings, config, alarm, (w - 2 * m).coerceAtLeast(1), (h - 2 * m).coerceAtLeast(1))
        views.setViewPadding(android.R.id.background, m, m, m, m)
        return views
    }

    private fun oneInner(context: Context, settings: ClockSettings, config: WidgetConfig, alarm: Alarm?, w: Int, h: Int): RemoteViews {
        val scene = draw(context, settings, config, alarm, w, h)
        val views = RemoteViews(context.packageName, R.layout.widget_alarm)
        views.setImageViewIcon(R.id.alarm_art, scene.bitmap.toIcon())

        // An invisible tap area exactly over the round button.
        val b = scene.button
        views.setViewPadding(
            R.id.alarm_hit,
            b.left.roundToInt().coerceAtLeast(0),
            b.top.roundToInt().coerceAtLeast(0),
            (w - b.right).roundToInt().coerceAtLeast(0),
            (h - b.bottom).roundToInt().coerceAtLeast(0),
        )

        val openApp = PendingIntent.getActivity(
            context,
            2,
            // Opens the app on the Alarms tab, which is where an alarm gets added.
            Intent(context, MainActivity::class.java).putExtra(Notifications.EXTRA_TAB, Tab.ALARMS.key),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (alarm != null) {
            val toggle = Intent(context, AlarmWidgetToggleReceiver::class.java)
                .setAction(AlarmWidgetToggleReceiver.ACTION_TOGGLE)
                .setData(Uri.parse("alarm-widget://toggle/${alarm.id}"))
                .putExtra(AlarmWidgetToggleReceiver.EXTRA_ID, alarm.id)
            val toggleIntent = PendingIntent.getBroadcast(
                context,
                0,
                toggle,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            // A tap anywhere on the widget switches the alarm. It never opens the app.
            views.setOnClickPendingIntent(android.R.id.background, toggleIntent)
            views.setOnClickPendingIntent(R.id.alarm_toggle, toggleIntent)
            views.setContentDescription(
                R.id.alarm_toggle,
                if (alarm.enabled) "Turn alarm off" else "Turn alarm on",
            )
        } else {
            // Nothing to switch yet: open the app, so an alarm can be added.
            views.setOnClickPendingIntent(android.R.id.background, openApp)
            views.setOnClickPendingIntent(R.id.alarm_toggle, openApp)
            views.setContentDescription(R.id.alarm_toggle, "Open alarms")
        }
        return views
    }

    private fun draw(context: Context, settings: ClockSettings, config: WidgetConfig, alarm: Alarm?, w: Int, h: Int): Scene {
        val d = context.resources.displayMetrics.density
        val wf = w.toFloat()
        val hf = h.toFloat()
        val oneRow = hf / d < ONE_ROW_MAX_HEIGHT_DP
        val wide = wf / d >= WIDE_MIN_WIDTH_DP
        // How wide the one-row widget is compared with its height decides the arrangement.
        val ratio = wf / hf
        val square = !oneRow && wf < 1.3f * hf
        val is24 = DateFormat.is24HourFormat(context)
        val on = alarm?.enabled == true

        val scheme = WidgetColours.scheme(context, config, settings)
        val filled = config.background == WidgetBackground.FILLED
        val bg = scheme.primaryContainer.toArgb()
        // With no card the text sits on the wallpaper, so it uses the plain text colour.
        val fg = if (filled) scheme.onPrimaryContainer.toArgb() else scheme.onSurface.toArgb()
        fun alpha(color: Int, a: Float) = ColorUtils.setAlphaComponent(color, (255 * a).roundToInt())
        val textColour = if (on || alarm == null) fg else alpha(fg, 0.55f)

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val radius = if (oneRow) hf / 2f else min(36f * d, min(wf, hf) / 2f)
        if (filled) {
            c.drawRoundRect(RectF(0f, 0f, wf, hf), radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg })
        }

        val typeface = ResourcesCompat.getFont(context, R.font.google_sans_flex)
        fun paint(color: Int, size: Float, weight: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            this.typeface = typeface
            fontVariationSettings = "'wght' $weight"
        }

        // ---- What to say ----
        val time = alarm?.time
        val timeStr = when {
            time == null -> "--:--"
            is24 -> "%02d:%02d".format(time.hour, time.minute)
            else -> "%d:%02d".format((time.hour % 12).takeIf { it != 0 } ?: 12, time.minute)
        }
        val ampm = if (time == null || is24) {
            ""
        } else {
            DateFormatSymbols.getInstance(Locale.getDefault()).amPmStrings[if (time.hour < 12) 0 else 1]
        }

        val order = settings.alarms.weekStart.order(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        val letters = order.map { it.getDisplayName(TextStyle.NARROW, Locale.getDefault()) }
        val active = order.map { alarm != null && it in alarm.days }

        // ---- Pieces ----
        fun drawDays(startX: Float, cell: Float, baseline: Float, size: Float) {
            for (i in 0..6) {
                val cx = startX + cell * (i + 0.5f)
                val isOn = active[i]
                val col = when {
                    isOn && on -> fg
                    isOn -> alpha(fg, 0.55f)
                    else -> alpha(fg, if (on) 0.45f else 0.3f)
                }
                val p = paint(col, size, 600)
                c.drawText(letters[i], cx - p.measureText(letters[i]) / 2f, baseline, p)
                if (isOn) {
                    c.drawCircle(cx, baseline - size * 1.25f, size * 0.12f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col })
                }
            }
        }

        fun drawButton(cx: Float, cy: Float, dia: Float): RectF {
            val r = dia / 2f
            val fill = if (on) scheme.primary.toArgb() else alpha(fg, 0.14f)
            c.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
            ContextCompat.getDrawable(context, R.drawable.ic_widget_alarm)?.mutate()?.let { icon ->
                icon.setTint(if (on) scheme.onPrimary.toArgb() else alpha(fg, 0.7f))
                val inset = dia * 0.26f
                icon.setBounds(
                    (cx - r + inset).roundToInt(), (cy - r + inset).roundToInt(),
                    (cx + r - inset).roundToInt(), (cy + r - inset).roundToInt(),
                )
                icon.draw(c)
            }
            if (alarm != null && !on) {
                // A slash through the bell: this alarm is off. The dimmer colours alone were easy
                // to miss.
                val slash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = alpha(fg, 0.75f)
                    style = Paint.Style.STROKE
                    strokeWidth = dia * 0.075f
                    strokeCap = Paint.Cap.ROUND
                }
                val k = r * 0.52f
                c.drawLine(cx - k, cy - k, cx + k, cy + k, slash)
            }
            return RectF(cx - r, cy - r, cx + r, cy + r)
        }

        /** Time, with AM/PM small beside it, starting at [x] on [baseline]. */
        fun drawTimeInline(x: Float, baseline: Float, size: Float) {
            val tp = paint(textColour, size, 600)
            c.drawText(timeStr, x, baseline, tp)
            if (ampm.isNotEmpty()) {
                c.drawText(ampm, x + tp.measureText(timeStr) + size * 0.10f, baseline, paint(textColour, size * 0.46f, 500))
            }
        }

        fun inlineWidth(size: Float): Float {
            val tw = paint(textColour, size, 600).measureText(timeStr)
            val aw = if (ampm.isEmpty()) 0f else size * 0.10f + paint(textColour, size * 0.46f, 500).measureText(ampm)
            return tw + aw
        }

        // ---- Four layouts ----
        val button: RectF
        when {
            // 2x1: button, then time with AM/PM underneath.
            oneRow && ratio < 3.0f -> {
                val dia = hf * 0.70f
                val left = hf * 0.15f
                button = drawButton(left + dia / 2f, hf / 2f, dia)
                val x0 = left + dia + hf * 0.14f
                val avail = wf - x0 - hf * 0.16f
                val ts = min(hf * 0.38f, avail / 2.8f)
                val cap = ts * 0.72f
                val acap = ts * 0.46f * 0.72f
                val gap = ts * 0.14f
                val total = cap + if (ampm.isEmpty()) 0f else gap + acap
                val top = (hf - total) / 2f
                c.drawText(timeStr, x0, top + cap, paint(textColour, ts, 600))
                if (ampm.isNotEmpty()) c.drawText(ampm, x0, top + total, paint(textColour, ts * 0.46f, 500))
            }

            // 4x1 and wider: button, time, and the weekdays on the right. The weekdays only get
            // the room the time leaves them, so the two can never overlap.
            oneRow && ratio >= 5.2f -> {
                val dia = hf * 0.70f
                val left = hf * 0.15f
                button = drawButton(left + dia / 2f, hf / 2f, dia)
                val x0 = left + dia + hf * 0.16f
                val ts = hf * 0.36f
                drawTimeInline(x0, hf / 2f + ts * 0.72f / 2f, ts)

                val timeEnd = x0 + inlineWidth(ts)
                val rightEdge = wf - hf * 0.20f
                val avail = rightEdge - (timeEnd + hf * 0.20f)
                val cell = min(hf * 0.30f * 1.9f, avail / 7f)
                val ls = min(hf * 0.30f, cell * 0.85f)
                if (ls > hf * 0.12f) drawDays(rightEdge - cell * 7f, cell, hf / 2f + ls * 0.4f, ls)
            }

            // 3x1: wide but not wide enough for time and weekdays side by side, so the weekdays
            // go under the time. Both come out bigger than when they share one line.
            oneRow -> {
                val dia = hf * 0.70f
                val left = hf * 0.15f
                button = drawButton(left + dia / 2f, hf / 2f, dia)
                val x0 = left + dia + hf * 0.16f
                val ts = hf * 0.42f
                val cap = ts * 0.72f
                val availW = wf - x0 - hf * 0.20f
                val cell = min(hf * 0.26f * 1.9f, availW / 7f)
                val ls = cell / 1.9f
                val total = cap + ts * 0.10f + ls * 1.37f
                val timeBase = (hf - total) / 2f + cap
                drawTimeInline(x0, timeBase, ts)
                drawDays(x0, cell, timeBase + ts * 0.10f + ls * 1.37f, ls)
            }

            // 2x2: time on top, weekdays under it, button at the bottom.
            square -> {
                val ts = min(wf * 0.84f / (if (is24) 2.9f else 3.5f), hf * 0.24f)
                val cap = ts * 0.72f
                val cy = hf * 0.27f
                drawTimeInline((wf - inlineWidth(ts)) / 2f, cy + cap / 2f, ts)
                val ls = ts * 0.46f
                val cell = min(ls * 2.0f, wf * 0.86f / 7f)
                val dotsY = cy + cap / 2f + ts * 0.30f
                drawDays((wf - cell * 7f) / 2f, cell, dotsY + ls * 1.25f, ls)
                button = drawButton(wf / 2f, hf * 0.77f, min(hf * 0.34f, wf * 0.44f))
            }

            // 4x2: label top-left, time and weekdays bottom-left, button bottom-right.
            else -> {
                val pad = min(16f * d, hf * 0.14f)
                val x0 = pad * 1.5f
                val label = alarm?.label?.takeIf { it.isNotBlank() } ?: "Alarm"
                val lblSize = hf * 0.17f
                c.drawText(label, x0, pad * 1.1f + lblSize * 0.8f, paint(alpha(fg, 0.8f), lblSize, 500))

                val ts = min(hf * 0.30f, wf * 0.52f / (if (is24) 2.9f else 3.5f))
                val ls = ts * 0.46f
                val lettersBase = hf - pad * 1.1f
                val dotsY = lettersBase - ls * 1.25f
                val timeBase = dotsY - ls * 0.9f - ts * 0.12f
                drawTimeInline(x0, timeBase, ts)
                val cell = min(ls * 2.1f, wf * 0.5f / 7f)
                drawDays(x0, cell, lettersBase, ls)

                val dia = hf * 0.42f
                button = drawButton(wf - pad * 1.3f - dia / 2f, hf - pad * 1.1f - dia / 2f, dia)
            }
        }
        return Scene(bmp, button)
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
