package app.materialclock

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import app.materialclock.alarm.Notifications
import app.materialclock.ui.ClockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // The same instance the screen gets from viewModel(): it belongs to this activity.
    private val clockViewModel: ClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Preserve original edge-to-edge functionality
        enableEdgeToEdge()

        // Handle initial intent from notification, widget, or Screensaver settings
        handleIntent(intent)

        holdFirstFrameUntilLoaded()

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

        // Refresh widgets whenever the app comes to the foreground, but not right now: redrawing
        // every widget is heavy, and doing it while the first screen is still appearing made
        // the app open slower. A moment later nobody notices.
        lifecycleScope.launch {
            delay(1500)
            WidgetRefresh.all(applicationContext)
        }
    }

    /**
     * Keeps the first frame back until the saved settings have been read (at most 0.35 s).
     *
     * Until then the screen is an empty page in the app's default colours, and the page would flash
     * on screen and then be replaced. Waiting a few milliseconds for the data, with the system's
     * own start-up screen still showing, avoids that flash and makes the opening look like one
     * smooth movement.
     */
    private fun holdFirstFrameUntilLoaded() {
        val content = findViewById<View>(android.R.id.content)
        val started = SystemClock.uptimeMillis()
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val ready = clockViewModel.isLoaded.value || SystemClock.uptimeMillis() - started > 350
                if (ready) content.viewTreeObserver.removeOnPreDrawListener(this)
                return ready
            }
        })
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