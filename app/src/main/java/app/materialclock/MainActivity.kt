package app.materialclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.materialclock.alarm.Notifications
import app.materialclock.ui.ClockApp

class MainActivity : ComponentActivity() {

    private var startTab by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTab = intent?.getStringExtra(Notifications.EXTRA_TAB)
        setContent { 
            ClockApp(startTab = startTab) 
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startTab = intent.getStringExtra(Notifications.EXTRA_TAB)
    }
}
