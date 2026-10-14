package app.materialclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.materialclock.alarm.Notifications
import app.materialclock.ui.ClockApp

class MainActivity : ComponentActivity() {

    private var startTab by mutableStateOf<String?>(null)

    /**
     * Goes up by one each time the app is opened again from outside (a notification, a widget).
     * The screen is rebuilt when it changes, so tapping the World Clock widget switches to that
     * tab even when the app is already open on another one.
     */
    private var openCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTab = intent?.getStringExtra(Notifications.EXTRA_TAB)
        setContent {
            key(openCount) {
                ClockApp(startTab = startTab)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startTab = intent.getStringExtra(Notifications.EXTRA_TAB)
        openCount++
    }
}