package app.materialclock.screensaver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.data.ScreensaverBackgroundType
import app.materialclock.data.ScreensaverSettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun ScreensaverContent(
    settings: ScreensaverSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ── Time & Date Live Updates ──────────────────────────────────────
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(settings.showSeconds) {
        val interval = if (settings.showSeconds) 1000L else 10000L
        while (true) {
            currentTime = Date()
            delay(interval)
        }
    }

    // ── Real-time Battery Percentage Listener ─────────────────────────
    var batteryPercentage by remember { mutableIntStateOf(100) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryPercentage = (level * 100) / scale
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // ── Smart Adaptive Pixel Shift Positions ──────────────────────────
    var clockX by remember { mutableIntStateOf(0) }
    var clockY by remember { mutableIntStateOf(0) }

    LaunchedEffect(settings.pixelShiftEnabled, settings.clockShiftIntervalMinutes) {
        if (settings.pixelShiftEnabled) {
            while (true) {
                delay(settings.clockShiftIntervalMinutes * 60 * 1000L)
                clockX = Random.nextInt(-35, 35)
                clockY = Random.nextInt(-35, 35)
            }
        } else {
            clockX = 0
            clockY = 0
        }
    }

    // ── Background Color Selection ────────────────────────────────────
    val backgroundColor = when (settings.backgroundType) {
        ScreensaverBackgroundType.OLED_BLACK -> Color.Black
        ScreensaverBackgroundType.LCD_DARK_GRAY -> Color(0xFF121212)
        ScreensaverBackgroundType.SOLID_COLOR -> Color(settings.solidBackgroundColor)
        else -> Color.Black
    }

    // ── Night Mode Dimming Transparency ──────────────────────────────
    val clockAlpha = if (settings.nightModeEnabled) settings.clockDimmingFactor else 1.0f

    // ── Time & Date Format Helpers ────────────────────────────────────
    val timePattern = if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
    val timeString = remember(currentTime, timePattern) {
        SimpleDateFormat(timePattern, Locale.getDefault()).format(currentTime)
    }
    val dateString = remember(currentTime, settings.dateFormat) {
        SimpleDateFormat(settings.dateFormat, Locale.getDefault()).format(currentTime)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(clockX, clockY) }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Clock Text Display
            Text(
                text = timeString,
                fontSize = 68.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(clockAlpha)
            )

            // Date Display
            if (settings.showDate) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateString,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Battery Percentage Display
            if (settings.showBattery) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$batteryPercentage%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
