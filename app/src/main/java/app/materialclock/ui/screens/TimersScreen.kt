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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.ClockTimer
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration

private const val RING_SIZE_DP = 300

// Selection state enum for the Tap-to-Type feature
enum class TimerField { HOURS, MINUTES, SECONDS }

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
    // Smooth fade and scale transition for screen changes
    AnimatedContent(
        targetState = timer != null,
        transitionSpec = { 
            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith 
            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) using 
            SizeTransform(clip = false) 
        },
        label = "timer-state",
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) { running ->
        if (running && timer != null) {
            RunningTimer(timer, nowElapsedMillis, onPauseResume, onAddTen, onCancel)
        } else {
            SetTimer(draft, onDigit, onBackspace, onWind, onStart, presets, onStartPreset, onEditPreset)
        }
    }
}

/* ── Setting Display (Fixes: Gap, Input Selection, Button Boxes) ────────────────────────────────────── */

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
) {
    val total = draft.seconds
    val hh = "%02d".format(total / 3600)
    val mm = "%02d".format((total % 3600) / 60)
    val ss = "%02d".format(total % 60)
    val armed = total > 0

    // Local state to track which field the user tapped (Hours, Mins, or Secs)
    var selectedField by remember { mutableStateOf(TimerField.SECONDS) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Increased top spacer to push the UI down slightly for better balance
        Spacer(Modifier.height(32.dp)) 

        DraftReadout(
            hh = hh, 
            mm = mm, 
            ss = ss,
            selectedField = selectedField,
            onFieldSelect = { selectedField = it },
            onClear = { onWind(0) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.weight(1f)) // pushes keypad to bottom proportionately

        Keypad(
            onDigit = { char -> 
                // Note: You will need to update your ViewModel's onDigit logic to respect 'selectedField'
                // Currently, we just pass the char. The UI selection is ready!
                onDigit(char) 
            }, 
            onBackspace = onBackspace, 
            onClearAll = { onWind(0) },
            modifier = Modifier.padding(horizontal = 32.dp) // tighter padding for keypad
        )

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()) {
            AnimatedStartButton(armed = armed, onClick = onStart)
        }

        Spacer(Modifier.height(24.dp))

        // Replaced LazyRow with FlowRow (Wraps to next line)
        PresetRow(presets, onStartPreset, onEditPreset)

        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetRow(
    presets: List<TimerPreset>,
    onStart: (TimerPreset) -> Unit,
    onEdit: (TimerPreset) -> Unit,
) {
    // FlowRow wraps items to the next line automatically instead of side-scrolling
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp) // FIXED: Chain modifiers separately to fix compile error
            .padding(bottom = 20.dp)
    ) {
        presets.forEach { preset ->
            PresetChip(preset, onClick = { onStart(preset) }, onLongClick = { onEdit(preset) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(preset: TimerPreset, onClick: () -> Unit, onLongClick: () -> Unit) {
    val minutes = preset.totalSeconds / 60
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy animation for presets
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
                text = "${minutes} min",
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
    onClear: () -> Unit, 
    modifier: Modifier = Modifier
) {
    val cap = 58.dp // Increased size slightly to fill space better
    val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    val activeColor = MaterialTheme.colorScheme.onSurface
    val highlightColor = MaterialTheme.colorScheme.primary // Color for selected field

    // Visual active state calculation
    val hColor by animateColorAsState(if (selectedField == TimerField.HOURS) highlightColor else if (hh != "00") activeColor else inactiveColor, label = "hColor")
    val mColor by animateColorAsState(if (selectedField == TimerField.MINUTES) highlightColor else if (mm != "00") activeColor else inactiveColor, label = "mColor")
    val sColor by animateColorAsState(if (selectedField == TimerField.SECONDS) highlightColor else if (ss != "00") activeColor else inactiveColor, label = "sColor")

    Row(
        verticalAlignment = Alignment.CenterVertically, // FIXED: Colons are now perfectly centered
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .clearAndSetSemantics { contentDescription = "$hh hours, $mm minutes,$ss seconds" },
    ) {
        // Hours Column - Now Clickable!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, 
                    onClick = { onFieldSelect(TimerField.HOURS) }
                )
        ) {
            Text("Hours", style = labelStyle, color = hColor)
            Spacer(Modifier.height(8.dp))
            Numerals(text = hh, capHeight = cap, color = hColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
        }

        Text(" : ", style = MaterialTheme.typography.displayMedium, color = inactiveColor, modifier = Modifier.padding(bottom = 6.dp))

        // Minutes Column - Now Clickable!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, 
                    onClick = { onFieldSelect(TimerField.MINUTES) }
                )
        ) {
            Text("Minutes", style = labelStyle, color = mColor)
            Spacer(Modifier.height(8.dp))
            Numerals(text = mm, capHeight = cap, color = mColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
        }

        Text(" : ", style = MaterialTheme.typography.displayMedium, color = inactiveColor, modifier = Modifier.padding(bottom = 6.dp))

        // Seconds Column - Now Clickable!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, 
                    onClick = { onFieldSelect(TimerField.SECONDS) }
                )
        ) {
            Text("Seconds", style = labelStyle, color = sColor)
            Spacer(Modifier.height(8.dp))
            Numerals(text = ss, capHeight = cap, color = sColor, width = ClockFace.TIMER_WIDTH, weight = ClockFace.TIMER_WEIGHT, slashedZero = true)
        }
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onClearAll: () -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf("123", "456", "789")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp), // Adjusted spacing for better shape
    ) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { c -> DigitKey(c, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // FIXED: Clear Button is now enclosed in a colored Box with bounce animation
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onClearAll,
                container = MaterialTheme.colorScheme.errorContainer, // Red tint box
                label = "Clear All",
            ) {
                Text(
                    "C", 
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), 
                    color = MaterialTheme.colorScheme.onErrorContainer 
                )
            }

            DigitKey('0', onDigit, Modifier.weight(1f))

            // FIXED: Delete Button is now enclosed in a proper colored Box with bounce animation
            KeyBox(
                modifier = Modifier.weight(1f),
                onClick = onBackspace,
                container = MaterialTheme.colorScheme.surfaceContainerHighest, // Grey/Darker Box
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
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = ClockFace.family(opticalSize = 28f, width = 100f, weight = 500)),
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

    // Bouncy Live Animation for Keypad
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
            .height(64.dp) // Adjusted height for better touch target
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

    Surface(
        onClick = onClick,
        enabled = armed,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (armed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (armed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
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

/* ── Running View (Fixes: Spacing, Gap, Animations) ───────────────────────────────────────────────────────────────── */

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
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Standardized side padding
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.7f)) // Balanced top spacer
        
        Box(contentAlignment = Alignment.Center) {
            // Added Smooth animation for Wave Amplitude to prevent glitch when pausing
            val waveAmplitude by animateFloatAsState(
                targetValue = if (running) 1f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "waveAmplitude"
            )
            
            CircularWavyProgressIndicator(
                progress = { timer.fractionLeft(nowElapsedMillis) },
                modifier = Modifier.size(RING_SIZE_DP.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                stroke = Stroke(width = with(LocalDensity.current) { 18.dp.toPx() }, cap = StrokeCap.Round),
                trackStroke = Stroke(width = with(LocalDensity.current) { 18.dp.toPx() }, cap = StrokeCap.Round),
                gapSize = 10.dp,
                wavelength = 76.dp,
                amplitude = { waveAmplitude },
                waveSpeed = if (running) 28.dp else 0.dp,
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
                        Icons.Rounded.NotificationsActive, // Rounded Expressive Icon
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
        
        Spacer(Modifier.weight(0.8f)) // FIXED GAP: Adjusted weight to bring buttons closer naturally

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                AnimatedPillButton(
                    text = if (running) "Pause" else "Resume",
                    icon = if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = onPauseResume,
                    container = if (running) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, // Stronger Pause Color
                    content = if (running) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                )
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
        
        Spacer(Modifier.height(32.dp)) // Bottom clearance
    }
}

// Reusable Animated Bouncy Button for the Running Timer Screen
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
