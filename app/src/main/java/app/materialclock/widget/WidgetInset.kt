package app.materialclock.widget

import android.content.Context
import kotlin.math.roundToInt

/**
 * The margin kept free around every widget.
 *
 * A launcher hands a widget its whole grid cell. Well-made widgets (Samsung's, Google's) leave a
 * small gap inside it, so neighbouring widgets do not touch and every widget looks a little
 * smaller than its cell. Without this gap ours filled the cell edge to edge and looked bigger
 * than the rest.
 */
internal object WidgetInset {
    const val DP = 8f

    fun px(context: Context): Int = (DP * context.resources.displayMetrics.density).roundToInt()
}
