package app.materialclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.materialclock.alarm.Notifications
import app.materialclock.ui.ClockApp
import app.materialclock.widget.WidgetRefresh

class MainActivity : ComponentActivity() {

    // Controls whether the Screensaver settings sheet should open
    private var openScreensaver by mutableStateOf(false)

    // Stores the initial tab requested by a notification or widget
    private var startTab by mutableStateOf<String?>(null)

    /**
     * Goes up by one each time the app is opened again
     * from outside (a notification, a widget, or Screensaver settings).
     *
     * The screen is rebuilt when it changes, so tapping
     * the World Clock widget switches to that tab even
     * when the app is already open on another one.
     */
    private var openCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Preserve original edge-to-edge functionality
        enableEdgeToEdge()

        // Handle initial intent from notification, widget, or Screensaver settings
        handleIntent(intent)

        setContent {
            key(openCount) {
                ClockApp(
                    startTab = startTab,
                    startWithScreensaver = openScreensaver
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Refresh widgets whenever the app comes to the foreground
        WidgetRefresh.all(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Preserve original intent handling
        setIntent(intent)

        // Extract the requested tab and Screensaver settings signal
        val newTab = extractStartTab(intent)
        val shouldOpenScreensaver =
            intent.getBooleanExtra("OPEN_SCREENSAVER", false)

        // Rebuild ClockApp when a specific tab or Screensaver is requested
        if (newTab != null || shouldOpenScreensaver) {
            startTab = newTab
            openScreensaver = shouldOpenScreensaver
            openCount++
        }
    }

    /**
     * Handles the initial intent when MainActivity is created.
     *
     * Supports:
     * - Notification navigation
     * - Widget navigation
     * - Opening Screensaver settings directly
     */
    private fun handleIntent(intent: Intent?) {

        // Preserve original tab extraction
        startTab = extractStartTab(intent)

        // Check whether Screensaver settings should open directly
        openScreensaver =
            intent?.getBooleanExtra("OPEN_SCREENSAVER", false) ?: false
    }

    /**
     * Preserve original helper function.
     *
     * Extracts the requested tab from notifications or widgets.
     */
    private fun extractStartTab(intent: Intent?): String? {
        return intent?.getStringExtra(Notifications.EXTRA_TAB)
    }
}