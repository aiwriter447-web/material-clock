package app.materialclock.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.ClockTimer
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import java.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    // Premium Living Flow: Direction-Aware Shared Axis (Y) Glide Transition
    AnimatedContent(
        targetState = timer != null,
        transitionSpec = {
            val forward = targetState // true = Starting Timer, false = Canceling Timer
            val slideSpec = spring<IntOffset>(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow)
            val fadeSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)

            if (forward) {
                // Flowing UP when starting
                (slideInVertically(slideSpec) { it / 5 } + fadeIn(fadeSpec)) togetherWith
                (slideOutVertically(slideSpec) { -it / 5 } + fadeOut(fadeSpec))
            } else {
                // Flowing DOWN when canceling
                (slideInVertically(slideSpec) { -it / 5 } + fadeIn(fadeSpec)) togetherWith
                (slideOutVertically(slideSpec) { it / 5 } + fadeOut(fadeSpec))
            } using SizeTransform(clip = false)
        },
        label = "timer-premium-fluid-state",
        modifier = modifier.fillMaxSize(),
    ) { running ->
        if (running && timer != null) {
            RunningTimer(timer, nowElapsedMillis, onPauseResume, onAddTen, onCancel, contentPadding)
        } else {
            val displayPresets = remember(presets) {
                if (presets.isEmpty()) {
                    listOf(
                        TimerPreset(1L, "Short Break", 5 * 60),
                        TimerPreset(2L, "Long Break", 15 * 60),
                        TimerPreset(3L, "Focus", 25 * 60),
                        TimerPreset(4L, "Nap", 30 * 60),
                        TimerPreset(5L, "Workout", 45 * 60),
                        TimerPreset(6L, "Deep Work", 50 * 60),
                        TimerPreset(7L, "Study", 60 * 60),
                        TimerPreset(8L, "Sport", 2 * 60 * 60),
                        TimerPreset(9L, "Sleep", 6 * 60 * 60)
                    ).sortedBy { it.totalSeconds }
                } else {
                    presets.sortedBy { it.totalSeconds }
                }
            }

            SetTimer(draft, onDigit, onBackspace, onStart, onStartPreset, onEditPreset, displayPresets, contentPadding)
        }
    }
}

/* ── Setting Display ─────────────────────────────────────────────────────────────────────────────────── */

@Composable
private fun SetTimer(
    draft: Duration,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onStart: () -> Unit,
    onStartPreset: (TimerPreset) -> Unit,
    onEditPreset: (TimerPreset) -> Unit,
    presets: List<TimerPreset>,
    contentPadding: PaddingValues,
) {
    var hours by remember { mutableStateOf((draft.seconds / 3600).toInt()) }
    var mins by remember { mutableStateOf(((draft.seconds % 3600) / 60).toInt()) }
    var secs by remember { mutableStateOf((draft.seconds % 60).toInt()) }

    LaunchedEffect(draft) {
        val dHours = (draft.seconds / 3600).toInt()
        val dMins = ((draft.seconds % 3600) / 60).toInt()
        val dSecs = (draft.seconds % 60).toInt()
        
        if (hours != dHours || mins != dMins || secs != dSecs) {
            hours = dHours
            mins = dMins
            secs = dSecs
        }
    }

    val armed = hours > 0 || mins > 0 || secs > 0
    val scope = rememberCoroutineScope()
    var isStarting by remember { mutableStateOf(false) }

    val colonColor by animateColorAsState(
        targetValue = if (armed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "colonBulbColor"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val setTop = contentPadding.calculateTopPadding()
        val setBottom = contentPadding.calculateBottomPadding() + 16.dp
        val wheelsBlock: @Composable () -> Unit = {
            // LABELS
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                val labelStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Hours", style = labelStyle, color = color, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Minutes", style = labelStyle, color = color, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Seconds", style = labelStyle, color = color, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(16.dp)) 

            // STRICT ALIGNMENT CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp), 
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InfiniteWheelPicker(range = 0..99, initialValue = hours, onValueChange = { hours = it })
                    }
                    Spacer(Modifier.width(16.dp)) 
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InfiniteWheelPicker(range = 0..59, initialValue = mins, onValueChange = { mins = it })
                    }
                    Spacer(Modifier.width(16.dp)) 
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        InfiniteWheelPicker(range = 0..59, initialValue = secs, onValueChange = { secs = it })
                    }
                }

                // ABSOLUTE COLONS OVERLAY
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.weight(1f))
                    Box(modifier = Modifier.width(16.dp), contentAlignment = Alignment.Center) {
                        Text(":", style = MaterialTheme.typography.displayLarge, color = colonColor, modifier = Modifier.offset(y = (-4).dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Box(modifier = Modifier.width(16.dp), contentAlignment = Alignment.Center) {
                        Text(":", style = MaterialTheme.typography.displayLarge, color = colonColor, modifier = Modifier.offset(y = (-4).dp))
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

        }
        val presetsBlock: @Composable () -> Unit = {
            PresetGrid(presets, onStartPreset, onEditPreset)
            
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Long press a preset to edit it",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

        }
        val startBlock: @Composable (Int) -> Unit = { pad ->
            Box(modifier = Modifier.padding(horizontal = pad.dp).fillMaxWidth()) {
                AnimatedStartButton(armed = armed && !isStarting, onClick = {
                    if (isStarting) return@AnimatedStartButton 
                    isStarting = true
                    scope.launch {
                        repeat(6) { onBackspace() }
                        val digits = "%02d%02d%02d".format(hours, mins, secs)
                        digits.forEach { onDigit(it) }
                        onStart()
                        delay(600) 
                        isStarting = false
                    }
                })
            }
        }

        if (maxWidth > maxHeight) {
            // Landscape / wide: wheels on the left, presets and Start on the right.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = setTop, bottom = setBottom, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    wheelsBlock()
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    presetsBlock()
                    Spacer(Modifier.height(16.dp))
                    startBlock(24)
                }
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = setTop, bottom = setBottom)
                .heightIn(min = (maxHeight - setTop - setBottom).coerceAtLeast(0.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.weight(0.12f))

            wheelsBlock()

            Spacer(Modifier.weight(0.14f))

            presetsBlock()

            Spacer(Modifier.height(24.dp))

            startBlock(48)

            Spacer(Modifier.weight(0.10f))
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
    val middlePoint = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % count)
    val initialIndex = middlePoint + (initialValue - range.first)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(initialValue) {
        val currentActual = (listState.firstVisibleItemIndex % count) + range.first
        if (initialValue != currentActual) {
            listState.scrollToItem(middlePoint + (initialValue - range.first))
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val actualValue = (listState.firstVisibleItemIndex % count) + range.first
        onValueChange(actualValue)
    }

    val itemHeight = 64.dp 

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * 3)
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(Int.MAX_VALUE) { index ->
            val actualValue = (index % count) + range.first
            val isSelected by remember { derivedStateOf { listState.firstVisibleItemIndex == index } }

            // Premium Fluid Scroll Scaling
            val targetScale = if (isSelected) 1f else 0.65f
            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
                label = "scaleAnim"
            )

            val targetColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = 200),
                label = "colorAnim"
            )

            Box(
                modifier = Modifier
                    .requiredHeight(itemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }) {
                    Numerals(
                        text = "%02d".format(actualValue),
                        capHeight = 42.dp,
                        color = animatedColor,
                        width = ClockFace.TIMER_WIDTH,
                        weight = ClockFace.TIMER_WEIGHT,
                        slashedZero = true
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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

    // Premium physical touch dampening
    val scale by animateFloatAsState(
        targetValu