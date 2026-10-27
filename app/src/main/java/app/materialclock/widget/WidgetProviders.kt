package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/** The four kinds of widget this app offers. */
enum class WidgetKind { ANALOG, DIGITAL, WORLD, ALARM }

/**
 * Every widget size is its own entry in the launcher's widget picker, so the picker can show a
 * proper preview for each one (2x1, 2x2, 4x1, 4x2) and group them under one name.
 *
 * They all do the same work as their parent: the real drawing depends only on the size the
 * launcher reports, never on which entry the widget came from. The only difference is the size
 * and the preview that each entry declares in its own XML file.
 *
 * Whenever the app needs "all the widgets of a kind" (to redraw them, or to know which saved
 * settings still belong to a live widget) it asks [WidgetProviders].
 */
object WidgetProviders {

    private val classes: Map<WidgetKind, List<Class<*>>> = mapOf(
        WidgetKind.ANALOG to listOf(
            AnalogClock2x1Provider::class.java,
            AnalogClock2x2Provider::class.java,
            AnalogClock4x1Provider::class.java,
        ),
        WidgetKind.DIGITAL to listOf(DigitalClock2x1Provider::class.java, DigitalClock2x2Provider::class.java, DigitalClock4x1Provider::class.java, DigitalClock4x2Provider::class.java),
        WidgetKind.WORLD to listOf(WorldClock2x1Provider::class.java, WorldClock2x2Provider::class.java, WorldClock4x1Provider::class.java, WorldClock4x2Provider::class.java),
        WidgetKind.ALARM to listOf(AlarmWidget2x1Provider::class.java, AlarmWidget2x2Provider::class.java, AlarmWidget4x1Provider::class.java, AlarmWidget4x2Provider::class.java),
    )

    fun components(context: Context, kind: WidgetKind): List<ComponentName> =
        classes.getValue(kind).map { ComponentName(context, it) }

    /** The ids of every placed widget of the given kinds. */
    fun ids(context: Context, vararg kinds: WidgetKind): IntArray {
        val manager = AppWidgetManager.getInstance(context)
        return kinds
            .flatMap { components(context, it) }
            .flatMap { manager.getAppWidgetIds(it).toList() }
            .toIntArray()
    }

    fun allIds(context: Context): IntArray = ids(context, *WidgetKind.entries.toTypedArray())

    /** Which kind a placed widget is, or null if it is not one of ours. */
    fun kindOf(context: Context, appWidgetId: Int): WidgetKind? {
        val provider = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)?.provider
            ?: return null
        return WidgetKind.entries.firstOrNull { kind -> components(context, kind).any { it == provider } }
    }
}

// One class per entry in the widget picker. Nothing to add: see the note above.
class AnalogClock2x1Provider : ClockWidgetProvider()
class AnalogClock2x2Provider : ClockWidgetProvider()
class AnalogClock4x1Provider : ClockWidgetProvider()
class DigitalClock2x1Provider : ClockWidgetProvider()
class DigitalClock2x2Provider : ClockWidgetProvider()
class DigitalClock4x1Provider : ClockWidgetProvider()
class DigitalClock4x2Provider : ClockWidgetProvider()
class WorldClock2x1Provider : WorldClockWidgetProvider()
class WorldClock2x2Provider : WorldClockWidgetProvider()
class WorldClock4x1Provider : WorldClockWidgetProvider()
class WorldClock4x2Provider : WorldClockWidgetProvider()
class AlarmWidget2x1Provider : AlarmWidgetProvider()
class AlarmWidget2x2Provider : AlarmWidgetProvider()
class AlarmWidget4x1Provider : AlarmWidgetProvider()
class AlarmWidget4x2Provider : AlarmWidgetProvider()
