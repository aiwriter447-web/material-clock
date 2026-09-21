package app.materialclock.util

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    fun formatAlarmTime(hour: Int, minute: Int, is24HourFormat: Boolean): String {
        val time = LocalTime.of(hour, minute)
        val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
}

@Composable
fun rememberWallTicker(): State<Long> {
    val millis = remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            millis.value = System.currentTimeMillis()
            delay(1000)
        }
    }
    return millis
}

@Composable
fun rememberElapsedTicker(isRunning: Boolean): State<Long> {
    val millis = remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isRunning) {
        while (isRunning) {
            millis.value = System.currentTimeMillis()
            delay(10)
        }
    }
    return millis
}
