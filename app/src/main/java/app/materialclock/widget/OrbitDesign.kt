package app.materialclock.widget

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.BatteryManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Orbit design: a round dial carrying four complications.
 *
 * ```
 *            [ small clock ]          top    (a scalloped disc, the hands live inside it)
 *   [battery ring]   [time]           left, right
 *            [    date    ]           bottom (a scalloped disc)
 * ```
 *
 * The discs, the ring and the battery figure are drawn once into the face bitmap by
 * [drawStatic]. The parts that change (the small clock's hands, the time and the date) are the
 * launcher's own views, placed over the matching [Slot] by the renderer, so they tick by
 * themselves like everything else in this app.
 */
object OrbitDesign {

    /** A circular area, in pixels. */
    class Slot(val cx: Float, val cy: Float, val r: Float)

    class Slots(val top: Slot, val bottom: Slot, val left: Slot, val right: Slot)

    /** Where the four areas go, for a dial centred at ([cx], [cy]) with radius [r]. */
    fun slots(cx: Float, cy: Float, r: Float) = Slots(
        top = Slot(cx, cy - 0.45f * r, 0.42f * r),
        bottom = Slot(cx, cy + 0.45f * r, 0.40f * r),
        left = Slot(cx - 0.60f * r, cy, 0.30f * r),
        right = Slot(cx + 0.62f * r, cy, 0.30f * r),
    )

    /** A disc with twelve very shallow scallops, filling [slot]. */
    fun scalloped(slot: Slot, out: Path = Path()): Path {
        val depth = 0.045f
        val r0 = slot.r / (1f + depth)
        val steps = 360
        out.rewind()
        for (i in 0..steps) {
            val t = (i * 2.0 * Math.PI / steps).toFloat()
            val r = r0 * (1f + depth * cos(12f * t))
            val x = slot.cx + r * sin(t)
            val y = slot.cy - r * cos(t)
            if (i == 0) out.moveTo(x, y) else out.lineTo(x, y)
        }
        out.close()
        return out
    }

    /** The battery level, 0 to 100. Read from the sticky broadcast, so no receiver is needed. */
    fun batteryPercent(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level < 0 || scale <= 0) return 0
        return (100f * level / scale).toInt().coerceIn(0, 100)
    }

    /** The parts of the design that do not move. */
    fun drawStatic(
        context: Context,
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        colours: FaceRenderer.Colours,
    ) {
        val s = slots(cx, cy, r)
        fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

        canvas.drawPath(scalloped(s.top), fill(colours.orbitTop))
        canvas.drawPath(scalloped(s.bottom), fill(colours.orbitBottom))
        canvas.drawCircle(s.right.cx, s.right.cy, s.right.r, fill(colours.orbitRight))

        // The battery: a grey ring with an arc of the accent colour for the charge, and the
        // figure in the hole.
        val pct = batteryPercent(context)
        val l = s.left
        canvas.drawCircle(l.cx, l.cy, l.r, fill(colours.orbitTrack))
        canvas.drawArc(
            RectF(l.cx - l.r, l.cy - l.r, l.cx + l.r, l.cy + l.r),
            -90f, 360f * pct / 100f, true, fill(colours.orbitRight),
        )
        canvas.drawCircle(l.cx, l.cy, l.r * 0.56f, fill(colours.face))

        val label = "$pct%"
        val paint = TextInk.numeralPaint(context, 100f, width = 100f, weight = 600).apply {
            color = colours.onFace
            textSize = l.r * 0.36f
        }
        val ink = TextInk.ink(paint, label)
        canvas.drawText(
            label,
            l.cx - ink.width() / 2f - ink.left,
            l.cy + ink.height() / 2f - ink.bottom,
            paint,
        )
    }
}
