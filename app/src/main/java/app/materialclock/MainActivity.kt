package app.materialclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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

    /**
     * Which tab to open on.
     *
     * Tapping a timer or stopwatch notification should land on that tab, not on Alarms. The
     * activity is `singleTask`, so a second tap while it is already open arrives at [onNewIntent]
     * rather than through `onCreate`. Writing to state that the composition reads covers both.
     */
    private var startTab by mutableStateOf<String?>(null)

    /**
     * PendingIntent used for both setting and cancelling the dummy alarm.
     * It's important to use the exact same PendingIntent to successfully remove the icon.
     */
    private val alarmPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java)
        PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTab = intent?.getStringExtra(Notifications.EXTRA_TAB)
        // Edge-to-edge is enabled inside ClockTheme, where the effective dark/light state is known
        // and the system-bar icon contrast can be set to match it.
        setContent { 
            ClockApp(startTab = startTab) 
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startTab = intent.getStringExtra(Notifications.EXTRA_TAB)
    }

    /**
     * Call this method to show the alarm icon in the status bar.
     * You can call this from your Compose UI when the alarm switch is turned ON.
     */
    fun showAlarmIcon() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Set a dummy trigger time (e.g., 1 hour from now) just to display the icon
        val triggerTime = System.currentTimeMillis() + (60 * 60 * 1000)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, alarmPendingIntent)
        
        // This tells the system an alarm is set, which shows the status bar icon
        alarmManager.setAlarmClock(alarmClockInfo, alarmPendingIntent)
    }

    /**
     * Call this method to hide the alarm icon from the status bar.
     * You can call this from your Compose UI when the alarm switch is turned OFF.
     */
    fun hideAlarmIcon() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancelling the exact pending intent removes the icon from the status bar
        alarmManager.cancel(alarmPendingIntent)
    }
}
