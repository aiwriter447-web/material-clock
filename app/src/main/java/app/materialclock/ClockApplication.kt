package app.materialclock

import android.app.Application
import app.materialclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Starts reading the saved data the moment the app process starts.
 *
 * The first read of the settings file is the slowest thing the app does at start-up: it has to be
 * opened and parsed, and until it is done the first screen is only a blank page. Starting it here,
 * in the background, means it runs in parallel with the creation of the window and the first
 * screen instead of after them. Anything that asks for the data later (the screen, a widget, an
 * alarm) finds it already in memory.
 */
class ClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            ClockStore(this@ClockApplication).settingsNow()
        }
    }
}
