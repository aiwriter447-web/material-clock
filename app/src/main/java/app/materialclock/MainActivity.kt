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

    private var startTab by mutableStateOf<String?>(null)

    /**
     * Goes up by one each time the app is opened again
     * from outside (a notification, a widget).
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

        // Get initial tab from notification or widget
        startTab = extractStartTab(intent)

        setContent {
            key(openCount) {
                ClockApp(startTab = startTab)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Refresh widgets whenever app comes to foreground
        WidgetRefresh.all(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Preserve original intent handling
        setIntent(intent)

        val newTab = extractStartTab(intent)

        // Rebuild ClockApp only if a specific tab is requested (e.g., from a widget)
        if (newTab != null) {
            startTab = newTab
            openCount++
        }
    }

    // Preserve original helper function
    private fun extractStartTab(intent: Intent?): String? {
        return intent?.getStringExtra(Notifications.EXTRA_TAB)
    }
}
