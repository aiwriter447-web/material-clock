package app.materialclock.widget

import app.materialclock.data.AnalogLook

/**
 * The proportions of the three hands for each design, as fractions of Rmin.
 *
 * The classic dial uses Google's watch-face figures from [DialGeometry]. The finished designs have
 * their own: Ripple, Bold and Digit use short thick hands (a hand's length is how far its tip
 * reaches from the pivot, its width is its thickness), and Orbit's are the hands of the small
 * clock in its top complication, so they are measured against that small clock's radius.
 */
object LookGeometry {

    class Hands(
        val hourL: Float, val hourW: Float, val hourTail: Float,
        val minuteL: Float, val minuteW: Float, val minuteTail: Float,
        val secondL: Float, val secondW: Float, val secondTail: Float,
    )

    private val classic = Hands(
        DialGeometry.HOUR_LENGTH, DialGeometry.HOUR_WIDTH, DialGeometry.HOUR_TAIL,
        DialGeometry.MINUTE_LENGTH, DialGeometry.MINUTE_WIDTH, DialGeometry.MINUTE_TAIL,
        DialGeometry.SECOND_LENGTH, DialGeometry.SECOND_WIDTH, DialGeometry.SECOND_TAIL,
    )

    /** For the seconds, "length" is how far the dot sits from the centre and "width" its size. */
    private val ripple = Hands(
        0.46f, 0.16f, 0.08f,
        0.62f, 0.13f, 0.065f,
        0.80f, 0.14f, 0f,
    )

    private val bold = Hands(
        0.50f, 0.15f, 0.075f,
        0.70f, 0.12f, 0.06f,
        0.90f, 0.03f, 0.16f,
    )

    private val orbit = Hands(
        0.50f, 0.15f, 0.075f,
        0.82f, 0.12f, 0.06f,
        0.88f, 0.05f, 0.15f,
    )

    fun hands(look: AnalogLook): Hands = when (look) {
        AnalogLook.CLASSIC -> classic
        AnalogLook.RIPPLE -> ripple
        AnalogLook.BOLD, AnalogLook.DIGIT -> bold
        AnalogLook.ORBIT -> orbit
    }
}
