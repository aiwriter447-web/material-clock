package app.materialclock.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.format.DateFormat
import app.materialclock.data.AnalogLook
import app.materialclock.data.WidgetConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The moving parts of Digit and Orbit, drawn as a still picture at 10:09.
 *
 * On the home screen these are the launcher's own views (the Digit numerals are a TextClock, the
 * small clock in Orbit is an AnalogClock). The settings screen has no launcher to borrow, so it
 * draws them itself, here. See `renderStill` for why 10:09.
 */
object LookOverlay {

    /** Digit: the hours over the minutes, drawn before the hands so they sit behind them. */
    fun underHands(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
    ) {
        if (config.look != AnalogLook.DIGIT) return
        val size = 0.50f * face.rMin
        val paint = TextInk.numeralPaint(context, size, width = 100f, weight = 800).apply {
            color = colours.onFace
            textSize = size
        }
        val lines = listOf("10", "09")
        val cap = 0.73f * size
        val pitch = 1.05f * size
        val first = face.cy - pitch / 2f + cap / 2f
        lines.forEachIndexed { i, text ->
            val ink = TextInk.ink(paint, text)
            canvas.drawText(text, face.cx - ink.width() / 2f - ink.left, first + i * pitch, paint)
        }
    }

    /** Orbit: the small clock, the time and the date, drawn over the face. */
    fun orbitLive(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        colours: FaceRenderer.Colours,
        face: FaceRenderer.Face,
    ) {
        if (config.look != AnalogLook.ORBIT) return
        val s = OrbitDesign.slots(face.cx, face.cy, face.rMin)

        // The small clock.
        val half = s.top.r * 0.92f
        val g = LookGeometry.hands(AnalogLook.ORBIT)
        val minute = androidx.core.graphics.ColorUtils.blendARGB(colours.orbitOnTop, colours.orbitTop, 0.45f)
        fun hand(style: app.materialclock.data.HandStyle, color: Int, l: Float, w: Float, t: Float, angle: Float, pin: Float) {
            if (style == app.materialclock.data.HandStyle.OFF) return
            val bmp = HandRenderer.render(style, color, half, l, w, t, pin, colours.orbitOnTop)
            canvas.save()
            canvas.rotate(angle, s.top.cx, s.top.cy)
            canvas.drawBitmap(bmp, s.top.cx - bmp.width / 2f, s.top.cy - bmp.height / 2f, null)
            canvas.restore()
        }
        hand(config.hourHand, colours.orbitOnTop, g.hourL, g.hourW, g.hourTail, 304.5f, 0f)
        hand(config.minuteHand, minute, g.minuteL, g.minuteW, g.minuteTail, 54f, 0f)
        hand(config.secondHand, colours.orbitOnTop, g.secondL, g.secondW, g.secondTail, 210f, 0.12f)

        fun centred(text: String, cx: Float, cy: Float, paint: Paint) {
            val ink = TextInk.ink(paint, text)
            canvas.drawText(text, cx - ink.width() / 2f - ink.left, cy + ink.height() / 2f - ink.bottom, paint)
        }

        // The time, in the circle on the right.
        val timePaint = TextInk.numeralPaint(context, 100f, width = 100f, weight = 700).apply {
            color = colours.orbitOnRight
            textSize = s.right.r * 0.62f
        }
        val is24 = DateFormat.is24HourFormat(context)
        if (is24) {
            centred("10:09", s.right.cx, s.right.cy, timePaint)
        } else {
            centred("10:09", s.right.cx, s.right.cy - s.right.r * 0.26f, timePaint)
            centred("am", s.right.cx, s.right.cy + s.right.r * 0.36f, timePaint)
        }

        // The date, in the disc below.
        val datePaint = TextInk.numeralPaint(context, 100f, width = 100f, weight = 700).apply {
            color = colours.orbitOnBottom
            textSize = s.bottom.r * 0.50f
        }
        val now = Date()
        val weekday = SimpleDateFormat("EEE", Locale.getDefault()).format(now)
        val day = SimpleDateFormat("d", Locale.getDefault()).format(now)
        centred(weekday, s.bottom.cx, s.bottom.cy - s.bottom.r * 0.24f, datePaint)
        centred(day, s.bottom.cx, s.bottom.cy + s.bottom.r * 0.30f, datePaint)
    }
}
