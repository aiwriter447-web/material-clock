package app.materialclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.materialclock.data.ClockSettings
import app.materialclock.data.ScreensaverRepository
import app.materialclock.data.ScreensaverSettings
import app.materialclock.ui.ClockApp
import app.materialclock.ui.sheets.ScreensaverSettingsSheet
import app.materialclock.ui.sheets.WorldSettingsSheet
import app.materialclock.ui.theme.MaterialClockTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialClockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainClockScreen()
                }
            }
        }
    }
}

@Composable
fun MainClockScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Repositories
    val screensaverRepository = remember { ScreensaverRepository(context) }

    // Screensaver Settings State
    val screensaverSettings by screensaverRepository.settingsFlow.collectAsState(
        initial = ScreensaverSettings()
    )

    // Visibility State for Screensaver Bottom Sheet
    var showScreensaverSettings by remember { mutableStateOf(false) }
    var showWorldSettings by remember { mutableStateOf(false) }

    // Main App Navigation / Core Content
    ClockApp(
        onOpenWorldSettings = { showWorldSettings = true },
        onOpenScreensaverSettings = { showScreensaverSettings = true }
    )

    // World Clock Settings Sheet
    if (showWorldSettings) {
        WorldSettingsSheet(
            settings = ClockSettings(),
            onChange = { /* Clock settings update callback */ },
            onOpenScreensaverSettings = {
                showWorldSettings = false
                showScreensaverSettings = true
            },
            onDismiss = { showWorldSettings = false }
        )
    }

    // Screensaver Settings Sheet
    if (showScreensaverSettings) {
        ScreensaverSettingsSheet(
            settings = screensaverSettings,
            onChange = { transform ->
                coroutineScope.launch {
                    screensaverRepository.updateSettings(transform)
                }
            },
            onDismiss = { showScreensaverSettings = false }
        )
    }
}
