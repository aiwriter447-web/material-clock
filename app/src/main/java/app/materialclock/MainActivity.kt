package app.materialclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.materialclock.alarm.Notifications
import app.materialclock.ui.ClockApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startTab = extractStartTab(intent)

        setContent {
            ClockApp(startTab = startTab)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractStartTab(intent: Intent?): String? {
        return intent?.getStringExtra(Notifications.EXTRA_TAB)
    }
}
