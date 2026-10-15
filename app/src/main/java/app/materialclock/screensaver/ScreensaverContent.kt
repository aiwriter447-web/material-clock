package app.materialclock.screensaver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        val receiver = object : BroadcastReceiver() {
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

    // ── Next System Alarm Fetch ───────────────────────────────────────
    var nextAlarmString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarm = alarmManager?.nextAlarmClock
        nextAlarmString = if (nextAlarm != null) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(nextAlarm.triggerTime)).lowercase()
        } else {
            null
        }
    }

    // ── Smart Adaptive Pixel Shift Positions (Burn-In Protection) ──────
    var clockX by remember { mutableIntStateOf(0) }
    var clockY by remember { mutableIntStateOf(0) }

    LaunchedEffect(settings.pixelShiftEnabled, settings.clockShiftIntervalMinutes) {
        if (settings.pixelShiftEnabled) {
            while (true) {
                delay(settings.clockShiftIntervalMinutes * 60 * 1000L)
                clockX = Random.nextInt(-30, 30)
                clockY = Random.nextInt(-30, 30)
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

    // ── Night Mode Dimming Alpha ──────────────────────────────────────
    val clockAlpha = if (settings.nightModeEnabled) settings.clockDimmingFactor else 1.0f

    // ── Time & Date Formatting ────────────────────────────────────────
    val mainTimePattern = if (settings.showSeconds) "h:mm:ss" else "h:mm"
    val timeMainString = remember(currentTime, mainTimePattern) {
        SimpleDateFormat(mainTimePattern, Locale.getDefault()).format(currentTime)
    }
    val amPmString = remember(currentTime) {
        SimpleDateFormat("a", Locale.getDefault()).format(currentTime).lowercase()
    }
    val dateString = remember(currentTime, settings.dateFormat) {
        SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()).format(currentTime)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(clockX, clockY) }
                .alpha(clockAlpha)
                .padding(24.dp)
        ) {
            if (isLandscape) {
                // ── Landscape Layout (Matches Pic 2) ──────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (settings.showDate) {
                            Text(
                                text = dateString,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (settings.showBattery) {
                                InfoChip(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Rounded.BatteryFull,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = "$batteryPercentage%"
                                )
                            }
                            nextAlarmString?.let { alarmText ->
                                InfoChip(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Notifications,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = alarmText
                                )
                            }
                        }
                    }

                    // Main Clock Display
                    TimeDisplay(
                        timeText = timeMainString,
                        amPmText = amPmString
                    )
                }
            } else {
                // ── Portrait Layout (Matches Pic 1) ───────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Main Clock Display
                    TimeDisplay(
                        timeText = timeMainString,
                        amPmText = amPmString
                    )

                    if (settings.showDate) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = dateString,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pill Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (settings.showBattery) {
                            InfoChip(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.BatteryFull,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = "$batteryPercentage%"
                            )
                        }
                        nextAlarmString?.let { alarmText ->
                            InfoChip(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Notifications,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = alarmText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeDisplay(
    timeText: String,
    amPmText: String
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = timeText,
            fontSize = 82.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = amPmText,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 14.dp)
        )
    }
}

@Composable
private fun InfoChip(
    icon: @Composable () -> Unit,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF222222),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
