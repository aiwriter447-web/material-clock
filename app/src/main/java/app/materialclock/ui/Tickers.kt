package app.materialclock.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun rememberWallTicker(intervalMillis: Long = 1000L): State<Long> {
    val millis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(intervalMillis) {
        while (true) {
            millis.longValue = System.currentTimeMillis()
            delay(intervalMillis)
        }
    }
    return millis
}

@Composable
fun rememberElapsedTicker(active: Boolean = true): State<Long> {
    val millis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) {
        while (active) {
            millis.longValue = System.currentTimeMillis()
            delay(10)
        }
    }
    return millis
}
