package app.materialclock.widget

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
import androidx.compose.ui.graphics.toArgb
import app.materialclock.MainActivity
import app.materialclock.R
import androidx.core.graphics.ColorUtils
import app.materialclock.data.AnalogLook
import app.materialclock.data.ClockStore
import app.materialclock.data.DateMode
import app.materialclock.data.DatePosition
import app.materialclock.data.HandStyle
import app.materialclock.data.WidgetConfig
import app.materialclock.data.WidgetPreset
import app.materialclock.data.WidgetType
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Builds and pushes the widget.
 *
 * The one thing worth knowing here: the payload goes over binder as **compressed bytes wrapped in
 * an `Icon`**, never as a `Bitmap`. `RemoteViews.estimateMemoryUsage()` counts only its bitmap
 * cache, which `setImageViewBitmap` fills and `setIcon` does not, so icons stay outside the
 * system's per-widget bitmap ceiling. A lossless WEBP of a flat-coloured dial is also one to two
 * orders of magnitude smaller than the raw ARGB it replaces.
 */
object ClockWidgetRenderer {

    suspend fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) =
        WidgetRefresh.serialized(appWidgetId) {
            val store = ClockStore(context)
            val config = store.widgetConfigNow(appWidgetId) ?: return@serialized
            val settings = store.settingsNow()
            // Read the size only now, when this draw's turn has come (see WidgetRefresh.serialized).
            val options = WidgetRefresh.optionsFor(context, manager, appWidgetId)

            WidgetRefresh.push(context, manager, appWidgetId) { build(context, config, settings, options) }

            // Orbit shows the battery level, which only stays right if the picture is redrawn now and then.
            if (config.look == AnalogLook.ORBIT) OrbitRefresh.schedule(context)
            Unit
        }

    /**
     * One `RemoteViews` per candidate size.
     *
     * From API 31 the host hands us the exact sizes it will use and picks between them itself, with
     * no round trip on rotation. Below that the portrait box is min-width × max-height and the
     * landscape box is max-width × min-height, combined with the old two-layout constructor. An
     * empty list, which the docs say is the launcher's prerogative, takes the same path.
     *
     * Every dp in that bundle is already padding-corrected by `AppWidgetHostView`, so it is the
     * content box: adding padding of our own would shrink the face away from the edges, which is
     * the one thing the shape fitting exists to prevent.
     */
    private fun build(
        context: Context,
        config: WidgetConfig,
        settings: app.materialclock.data.ClockSettings,
        options: Bundle,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        fun px(dp: Int) = (dp * density).roundToInt().coerceAtLeast(1)

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
                one(context, config, settings, px(size.width.roundToInt()), px(size.height.roundToInt()))
            }
            return RemoteViews(map)
        }

        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minW)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minH)
        val portrait = one(context, config, settings, px(minW), px(maxH))
        val landscape = one(context, config, settings, px(maxW), px(minH))
        return RemoteViews(landscape, portrait)
    }

    private fun one(
        context: Context,
        config: WidgetConfig,
        settings: app.materialclock.data.ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        // Digital widgets have their own layout and drawing; see DigitalRenderer. It keeps its
        // own margin.
        if (config.type == WidgetType.DIGITAL) {
            return DigitalRenderer.build(context, config, settings, w, h)
        }

        val m = WidgetInset.px(context)
        val d = context.resources.displayMetrics.density
        val pillW = (w - 2 * m).coerceAtLeast(1)
        val pillH = (h - 2 * m).coerceAtLeast(1)

        // One row and wide (2x1): a capsule with a small clock on the left and the date on the
        // right, kept a little inside its cell like other launchers' capsules.
        if (pillH / d < PILL_MAX_HEIGHT_DP && pillW >= 1.5f * pillH) {
            val views = pill(context, config, settings, pillW, pillH)
            views.setViewPadding(android.R.id.background, m, m, m, m)
            return views
        }

        // The shaped clock fills its whole cell: other clocks do, and a margin made ours visibly
        // smaller. The padding is set to zero rather than left alone, because a launcher may
        // re-use the previous views when the widget is resized, and a margin left over from the
        // capsule would push the hands off the dial.
        val views = oneInner(context, config, settings, w, h)
        views.setViewPadding(android.R.id.background, 0, 0, 0, 0)
        return views
    }

    /** The 2x1 capsule: a small clock on the left, the date (two lines) on the right. */
    private fun pill(
        context: Context,
        requested: WidgetConfig,
        settings: app.materialclock.data.ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        // Orbit needs room for four complications, which a capsule's small dial cannot give, so
        // there it shows the Ripple dial instead.
        val config = if (requested.look == AnalogLook.ORBIT) {
            WidgetPreset.RIPPLE.config.copy(colour = requested.colour, palette = requested.palette)
        } else {
            requested
        }
        val colours = WidgetColours.resolve(context, config, settings)
        val scheme = WidgetColours.scheme(context, config, settings)

        val side = (h * 0.84f).roundToInt()
        val top = ((h - side) / 2f).roundToInt()
        val left = top + (h * 0.06f).roundToInt()
        val face = FaceRenderer.render(context, config, colours, side, side)

        // Capsule and dial in one picture.
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawRoundRect(
            RectF(0f, 0f, w.toFloat(), h.toFloat()), h / 2f, h / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = scheme.surface.toArgb() },
        )
        canvas.drawBitmap(face.bitmap, left.toFloat(), top.toFloat(), null)

        val views = RemoteViews(context.packageName, R.layout.widget_clock)
        views.setImageViewIcon(R.id.face, bmp.toIcon())
        applyHands(views, config, colours, face)
        applyLook(context, views, config, colours, face, left.toFloat(), top.toFloat(), w, h)
        // The platform's hands must sit over the small dial only.
        views.setViewPadding(
            R.id.hands_slot,
            left, top,
            (w - left - side).coerceAtLeast(0),
            (h - top - side).coerceAtLeast(0),
        )

        // The date, two lines ("Wed," over "14 Oct"). A TextClock, so it rolls over at midnight
        // without this app doing anything. A wider capsule has room for the full date.
        val density = context.resources.displayMetrics.density
        val wide = w / density >= 200f
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), if (wide) "EEEEdMMMM" else "EEEdMMM")
            .replaceFirst(Regex("\\s+"), "'\n'")
        val textLeft = left + side + h * 0.16f
        val rightPad = h * 0.20f
        val availW = w - textLeft - rightPad

        // The longest line is "Wednesday," (about 10 characters) or "15 Oct" (about 6), and a
        // character is roughly 0.55 of the type size wide. The size is the smaller of what the
        // height and the room allow, so the text can never touch the capsule's end.
        val chars = if (wide) 10.5f else 6.2f
        val size = min(h * (if (wide) 0.32f else 0.27f), availW / (chars * 0.55f))
        views.setViewVisibility(R.id.date, View.VISIBLE)
        views.setCharSequence(R.id.date, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.date, "setFormat24Hour", pattern)
        views.setTextColor(R.id.date, colours.onFace)
        views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, size)
        views.setViewPadding(
            R.id.date_slot,
            textLeft.roundToInt(),
            ((h - size * 2.5f) / 2f).roundToInt().coerceAtLeast(0),
            0,
            0,
        )

        // Room left over on the right? Then the time goes there, instead of empty space.
        val textW = size * chars * 0.55f
        val leftover = availW - textW
        val is24 = DateFormat.is24HourFormat(context)
        val timeSize = min(h * 0.40f, leftover * 0.85f / (if (is24) 2.8f else 4.4f))
        val showTime = leftover >= h * 1.3f && timeSize >= h * 0.22f
        views.setViewVisibility(R.id.pill_time_slot, if (showTime) View.VISIBLE else View.GONE)
        if (showTime) {
            views.setViewPadding(R.id.pill_time_slot, 0, 0, rightPad.roundToInt(), 0)
            views.setCharSequence(R.id.pill_time, "setFormat12Hour", "h:mm a")
            views.setCharSequence(R.id.pill_time, "setFormat24Hour", "HH:mm")
            views.setTextColor(R.id.pill_time, colours.onFace)
            views.setTextViewTextSize(R.id.pill_time, TypedValue.COMPLEX_UNIT_PX, timeSize)
        }

        views.setOnClickPendingIntent(
            android.R.id.background,
            android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    private fun oneInner(
        context: Context,
        config: WidgetConfig,
        settings: app.materialclock.data.ClockSettings,
        w: Int,
        h: Int,
    ): RemoteViews {
        val colours = WidgetColours.resolve(context, config, settings)
        val face = FaceRenderer.render(context, config, colours, w, h)
        val views = RemoteViews(context.packageName, R.layout.widget_clock)

        views.setImageViewIcon(R.id.face, face.bitmap.toIcon())
        // Reset, for the reason given in one(): the capsule squeezes the hands into a corner.
        views.setViewPadding(R.id.hands_slot, 0, 0, 0, 0)
        views.setViewVisibility(R.id.pill_time_slot, View.GONE)

        applyHands(views, config, colours, face)
        applyLook(context, views, config, colours, face, 0f, 0f, w, h)

        applyDate(context, views, config, colours, face, w, h)

        views.setOnClickPendingIntent(
            android.R.id.background,
            android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        return views
    }

    /** The platform's own AnalogClock: dial blank, hands drawn by [HandRenderer]. */
    private fun applyHands(
        views: RemoteViews,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
    ) {
        val g = LookGeometry.hands(config.look)

        // The dial itself is empty: the face underneath is what the user sees, and AnalogClock
        // would otherwise draw its own default clock face over it.
        views.setIcon(R.id.hands, "setDial", HandRenderer.blank().toIcon())

        // The pin rides the topmost hand that is switched on, because nothing of ours can be drawn
        // above AnalogClock's own painting order.
        val topmost = when {
            config.secondHand != HandStyle.OFF -> 2
            config.minuteHand != HandStyle.OFF -> 1
            else -> 0
        }
        val pinR = if (config.centrePin) DialGeometry.PIN_DIAMETER else 0f

        views.setIcon(
            R.id.hands, "setHourHand",
            HandRenderer.render(
                config.hourHand, colours.hourHand, face.rMin,
                g.hourL, g.hourW, g.hourTail,
                if (topmost == 0) pinR else 0f, colours.pin,
            ).toIcon(),
        )
        views.setIcon(
            R.id.hands, "setMinuteHand",
            HandRenderer.render(
                config.minuteHand, colours.minuteHand, face.rMin,
                g.minuteL, g.minuteW, g.minuteTail,
                if (topmost == 1) pinR else 0f, colours.pin,
            ).toIcon(),
        )
        if (config.secondHand == HandStyle.OFF) {
            // Null is legal only for the second hand, and it is also what stops the host's 1 Hz
            // tick, which is the point. The hour and minute hands must be transparent icons
            // instead, because AnalogClock dereferences those without a null check.
            views.setIcon(R.id.hands, "setSecondHand", null)
        } else {
            views.setIcon(
                R.id.hands, "setSecondHand",
                HandRenderer.render(
                    config.secondHand, colours.secondHand, face.rMin,
                    g.secondL, g.secondW, g.secondTail,
                    if (topmost == 2) pinR else 0f, colours.pin,
                ).toIcon(),
            )
        }
    }

    /**
     * Everything that belongs to a finished design rather than to the plain dial.
     *
     * Every extra view is switched explicitly, on or off, on every draw. A launcher may re-use the
     * views it already has when the same widget is redrawn at another size or with another design,
     * so anything left unset would keep whatever the previous draw gave it.
     *
     * ([originX], [originY]) is where the face bitmap sits inside the widget, which is (0, 0)
     * except in the 2x1 capsule, and ([w], [h]) is the widget's own size.
     */
    private fun applyLook(
        context: Context,
        views: RemoteViews,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
        originX: Float,
        originY: Float,
        w: Int,
        h: Int,
    ) {
        /** Squeezes a full-size slot down to the square of half-width [half] around a point. */
        fun place(id: Int, cx: Float, cy: Float, half: Float) {
            views.setViewPadding(
                id,
                (originX + cx - half).roundToInt().coerceAtLeast(0),
                (originY + cy - half).roundToInt().coerceAtLeast(0),
                (w - (originX + cx + half)).roundToInt().coerceAtLeast(0),
                (h - (originY + cy + half)).roundToInt().coerceAtLeast(0),
            )
        }

        // Digit: the time as big numerals behind the hands.
        val digit = config.look == AnalogLook.DIGIT
        views.setViewVisibility(R.id.readout_slot, if (digit) View.VISIBLE else View.GONE)
        if (digit) {
            place(R.id.readout_slot, face.cx, face.cy, face.rMin)
            views.setCharSequence(R.id.readout, "setFormat12Hour", "hh'\n'mm")
            views.setCharSequence(R.id.readout, "setFormat24Hour", "HH'\n'mm")
            views.setTextColor(R.id.readout, colours.onFace)
            views.setTextViewTextSize(R.id.readout, TypedValue.COMPLEX_UNIT_PX, 0.50f * face.rMin)
        }

        // Orbit: the main hands give way to the small clock and the two text complications.
        val orbit = config.look == AnalogLook.ORBIT
        val orbitVisibility = if (orbit) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.hands_slot, if (orbit) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.orbit_hands_slot, orbitVisibility)
        views.setViewVisibility(R.id.orbit_time_slot, orbitVisibility)
        views.setViewVisibility(R.id.orbit_date_slot, orbitVisibility)
        if (!orbit) return

        val s = OrbitDesign.slots(face.cx, face.cy, face.rMin)

        // The small clock, in the disc on top.
        val half = s.top.r * 0.92f
        place(R.id.orbit_hands_slot, s.top.cx, s.top.cy, half)
        applyMiniHands(views, config, colours, half)

        // The time, in the circle on the right: "12 : 40" over "am", or just "13 : 05".
        place(R.id.orbit_time_slot, s.right.cx, s.right.cy, s.right.r)
        views.setCharSequence(R.id.orbit_time, "setFormat12Hour", "h:mm'\n'a")
        views.setCharSequence(R.id.orbit_time, "setFormat24Hour", "HH:mm")
        views.setTextColor(R.id.orbit_time, colours.orbitOnRight)
        views.setTextViewTextSize(R.id.orbit_time, TypedValue.COMPLEX_UNIT_PX, s.right.r * 0.62f)

        // The date, in the disc below: "Wed" over "14".
        place(R.id.orbit_date_slot, s.bottom.cx, s.bottom.cy, s.bottom.r)
        views.setCharSequence(R.id.orbit_date, "setFormat12Hour", "EEE'\n'd")
        views.setCharSequence(R.id.orbit_date, "setFormat24Hour", "EEE'\n'd")
        views.setTextColor(R.id.orbit_date, colours.orbitOnBottom)
        views.setTextViewTextSize(R.id.orbit_date, TypedValue.COMPLEX_UNIT_PX, s.bottom.r * 0.50f)
    }

    /** The three hands of Orbit's small clock, measured against that clock's own radius. */
    private fun applyMiniHands(
        views: RemoteViews,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        half: Float,
    ) {
        val g = LookGeometry.hands(AnalogLook.ORBIT)
        val dark = colours.orbitOnTop
        val soft = ColorUtils.blendARGB(dark, colours.orbitTop, 0.45f)
        val pinR = 0.12f

        views.setIcon(R.id.orbit_hands, "setDial", HandRenderer.blank().toIcon())
        views.setIcon(
            R.id.orbit_hands, "setHourHand",
            HandRenderer.render(
                config.hourHand, dark, half, g.hourL, g.hourW, g.hourTail,
                if (config.secondHand == HandStyle.OFF && config.minuteHand == HandStyle.OFF) pinR else 0f, dark,
            ).toIcon(),
        )
        views.setIcon(
            R.id.orbit_hands, "setMinuteHand",
            HandRenderer.render(
                config.minuteHand, soft, half, g.minuteL, g.minuteW, g.minuteTail,
                if (config.secondHand == HandStyle.OFF) pinR else 0f, dark,
            ).toIcon(),
        )
        if (config.secondHand == HandStyle.OFF) {
            views.setIcon(R.id.orbit_hands, "setSecondHand", null)
        } else {
            views.setIcon(
                R.id.orbit_hands, "setSecondHand",
                HandRenderer.render(
                    config.secondHand, dark, half, g.secondL, g.secondW, g.secondTail, pinR, dark,
                ).toIcon(),
            )
        }
    }

    private fun applyDate(
        context: Context,
        views: RemoteViews,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
        w: Int,
        h: Int,
    ) {
        if (config.date == DateMode.NONE) {
            views.setViewVisibility(R.id.date, View.GONE)
            return
        }
        views.setViewVisibility(R.id.date, View.VISIBLE)

        val skeleton = when (config.date) {
            DateMode.DAY -> "d"
            DateMode.DAY_WEEKDAY -> "EEEd"
            DateMode.FULL -> "EEEdMMM"
            DateMode.NONE -> "d"
        }
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
        // Both, with the same pattern: a date has no 12/24-hour form, and leaving one unset makes
        // the view fall back to its own default on devices set the other way.
        views.setCharSequence(R.id.date, "setFormat12Hour", pattern)
        views.setCharSequence(R.id.date, "setFormat24Hour", pattern)
        views.setTextColor(R.id.date, colours.minor)

        val sizePx = 0.14f * face.rMin
        views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, sizePx)

        // Placed with padding rather than margins: setViewLayoutMargin is API 31 and this app
        // supports 26.
        val angle = when (config.datePosition) {
            DatePosition.THREE -> 90f
            DatePosition.FOUR_THIRTY -> 135f
            DatePosition.SIX -> 180f
        }
        val (x, y) = if (config.date == DateMode.FULL) {
            w / 2f to face.cy + 0.42f * face.rMin
        } else {
            val a = Math.toRadians(angle.toDouble()).toFloat()
            face.cx + sin(a) * 0.55f * face.rMin to face.cy - cos(a) * 0.55f * face.rMin
        }
        views.setViewPadding(
            R.id.date_slot,
            (x - sizePx * 1.2f).roundToInt().coerceAtLeast(0),
            (y - sizePx * 0.7f).roundToInt().coerceAtLeast(0),
            0,
            0,
        )
    }

    /**
     * Lossless WEBP, wrapped as data rather than as a bitmap.
     *
     * `Icon.createWithData` keeps the payload out of `RemoteViews`' bitmap cache, which is what the
     * system's per-widget memory ceiling actually measures, and compresses a flat dial by one to
     * two orders of magnitude before it crosses binder.
     */
    private fun Bitmap.toIcon(): Icon {
        val out = ByteArrayOutputStream(BUFFER)
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

    private const val BUFFER = 64 * 1024

    /** Below this inner height (dp) a wide widget becomes the capsule. */
    private const val PILL_MAX_HEIGHT_DP = 100f
}