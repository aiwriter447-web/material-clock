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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
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
// Reduced keypad size to give more room at the top
private val KEY_HEIGHT = 56.dp
private val KEYPAD_SPACING = 12.dp

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
    onAddPreset: () -> Unit,
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
        // Removed global padding here to fix the dock cut-off issue
        modifier = modifier.fillMaxSize(),
    ) { running ->
        if (running && timer != null) {
            RunningTimer(timer, nowElapsedMillis, onPauseResume, onAddTen, onCancel, contentPadding)
        } else {
            SetTimer(draft, onDigit, onBackspace, onWind, onStart, presets, onStartPreset, onEditPreset, onAddPreset, contentPadding)
        }
    }
}

/* ── Setting Display ─────────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun SetTimer(
    draft: Duration,
    onDigit: (Char, TimerField) -> Unit,
    onBackspace: () -> Unit,
    onWind: (Int) -> Unit,
    onStart: () -> Unit,
    presets: List<TimerPreset>,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    onAddPreset: () -> Unit,
    contentPadding: PaddingValues,
) {
    var selectedField by remember { mutableStateOf(TimerField.SECONDS) }

    // Independent 2-digit state for each field
    var hStr by remember { mutableStateOf("%02d".format(draft.seconds / 3600)) }
    var mStr by remember { mutableStateOf("%02d".format((draft.seconds % 3600) / 60)) }
    var sStr by remember { mutableStateOf("%02d".format(draft.seconds % 60)) }

    // Reset local strings if timer is cancelled or fully cleared externally
    LaunchedEffect(draft.seconds) {
        if (draft.seconds == 0L) {
            hStr = "00"
            mStr = "00"
            sStr = "00"
        }
    }

    val armed = (hStr.toLongOrNull() ?: 0L) > 0 || (mStr.toLongOrNull() ?: 0L) > 0 || (sStr.toLongOrNull() ?: 0L) > 0

    // Synchronize independent local entries with the ViewModel's underlying microwave logic
    val syncToViewModel = {
        repeat(6) { onBackspace() }
        (hStr + mStr + sStr).forEach { onDigit(it, TimerField.SECONDS) }
    }

    val handleDigit = { digit: Char ->
        when (selectedField) {
            TimerField.HOURS -> hStr = (hStr + digit).takeLast(2)
            TimerField.MINUTES -> mStr = (mStr + digit).takeLast(2)
            TimerField.SECONDS -> sStr = (sStr + digit).takeLast(2)
        }
        syncToViewModel()
    }

    val handleBackspace = {
        when (selectedField) {
            TimerField.HOURS -> hStr = ("0" + hStr.dropLast(1)).takeLast(2)
            TimerField.MINUTES -> mStr = ("0" + mStr.dropLast(1)).takeLast(2)
            TimerField.SECONDS -> sStr = ("0" + sStr.dropLast(1)).takeLast(2)
        }
        syncToViewModel()
    }

    val handleClearAll = {
        hStr = "00"
        mStr = "00"
        sStr = "00"
        onWind(0)
        syncToViewModel()
        selectedField = TimerField.SECONDS
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Top and Bottom padding applied inside the scrollable container fixes the floating dock overlap
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(42.dp))

        DraftReadout(
            hh = hStr,
            mm = mStr,
            ss = sStr,
            selectedField = selectedField,
            onFieldSelect = { selectedField = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        Keypad(
            onDigit = handleDigit,
            onBackspace = handleBackspace,
            onClearAll = handleClearAll,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()) {
            AnimatedStartButton(armed = armed, onClick = onStart)
        }

        Spacer(Modifier.height(24.dp))

        PresetRow(presets, onStartPreset, onEditPreset, onAddPreset)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PresetRow(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    onEdit: (TimerPreset) -> Unit,
    onAdd: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, bottom = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            PresetAddChip(onClick = onAdd)
        }
        items(presets, key = { it.id }) { preset ->
            PresetChip(preset, onClick = { onStart(preset) }, onLongClick = { onEdit(preset) })
        }
    }
}

@Composable
private fun PresetAddChip(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "presetAddScale"
    )

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.Add, 
                contentDescription = "New Preset", 
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "New",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(preset: TimerPreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val minutes = preset.totalSeconds / 60
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "presetChipScale"
    )

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
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
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Text(
                text = "$minutes min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun DraftReadout(
    hh: String,
    mm: String,
    ss: String,
    selectedField: TimerField,
    onFieldSelect: (TimerField) -> Unit,
    modifier: Modifier = Modifier
) {
    val cap = 64.dp
    val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp)
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    val activeColor = MaterialTheme.colorScheme.onSurface
    val highlightColor = MaterialTheme.colorScheme.primary

    val hColor by animateColorAsState(if (selectedField == TimerField.HOURS) highlightColor else if (hh != "00") activeColor else inactiveColor, label = "hColor")
    val mColor by animateColorAsState(if (selectedField == TimerField.MINUTES) highlightColor else if (mm != "00") activeColor else inactiveColor, label = "mColor")
    val sColor by animateColorAsState(if (selectedField == TimerField.SECONDS) highlightColor else if (ss != "00") activeColor else inactiveColor, label = "sColor")

    val fieldWidth = 88.dp
    val colonWidth = 24.dp
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Labels row structured independently so numerals don't jump when labels change position
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Hours", style = labelStyle, color = hColor, modifier = Modifier.width(fieldWidth), textAlign = TextAlign.Center)
            Spacer(Modifier.width(colonWidth))
            Text("Minutes", style = labelStyle, color = mColor, modifier = Modifier.width(fieldWidth), textAlign = TextAlign.Center)
            Spacer(Modifier.width(colonWidth))
            Text("Seconds", style = labelStyle, color = sColor, modifier = Modifier.width(fieldWidth), textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(18.dp))

        // Values Row perfectly aligned with Colons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clearAndSetSemantics { contentDescription = "$hh hours, $mm minutes, $ss seconds" }
        ) {
            Box(
                Modifier
                    .width(fieldWidth)
                    .clickable(interactionSource = interactionSource, indication = null) { onFieldSelect(TimerField.HOURS) },
                contentAlignment = Alignment.Center
            ) {
                Numerals(text = hh, capHeight = cap, color = hColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
            }
            Box(Modifier.width(colonWidth), contentAlignment = Alignment.Center) {
                Text(":", style = MaterialTheme.typography.displayMedium, color = inactiveColor, modifier = Modifier.offset(y = (-6).dp))
            }
            Box(
                Modifier
                    .width(fieldWidth)
                    .clickable(interactionSource = interactionSource, indication = null) { onFieldSelect(TimerField.MINUTES) },
                contentAlignment = Alignment.Center
            ) {
                Numerals(text = mm, capHeight = cap, color = mColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
            }
            Box(Modifier.width(colonWidth), contentAlignment = Alignment.Center) {
                Text(":", style = MaterialTheme.typography.displayMedium, color = inactiveColor, modifier = Modifier.offset(y = (-6).dp))
            }
            Box(
                Modifier
                    .width(fieldWidth)
                    .clickable(interactionSource = interactionSource, indication = null) { onFieldSelect(TimerField.SECONDS) },
                contentAlignment = Alignment.Center
            ) {
                Numerals(text = ss, capHeight = cap, color = sColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
            }
        }
    }
}

@Composable
private fun Keypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf("123", "456", "789")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KEYPAD_SPACING),
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KEYPAD_SPACING)) {
                row.forEach { c -> DigitKey(c, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KEYPAD_SPACING)) {
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onClearAll,
                container = MaterialTheme.colorScheme.errorContainer,
                label = "Clear",
            ) {
                Text(
                    "C",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            DigitKey('0', onDigit, Modifier.weight(1f))

            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onBackspace,
                container = MaterialTheme.colorScheme.surfaceContainerHighest,
                label = "Delete",
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun DigitKey(digit: Char, onDigit: (Char) -> Unit, modifier: Modifier = Modifier) {
    KeyBox(
        modifier = modifier,
        onClick = { onDigit(digit) },
        container = MaterialTheme.colorScheme.surfaceContainer,
        label = digit.toString()
    ) {
        Text(
            digit.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = ClockFace.family(opticalSize = 28f, width = 100f, weight = 500)
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KeyBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    container: Color,
    label: String,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "keyBoxScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = container,
        shape = CircleShape,
        modifier = modifier
            .height(KEY_HEIGHT)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clearAndSetSemantics { contentDescription = label }
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
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
            .height(68.dp)
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
