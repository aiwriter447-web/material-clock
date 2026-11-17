package app.materialclock.widget

import android.graphics.RectF

/**
 * Where things sit on the Day dial and on Ripple's dial, as fractions of the dial's radius.
 *
 * The face renderer draws the badge into the dial bitmap, the launcher's TextClock puts the date
 * on top of it, and the editor's still picture draws the date itself. All three read the numbers
 * from here, so they cannot disagree about where the badge is.
 */
object DayDesign {

    /** The date badge: a rounded rectangle at three o'clock. */
    class Badge(val rect: RectF, val corner: Float, val cx: Float, val cy: Float)

    private const val BADGE_CENTRE = 0.66f
    private const val BADGE_WIDTH = 0.46f
    private const val BADGE_HEIGHT = 0.34f
    private const val BADGE_CORNER = 0.10f

    /** The size of the date number, against the dial's radius. */
    const val DAY_TEXT = 0.20f

    fun badge(cx: Float, cy: Float, r: Float): Badge {
        val bx = cx + BADGE_CENTRE * r
        val hw = BADGE_WIDTH * r / 2f
        val hh = BADGE_HEIGHT * r / 2f
        return Badge(RectF(bx - hw, cy - hh, bx + hw, cy + hh), BADGE_CORNER * r, bx, cy)
    }

    /** Ripple's date, "Sat 22": its size and how far above the centre it is centred. */
    const val RIPPLE_TEXT = 0.15f
    const val RIPPLE_HEIGHT = 0.58f
}
