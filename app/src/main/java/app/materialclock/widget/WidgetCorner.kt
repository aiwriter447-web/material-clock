package app.materialclock.widget

import kotlin.math.min

/**
 * The corner of a widget's card. One row tall: a capsule. Taller: a gentle rounded corner, a
 * little tighter on a wide card (4x2) than on a squarer one (2x2), so every widget of the family
 * has the same family of shapes.
 */
object WidgetCorner {
    fun radius(w: Float, h: Float, oneRow: Boolean): Float =
        if (oneRow) h / 2f else (if (w / h > 1.8f) 0.15f else 0.19f) * min(w, h)
}
