package app.materialclock.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.ClockTimer
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration

private const val RING_SIZE_DP = 300
enum class TimerField { HOURS, MINUTES, SECONDS }

@Composable
fun TimersScreen(
    timer: ClockTimer?,
    draft: Duration,
    nowElapsedMillis: Long,
    presets: List<TimerPreset>,
    onDigit: (Char, TimerField) -> Unit, 
    onBackspace: () -> Unit,
    onWind: (Int) -> Unit,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    onAddPreset: () -> Unit, // FAB removed, but signature kept intact
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = timer != null,
        transitionSpec = {
            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) using
                SizeTransform(clip = false)
        },
        label = "timer-state",
        modifier = modifier.fillMaxSize(),
    ) { running ->
        if (running && timer != null) {
            RunningTimer(timer, nowElapsedMillis, onPauseResume, onAddTen, onCancel, contentPadding)
        } else {
            SetTimer(draft, onDigit, onBackspace, onStart, onStartPreset, onEditPreset, contentPadding)
        }
    }
}

/* ── Setting Display ─────────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun SetTimer(
    draft: Duration,
    onDigit: (Char, TimerField) -> Unit,
    onBackspace: () -> Unit,
    onStart: () -> Unit,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    contentPadding: PaddingValues,
) {
    var hours by remember { mutableStateOf((draft.seconds / 3600).toInt()) }
    var mins by remember { mutableStateOf(((draft.seconds % 3600) / 60).toInt()) }
    var secs by remember { mutableStateOf((draft.seconds % 60).toInt()) }

    LaunchedEffect(draft.seconds) {
        if (draft.seconds == 0L) {
            hours = 0
            mins = 0
            secs = 0
        }
    }

    val armed = hours > 0 || mins > 0 || secs > 0

    val syncToViewModel = {
        repeat(6) { onBackspace() } 
        val digits = "%02d%02d%02d".format(hours, mins, secs)
        digits.forEach { onDigit(it, TimerField.SECONDS) }
    }

    // Default Hardcoded Presets, sorted from smallest to largest time
    val defaultPresets = remember {
        listOf(
            TimerPreset(1L, "Short Break", 5 * 60),
            TimerPreset(2L, "Long Break", 15 * 60),
            TimerPreset(3L, "Focus", 25 * 60),
            TimerPreset(4L, "Deep Work", 50 * 60),
            TimerPreset(5L, "Study", 60 * 60),
            TimerPreset(6L, "Sleep", 6 * 60 * 60)
        ).sortedBy { it.totalSeconds }
    }

    // Bulb effect color for colons
    val colonColor by animateColorAsState(
        targetValue = if (armed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "colonBulbColor"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp 
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val color = MaterialTheme.colorScheme.onSurfaceVariant
                Text("Hours", style = labelStyle, color = color, modifier = Modifier.width(88.dp), textAlign = TextAlign.Center)
                Spacer(Modifier.width(16.dp))
                Text("Minutes", style = labelStyle, color = color, modifier = Modifier.width(88.dp), textAlign = TextAlign.Center)
                Spacer(Modifier.width(16.dp))
                Text("Seconds", style = labelStyle, color = color, modifier = Modifier.width(88.dp), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfiniteWheelPicker(
                    range = 0..99,
                    initialValue = hours,
                    onValueChange = { hours = it; syncToViewModel() }
                )
                
                Text(
                    text = ":", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = colonColor, 
                    modifier = Modifier.offset(y = (-4).dp).padding(horizontal = 4.dp)
                )
                
                InfiniteWheelPicker(
                    range = 0..59,
                    initialValue = mins,
                    onValueChange = { mins = it; syncToViewModel() }
                )
                
                Text(
                    text = ":", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = colonColor, 
                    modifier = Modifier.offset(y = (-4).dp).padding(horizontal = 4.dp)
                )
                
                InfiniteWheelPicker(
                    range = 0..59,
                    initialValue = secs,
                    onValueChange = { secs = it; syncToViewModel() }
                )
            }

            Spacer(Modifier.height(48.dp))

            PresetGrid(defaultPresets, onStartPreset, onEditPreset)
            
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Long press a preset to edit it",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()) {
                AnimatedStartButton(armed = armed, onClick = {
                    syncToViewModel()
                    onStart()
                })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InfiniteWheelPicker(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = range.last - range.first + 1
    // Calculate a safe middle point for infinite scrolling
    val middlePoint = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % count)
    val initialIndex = middlePoint + (initialValue - range.first)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    
    // Smooth snapping behavior
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(initialValue) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentValue = (currentIndex % count) + range.first
        if (initialValue == 0 && currentValue != 0) {
            listState.animateScrollToItem(middlePoint)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val actualValue = (listState.firstVisibleItemIndex % count) + range.first
        onValueChange(actualValue)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(180.dp)
            .width(88.dp),
        contentPadding = PaddingValues(vertical = 60.dp), // Centering the active item perfectly
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(Int.MAX_VALUE) { index ->
            val actualValue = (index % count) + range.first
            val isSelected by remember { derivedStateOf { listState.firstVisibleItemIndex == index } }

            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Numerals(
                    text = "%02d".format(actualValue),
                    capHeight = if (isSelected) 52.dp else 30.dp, // Reduced font sizes for perfect 3-row fit
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    width = ClockFace.TIMER_WIDTH,
                    weight = ClockFace.TIMER_WEIGHT,
                    slashedZero = true
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetGrid(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    onEdit: (TimerPreset) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        presets.forEach { preset ->
            PresetChip(preset, onClick = { onStart(preset) }, onLongClick = { onEdit(preset) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(preset: TimerPreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val totalMins = preset.totalSeconds / 60
    val secs = preset.totalSeconds % 60
    val timeLabel = if (totalMins >= 60) {
        val h = totalMins / 60
        val m = totalMins % 60
        if (m > 0) "$h h $m m" else "$h h"
    } else if (secs > 0) {
        "$totalMins m $secs s"
    } else {
        "$totalMins min"
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "presetChipScale"
    )

    Surface(
        shape = RoundedCornerShape(percent = 50), 
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
        }
    }
}

@Composable
private fun AnimatedStartButton(armed: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && armed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "startScale"
    )

    val containerColor by animateColorAsState(
        if (armed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "startContainer"
    )
    val contentColor by animateColorAsState(
        if (armed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "startContent"
    )

    Surface(
        onClick = onClick,
        enabled = armed,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

/* ── Running View ───────────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun RunningTimer(
    timer: ClockTimer,
    nowElapsedMillis: Long,
    onPauseResume: () -> Unit,
    onAddTen: () -> Unit,
    onCancel: () -> Unit,
    contentPadding: PaddingValues
) {
    val remaining = timer.remaining(nowElapsedMillis)
    val secs = remaining.seconds
    val hasHours = secs >= 3600
    val text = if (hasHours) {
        "%d:%02d:%02d".format(secs / 3600, (secs % 3600) / 60, secs % 60)
    } else {
        "%02d:%02d".format((secs % 3600) / 60, secs % 60)
    }
    val running = timer.state == TimerState.RUNNING

    val context = LocalContext.current
    val is24Hour = remember(context) { android.text.format.DateFormat.is24HourFormat(context) }

    val targetTime = remember(remaining) { java.time.LocalDateTime.now().plusSeconds(remaining.seconds) }
    val targetFormatter = remember(is24Hour) {
        if (is24Hour) java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        else java.time.format.DateTimeFormatter.ofPattern("h:mm a")
    }
    val targetString = targetTime.format(targetFormatter).lowercase()

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    val refStyle = ClockFace.numerals(capHeight = 62.dp, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
    val strokeWidthPx = with(density) { 18.dp.toPx() }

    val capHeight = remember(hasHours, density, refStyle) {
        with(density) {
            val maxTextWidth = RING_SIZE_DP.dp * 0.62f
            val ref = 62.dp
            val worstCase = if (hasHours) "88:88:88" else "88:88"
            val measured = measurer.measure(worstCase, refStyle).size.width.toDp()
            if (measured <= maxTextWidth) ref else ref * (maxTextWidth / measured)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.7f))

        Box(contentAlignment = Alignment.Center) {
            val waveAmplitude by animateFloatAsState(
                targetValue = if (running) 1f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "waveAmplitude"
            )

            val waveSpeedFactor by animateFloatAsState(
                targetValue = if (running) 1f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "waveSpeedFactor"
            )

            CircularWavyProgressIndicator(
                progress = { timer.fractionLeft(nowElapsedMillis) },
                modifier = Modifier.size(RING_SIZE_DP.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                trackStroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                gapSize = 10.dp,
                wavelength = 76.dp,
                amplitude = { waveAmplitude },
                waveSpeed = (28.dp * waveSpeedFactor),
            )

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
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        targetString,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Ends at $targetString"
                        }
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.8f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                PauseResumeButton(running = running, onClick = onPauseResume)
            }
            Box(Modifier.weight(1f)) {
                AnimatedPillButton(
                    text = "+10s",
                    icon = Icons.Rounded.Add,
                    onClick = onAddTen,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        AnimatedPillButton(
            text = "Cancel",
            icon = Icons.Rounded.Close,
            onClick = onCancel,
            container = MaterialTheme.colorScheme.surfaceContainer,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PauseResumeButton(running: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pauseResumeScale"
    )
    val containerColor by animateColorAsState(
        if (running) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        label = "pauseResumeContainer"
    )
    val contentColor by animateColorAsState(
        if (running) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
        label = "pauseResumeContent"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        AnimatedContent(
            targetState = running,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                    fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
            },
            label = "pauseResumeLabel"
        ) { isRunning ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = if (isRunning) "Pause timer" else "Resume timer"
                }
            ) {
                Icon(
                    if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRunning) "Pause" else "Resume",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun AnimatedPillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    container: Color,
    content: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pillButtonScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = container,
        contentColor = content,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clearAndSetSemantics { contentDescription = "$text button" }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
