package app.materialclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import app.materialclock.data.AnalogLook
import app.materialclock.data.ClockStore

/**
 * Keeps the Orbit design's battery figure and ring reasonably fresh.
 *
 * A widget's picture is drawn once, and the battery level is part of that picture. Android does
 * not let an app listen for battery changes from the manifest, so the picture would show the level
 * from the moment it was last drawn. This asks the system to nudge the app every 15 minutes, only
 * while an Orbit widget exists. The alarm type does not wake the phone, so it costs nothing while
 * the screen is off: the redraw simply happens the next time the phone is awake.
 */
object OrbitRefresh {

    const val ACTION = "app.materialclock.action.ORBIT_REFRESH"
    private const val REQUEST = 9101
    private const val INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES

    private fun intent(context: Context) =
        Intent(context, AnalogClock2x2Provider::class.java).setAction(ACTION)

    /** Starts the 15-minute nudge, unless it is already running. */
    fun schedule(context: Context) {
        val app = context.applicationContext
        val existing = PendingIntent.getBroadcast(
            app, REQUEST, intent(app),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (existing != null) return
        val pending = PendingIntent.getBroadcast(
            app, REQUEST, intent(app),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        app.getSystemService(AlarmManager::class.java).setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + INTERVAL,
            INTERVAL,
            pending,
        )
    }

    fun cancel(context: Context) {
        val app = context.applicationContext
        val pending = PendingIntent.getBroadcast(
            app, REQUEST, intent(app),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        app.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }

    /** Called when the nudge arrives: redraw, and stop nudging once no Orbit widget is left. */
    suspend fun onTick(context: Context, store: ClockStore) {
        val ids = WidgetProviders.ids(context, WidgetKind.ANALOG)
        val anyOrbit = ids.any { store.widgetConfigNow(it)?.look == AnalogLook.ORBIT }
        if (!anyOrbit) {
            cancel(context)
            return
        }
        ClockWidgetProvider.refreshAll(context)
    }
}
