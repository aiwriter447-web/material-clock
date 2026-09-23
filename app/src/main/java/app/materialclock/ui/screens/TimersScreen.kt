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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.ClockTimer
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration

private const val WIND_MAX_MINUTES = 60
private const val RING_SIZE_DP = 320 // Increased for MD3E

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
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), // More breathing room
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        DraftReadout(hh, mm, ss)
        Spacer(Modifier.height(28.dp))
        
        Keypad(onDigit = onDigit, onBackspace = onBackspace)
        Spacer(Modifier.height(16.dp))
        
        Winder(
            value = (total / 60).toInt().coerceAtMost(WIND_MAX_MINUTES),
            range = 0..WIND_MAX_MINUTES,
            onValueChange = onWind,
        )
        Spacer(Modifier.height(16.dp))
        
        // Expressive Start Button
        WidePill(
            text = "Start",
            icon = Icons.Rounded.PlayArrow,
            onClick = onStart,
            height = 80.dp, // Larger and bolder button for MD3E
            container = if (armed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            content = if (armed) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.height(16.dp))
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(presets, key = { it.id }) { preset ->
            PresetChip(preset, onClick = { onStart(preset) }, onLongClick = { onEdit(preset) })
        }
        item {
            AssistChip(
                onClick = onAdd,
                label = { Text("+ Add", fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(16.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.padding(vertical = 4.dp).height(56.dp)
            )
        }
    }
}

@Composable
private fun PresetChip(preset: TimerPreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val minutes = preset.totalSeconds / 60
    Surface(
        shape = RoundedCornerShape(16.dp), // MD3E squircle shape
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick).height(64.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                preset.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${minutes} min",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun DraftReadout(hh: String, mm: String, ss: String) {
    val text = "$hh:$mm:$ss"
    val firstReal = text.indexOfFirst { it in '1'..'9' }.let { if (it < 0) text.length - 1 else it }
    val cap = 64.dp // Made the draft readout much larger
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "$hh hours, $mm minutes, $ss seconds"
        },
    ) {
        if (firstReal > 0) {
            Numerals(
                text = text.take(firstReal),
                capHeight = cap,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f),
                width = ClockFace.TIMER_WIDTH,
                weight = ClockFace.TIMER_WEIGHT,
                slashedZero = true,
            )
        }
        Numerals(
            text = text.drop(firstReal),
            capHeight = cap,
            color = MaterialTheme.colorScheme.onSurface,
            width = ClockFace.TIMER_WIDTH,
            weight = ClockFace.TIMER_WEIGHT,
            slashedZero = true,
        )
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf("123", "456", "789")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { c -> DigitKey(c, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(Modifier.weight(1f))
            DigitKey('0', onDigit, Modifier.weight(1f))
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onBackspace,
                container = Color.Transparent, // Delete button remains transparent
                label = "Delete",
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun DigitKey(digit: Char, onDigit: (Char) -> Unit, modifier: Modifier = Modifier) {
    KeyBox(modifier = modifier, onClick = { onDigit(digit) }, label = digit.toString()) {
        Text(
            digit.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium), // Larger font
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KeyBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh, // Expressive and dark tone
    label: String,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = container,
        shape = CircleShape, // Fully rounded button
        modifier = modifier
            .height(68.dp) // Increased button height
            .clearAndSetSemantics { contentDescription = label },
    ) {
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

    val measurer = rememberTextMeasurer()
    val maxTextWidth = RING_SIZE_DP.dp * 0.65f
    val capHeight = with(LocalDensity.current) {
        val ref = 72.dp // Increased the text size inside the ring even further
        val refStyle = ClockFace.numerals(
            capHeight = ref,
            width = ClockFace.TIMER_WIDTH,
            weight = ClockFace.WEIGHT_ON,
            slashedZero = true,
        )
        val measured = measurer.measure(text, refStyle).size.width.toDp()
        if (measured <= maxTextWidth) ref else ref * (maxTextWidth / measured)
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = { timer.fractionLeft(nowElapsedMillis) },
                modifier = Modifier.size(RING_SIZE_DP.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                stroke = Stroke(width = with(LocalDensity.current) { 22.dp.toPx() }, cap = StrokeCap.Round), // Thicker stroke
                trackStroke = Stroke(width = with(LocalDensity.current) { 22.dp.toPx() }, cap = StrokeCap.Round),
                gapSize = 12.dp,
                wavelength = 84.dp,
                amplitude = { if (running) 1f else 0f },
                waveSpeed = if (running) 32.dp else 0.dp,
            )
            Numerals(
                text = text,
                capHeight = capHeight,
                color = MaterialTheme.colorScheme.onSurface,
                width = ClockFace.TIMER_WIDTH,
                weight = ClockFace.WEIGHT_ON, // Bold numbers
                slashedZero = true,
            )
        }
        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                WidePill(
                    text = if (running) "Pause" else "Resume",
                    icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = onPauseResume,
                    height = 80.dp, // Large expressive buttons
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Box(Modifier.weight(1f)) {
                WidePill(
                    text = "+10s",
                    icon = Icons.Rounded.Add,
                    onClick = onAddTen,
                    height = 80.dp,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        WidePill(
            text = "Cancel",
            icon = Icons.Rounded.Close,
            onClick = onCancel,
            container = MaterialTheme.colorScheme.surfaceContainer,
            content = MaterialTheme.colorScheme.onSurface,
            height = 72.dp, // Kept the cancel button slightly sleek
        )
        Spacer(Modifier.height(10.dp))
    }
}