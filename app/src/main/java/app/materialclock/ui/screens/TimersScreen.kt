package app.materialclock.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.materialclock.core.ClockTimer
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration

private const val WIND_MAX_MINUTES = 60
private const val RING_SIZE_DP = 300

@Composable
fun TimersScreen(
    timer: ClockTimer?,
    draft: Duration,
    nowElapsedMillis: Long,
    presets: List<TimerPreset>,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onWind: (Int) -> Unit,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    onAddPreset: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = timer != null,
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
        label = "timer-state",
        modifier = modifier.fillMaxSize().padding(contentPadding),
    ) { running ->
        if (running && timer != null) {
            RunningTimer(timer, nowElapsedMillis, onPauseResume, onAddTen, onCancel)
        } else {
            SetTimer(draft, onDigit, onBackspace, onWind, onStart, presets, onStartPreset, onEditPreset, onAddPreset)
        }
    }
}

/* ── Setting ───────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun SetTimer(
    draft: Duration,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onWind: (Int) -> Unit,
    onStart: () -> Unit,
    presets: List<TimerPreset>,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    onAddPreset: () -> Unit,
) {
    val total = draft.seconds
    val hh = "%02d".format(total / 3600)
    val mm = "%02d".format((total % 3600) / 60)
    val ss = "%02d".format(total % 60)
    val armed = total > 0

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))
        DraftReadout(hh, mm, ss)
        Spacer(Modifier.height(22.dp))
        Keypad(onDigit = onDigit, onBackspace = onBackspace)
        Spacer(Modifier.height(10.dp))
        Winder(
            value = (total / 60).toInt().coerceAtMost(WIND_MAX_MINUTES),
            range = 0..WIND_MAX_MINUTES,
            onValueChange = onWind,
        )
        Spacer(Modifier.height(10.dp))
        WidePill(
            text = "Start",
            icon = Icons.Rounded.PlayArrow,
            onClick = onStart,
            height = 76.dp,
            container = if (armed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            content = if (armed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        PresetRow(presets, onStartPreset, onEditPreset, onAddPreset)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun PresetRow(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    onEdit: (TimerPreset) -> Unit,
    onAdd: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(presets, key = { it.id }) { preset ->
            PresetChip(preset, onClick = { onStart(preset) }, onLongClick = { onEdit(preset) })
        }
        item {
            AssistChip(
                onClick = onAdd,
                label = { Text("+ Add") },
            )
        }
    }
}

@Composable
private fun PresetChip(preset: TimerPreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val minutes = preset.totalSeconds / 60
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                preset.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                "${minutes} min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun DraftReadout(hh: String, mm: String, ss: String) {
    val text = "$hh:$mm:$ss"
    val firstReal = text.indexOfFirst { it in '1'..'9' }.let { if (it < 0) text.length - 1 else it }
    val cap = 56.dp
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.clearAndSetSemantics { contentDescription = "$hh hours, $mm minutes, $ss seconds" },
    ) {
        if (firstReal > 0) {
            Numerals(text = text.take(firstReal), capHeight = cap, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f), width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
        }
        Numerals(text = text.drop(firstReal), capHeight = cap, color = MaterialTheme.colorScheme.onSurface, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf("123", "456", "789")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c -> DigitKey(c, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.weight(1f))
            DigitKey('0', onDigit, Modifier.weight(1f))
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onBackspace,
                container = Color.Transparent,
                label = "Delete",
            ) {
                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun DigitKey(digit: Char, onDigit: (Char) -> Unit, modifier: Modifier = Modifier) {
    KeyBox(modifier = modifier, onClick = { onDigit(digit) }, label = digit.toString()) {
        Text(
            digit.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = ClockFace.family(opticalSize = 28f, width = 100f, weight = 500)),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KeyBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    container: Color = MaterialTheme.colorScheme.surfaceContainer,
    label: String,
    content: @Composable () -> Unit,
) {
    Surface(onClick = onClick, color = container, shape = CircleShape, modifier = modifier.height(58.dp).clearAndSetSemantics { contentDescription = label }) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/* ── Running ───────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun RunningTimer(
    timer: ClockTimer,
    nowElapsedMillis: Long,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
) {
    val remaining = timer.remaining(nowElapsedMillis)
    val secs = remaining.seconds
    val text = if (secs >= 3600) {
        "%d:%02d:%02d".format(secs / 3600, (secs % 3600) / 60, secs % 60)
    } else {
        "%02d:%02d".format((secs % 3600) / 60, secs % 60)
    }
    val running = timer.state == TimerState.RUNNING

    val context = LocalContext.current
    val is24Hour = remember(context) { android.text.format.DateFormat.is24HourFormat(context) }
    val targetTime = remember(remaining) { java.time.LocalTime.now().plusSeconds(remaining.seconds) }
    val targetFormatter = remember(is24Hour) {
        if (is24Hour) java.time.format.DateTimeFormatter.ofPattern("HH:mm") else java.time.format.DateTimeFormatter.ofPattern("h:mm a")
    }
    val targetString = targetTime.format(targetFormatter).lowercase()

    val measurer = rememberTextMeasurer()
    val maxTextWidth = RING_SIZE_DP.dp * 0.62f
    val capHeight = with(LocalDensity.current) {
        val ref = 62.dp
        val refStyle = ClockFace.numerals(capHeight = ref, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
        val measured = measurer.measure(text, refStyle).size.width.toDp()
        if (measured <= maxTextWidth) ref else ref * (maxTextWidth / measured)
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = { timer.fractionLeft(nowElapsedMillis) },
                modifier = Modifier.size(RING_SIZE_DP.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                stroke = Stroke(width = with(LocalDensity.current) { 18.dp.toPx() }, cap = StrokeCap.Round),
                trackStroke = Stroke(width = with(LocalDensity.current) { 18.dp.toPx() }, cap = StrokeCap.Round),
                gapSize = 10.dp,
                wavelength = 76.dp,
                amplitude = { if (running) 1f else 0f },
                waveSpeed = if (running) 28.dp else 0.dp,
            )
            
            // Timer Digits + Target Bell Icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Numerals(
                    text = text,
                    capHeight = capHeight,
                    color = MaterialTheme.colorScheme.onSurface,
                    width = ClockFace.TIMER_WIDTH,
                    weight = ClockFace.TIMER_WEIGHT,
                    slashedZero = true,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.NotificationsActive, 
                        contentDescription = "Ends at", 
                        modifier = Modifier.size(20.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        targetString, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                WidePill(
                    text = if (running) "Pause" else "Resume",
                    icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = onPauseResume,
                    height = 78.dp,
                )
            }
            Box(Modifier.weight(1f)) {
                WidePill(
                    text = "+10s",
                    icon = Icons.Rounded.Add,
                    onClick = onAddTen,
                    height = 78.dp,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        WidePill(
            text = "Cancel",
            icon = Icons.Rounded.Close,
            onClick = onCancel,
            outlined = true,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            height = 78.dp,
        )
        Spacer(Modifier.height(10.dp))
    }
}
