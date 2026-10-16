package app.materialclock.screensaver

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.materialclock.MainActivity

class ScreensaverSettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Send a signal to MainActivity to open the Screensaver settings sheet directly
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("OPEN_SCREENSAVER", true)

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Launch MainActivity with the Screensaver settings signal
        startActivity(intent)

        // Close this bridge activity so it does not remain in the background
        finish()
    }
}