package app.materialclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SwipeVertical
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.theme.Numerals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditSheet(
    initial: Alarm,
    weekStart: WeekStart,
    groups: List<AlarmGroup>,
    onCreateGroup: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: ((Long) -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Smooth closing animation के लिए

    var label by rememberSaveable(initial.id) { mutableStateOf(initial.label) }
    var days by remember(initial.id) { mutableStateOf(initial.days) }
    var vibrate by rememberSaveable(initial.id) { mutableStateOf(initial.vibrate) }
    var soundUri by rememberSaveable(initial.id) { mutableStateOf(initial.soundUri) }
    var groupId by rememberSaveable(initial.id) { mutableStateOf(initial.groupId) }
    
    var manualEntry by rememberSaveable(initial.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) } // Delete confirmation dialog state

    var pendingNewGroupName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(groups) {
        pendingNewGroupName?.let { pending ->
            groups.firstOrNull { it.name == pending }?.let {
                groupId = it.id
                pendingNewGroupName = null
            }
        }
    }

    val timeState = rememberTimePickerState(
        initialHour = initial.time.hour,
        initialMinute = initial.time.minute,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context),
    )

    // Smooth Closing Animation Logic
    fun commitAndClose(explicit: Boolean = false) {
        scope.launch {
            sheetState.hide() // पहले बॉटम शीट को नीचे एनिमेट करो
            if (explicit || onDelete != null) {
                onSave(
                    initial.copy(
                        time = LocalTime.of(timeState.hour, timeState.minute),
                        label = label.trim(),
                        days = days,
                        enabled = initial.enabled,
                        vibrate = vibrate,
                        soundUri = soundUri,
                        groupId = groupId,
                    )
                )
            }
            onDismiss() // फिर असली डिसमिस कॉल करो
        }
    }

    BackHandler {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val ringtonePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            soundUri = picked?.toString() ?: SILENT
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Alarm?") },
            text = { Text("Are you sure you want to permanently delete this alarm?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            sheetState.hide()
                            onDelete(initial.id)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = { 
            scope.launch { sheetState.hide(); onDismiss() } 
        },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Box wrapping the time picker to position the Toggle Icon at Top-Right (like Pic 4)
            Box(Modifier.fillMaxWidth().padding(horizontal = EDGE)) {
                
                AnimatedContent(
                    targetState = manualEntry,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220)))
                            .using(SizeTransform(clip = false) { _, _ ->
                                spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                )
                            })
                    },
                    label = "TimeInputModeAnim",
                    modifier = Modifier.fillMaxWidth()
                ) { isManual ->
                    if (isManual) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            SwipeTimeEntry(state = timeState) 
                            Spacer(Modifier.height(16.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            WideTimeField(state = timeState)
                            Spacer(Modifier.height(12.dp))
                            
                            var popScale by remember { mutableStateOf(1f) }
                            LaunchedEffect(timeState.hour, timeState.minute) {
                                popScale = 1.04f
                                delay(60)
                                popScale = 1f
                            }
                            val animatedPopScale by animateFloatAsState(
                                targetValue = popScale,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "dialPop"
                            )

                            PillDial(
                                state = timeState,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = animatedPopScale
                                        scaleY = animatedPopScale
                                    },
                                onHourPicked = { timeState.selection = TimePickerSelectionMode.Minute },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                // Toggle Icon strictly positioned at Top-End
                ToggleIcon(
                    isManual = manualEntry, 
                    onToggle = { manualEntry = !manualEntry },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            DayToggles(
                order = weekStart.order(systemFirstDay()),
                selected = days,
                onToggle = { d -> days = if (d in days) days - d else days + d },
            )

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Name") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(horizontal = EDGE),
            )

            Spacer(Modifier.height(12.dp))
            GroupPicker(
                groups = groups,
                selectedId = groupId,
                onSelect = { groupId = it },
                onCreateGroup = { name -> onCreateGroup(name); pendingNewGroupName = name },
            )

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = EDGE),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val soundInteraction = remember { MutableInteractionSource() }
                val isSoundPressed by soundInteraction.collectIsPressedAsState()
                val soundScale by animateFloatAsState(
                    targetValue = if (isSoundPressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                    label = "soundScale"
                )

                FilledTonalButton(
                    onClick = {
                        ringtonePicker.launch(
                            Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                .putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    soundUri?.takeIf { it != SILENT }?.let(Uri::parse),
                                )
                        )
                    },
                    interactionSource = soundInteraction,
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(ROW_H)
                        .graphicsLayer {
                            scaleX = soundScale
                            scaleY = soundScale
                        },
                ) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        ringtoneName(soundUri),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Lively Vibration Icon Animation
                val vibrateAnim = remember { Animatable(0f) }
                LaunchedEffect(vibrate) {
                    if (vibrate) {
                        vibrateAnim.animateTo(15f, spring(dampingRatio = 0.2f, stiffness = Spring.StiffnessHigh))
                        vibrateAnim.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
                    }
                }

                FilledIconToggleButton(
                    checked = vibrate,
                    onCheckedChange = { vibrate = it },
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.size(ROW_H),
                ) {
                    Icon(
                        imageVector = if (vibrate) Icons.Rounded.Vibration else Icons.Rounded.Smartphone,
                        contentDescription = if (vibrate) "Vibrate on" else "Vibrate off",
                        modifier = Modifier.graphicsLayer { rotationZ = vibrateAnim.value }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = EDGE),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (onDelete != null) {
                    val deleteInteraction = remember { MutableInteractionSource() }
                    val isDeletePressed by deleteInteraction.collectIsPressedAsState()
                    val deleteScale by animateFloatAsState(
                        targetValue = if (isDeletePressed) 0.90f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                        label = "deleteScale"
                    )

                    // Red Expressive Delete Button
                    FilledTonalButton(
                        onClick = { showDeleteConfirm = true },
                        interactionSource = deleteInteraction,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(ROW_H)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            },
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, Modifier.size(24.dp))
                    }
                }

                val cancelInteraction = remember { MutableInteractionSource() }
                val isCancelPressed by cancelInteraction.collectIsPressedAsState()
                val cancelScale by animateFloatAsState(
                    targetValue = if (isCancelPressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                    label = "cancelScale"
                )

                OutlinedButton(
                    onClick = { 
                        scope.launch { sheetState.hide(); onDismiss() }
                    },
                    interactionSource = cancelInteraction,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(ROW_H)
                        .graphicsLayer {
                            scaleX = cancelScale
                            scaleY = cancelScale
                        },
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }

                val saveInteraction = remember { MutableInteractionSource() }
                val isSavePressed by saveInteraction.collectIsPressedAsState()
                val saveScale by animateFloatAsState(
                    targetValue = if (isSavePressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                    label = "saveScale"
                )

                Button(
                    onClick = { commitAndClose(explicit = true) },
                    interactionSource = saveInteraction,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(if (onDelete != null) 1.8f else 2.4f)
                        .height(ROW_H)
                        .graphicsLayer {
                            scaleX = saveScale
                            scaleY = saveScale
                        },
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ToggleIcon(isManual: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    val iconInteraction = remember { MutableInteractionSource() }
    val isIconPressed by iconInteraction.collectIsPressedAsState()
    val iconScale by animateFloatAsState(
        targetValue = if (isIconPressed) 0.80f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "iconScale"
    )

    IconButton(
        onClick = onToggle,
        interactionSource = iconInteraction,
        modifier = modifier.graphicsLayer {
            scaleX = iconScale
            scaleY = iconScale
        }
    ) {
        Icon(
            imageVector = if (isManual) Icons.Rounded.Schedule else Icons.Rounded.SwipeVertical,
            contentDescription = if (isManual) "Use dial" else "Use swipe picker",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------
// Dial Mode Header
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WideTimeField(state: TimePickerState) {
    val hour = if (state.is24hour) state.hour else ((state.hour % 12).takeIf { it != 0 } ?: 12)
    val onHour = state.selection == TimePickerSelectionMode.Hour

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Hours",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FieldPill(
                text = "%02d".format(hour),
                selected = onHour,
                onClick = { state.selection = TimePickerSelectionMode.Hour },
            )
        }
        
        Text(
            ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp).padding(top = 24.dp),
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Minutes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FieldPill(
                text = "%02d".format(state.minute),
                selected = !onHour,
                onClick = { state.selection = TimePickerSelectionMode.Minute },
            )
        }

        if (!state.is24hour) {
            Spacer(Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp), 
                modifier = Modifier.padding(top = 24.dp)
            ) {
                MeridiemChip("AM", state.hour < 12) { if (state.hour >= 12) state.hour -= 12 }
                MeridiemChip("PM", state.hour >= 12) { if (state.hour < 12) state.hour += 12 }
            }
        }
    }
}

// ---------------------------------------------------------
// Swipe Mode Header
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeTimeEntry(state: TimePickerState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Hours",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            WheelPicker(
                range = if (state.is24hour) 0..23 else 1..12,
                value = if (state.is24hour) state.hour else ((state.hour % 12).takeIf { it != 0 } ?: 12),
                onValueChanged = { newVal ->
                    if (state.is24hour) {
                        state.hour = newVal
                    } else {
                        val isPm = state.hour >= 12
                        state.hour = (newVal % 12) + if (isPm) 12 else 0
                    }
                }
            )
        }

        Text(
            ":",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Minutes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            WheelPicker(
                range = 0..59,
                value = state.minute,
                onValueChanged = { state.minute = it }
            )
        }

        if (!state.is24hour) {
            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                MeridiemChip("AM", state.hour < 12) { if (state.hour >= 12) state.hour -= 12 }
                MeridiemChip("PM", state.hour >= 12) { if (state.hour < 12) state.hour += 12 }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    range: IntRange,
    value: Int,
    onValueChanged: (Int) -> Unit
) {
    val items = range.toList()
    val virtualCount = 100_000 
    val initialCenter = virtualCount / 2
    val initialIndex = initialCenter - (initialCenter % items.size) + items.indexOf(value).coerceAtLeast(0)
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
            val realIndex = idx % items.size
            if (realIndex in items.indices) {
                val newVal = items[realIndex]
                if (newVal != value) {
                    onValueChanged(newVal)
                }
            }
        }
    }

    LaunchedEffect(value) {
        val currentRealIndex = listState.firstVisibleItemIndex % items.size
        val targetRealIndex = items.indexOf(value)
        if (currentRealIndex != targetRealIndex && targetRealIndex >= 0) {
            var diff = targetRealIndex - currentRealIndex
            if (diff > items.size / 2) diff -= items.size
            else if (diff < -items.size / 2) diff += items.size
            
            listState.animateScrollToItem(listState.firstVisibleItemIndex + diff)
        }
    }

    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        modifier = Modifier
            .height(180.dp) 
            .width(90.dp),
        contentPadding = PaddingValues(vertical = 65.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(virtualCount) { index ->
            val realIndex = index % items.size
            val itemValue = items[realIndex]
            val isSelected = index == firstVisible
            
            val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "alpha")
            // Removed bold weight, using standard medium/normal displayLarge but scaling up slightly
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 0.85f, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "scale"
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "%02d".format(itemValue),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Medium // Reverted to non-distorted medium weight
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------

@Composable
private fun FieldPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "fieldPillPressScale"
    )
    
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "fieldPillSelectionScale"
    )

    val targetContainer = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val targetContent = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    val containerColor by animateColorAsState(targetContainer, spring(stiffness = Spring.StiffnessMediumLow), label = "pillContainer")
    val contentColor by animateColorAsState(targetContent, spring(stiffness = Spring.StiffnessMediumLow), label = "pillContent")

    Surface(
        color = containerColor,
        shape = CircleShape,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer {
            scaleX = pressScale * selectionScale
            scaleY = pressScale * selectionScale
        }
    ) {
        Box(Modifier.padding(horizontal = 22.dp, vertical = FIELD_PAD_V)) {
            Numerals(
                text = text,
                capHeight = FIELD_CAP,
                color = contentColor,
                width = 160f, // Slightly wider for bigger font
                weight = 550, // Increased weight to make the Dial font slightly Bolder/Thicker
            )
        }
    }
}

@Composable
private fun MeridiemChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "meridiemPressScale"
    )
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "meridiemSelectionScale"
    )

    val targetContainer = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val targetContent = if (selected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val containerColor by animateColorAsState(targetContainer, spring(stiffness = Spring.StiffnessMediumLow), label = "meridiemBg")
    val contentColor by animateColorAsState(targetContent, spring(stiffness = Spring.StiffnessMediumLow), label = "meridiemFg")

    Surface(
        modifier = Modifier
            .height(36.dp) 
            .width(48.dp)  
            .graphicsLayer {
                scaleX = pressScale * selectionScale
                scaleY = pressScale * selectionScale
            }, 
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GroupPicker(
    groups: List<AlarmGroup>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    onCreateGroup: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.id == selectedId }?.name ?: "No group"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "groupPickerScale"
    )

    Box(Modifier.fillMaxWidth().padding(horizontal = EDGE)) {
        FilledTonalButton(
            onClick = { expanded = true },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(ROW_H)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            Icon(Icons.Rounded.Group, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                selectedName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
            )
        }
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("No group") },
                    onClick = { onSelect(null); expanded = false },
                )
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = { onSelect(group.id); expanded = false },
                    )
                }
                DropdownMenuItem(
                    text = { Text("New group") },
                    leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = { expanded = false; showCreate = true },
                )
            }
        }
    }

    if (showCreate) {
        var name by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New group") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) onCreateGroup(name.trim())
                        showCreate = false
                    },
                ) { Text("Create") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreate = false }) { Text("Cancel") }
            },
        )
    }
}

private val EDGE = 24.dp
private val ROW_H = 56.dp
private val FIELD_CAP = 30.dp
private val FIELD_PAD_V = 16.dp

const val SILENT = "silent"

const val SHEET_MAX_FRACTION = 0.88f

@Composable
private fun ringtoneName(uri: String?): String {
    val context = LocalContext.current
    return remember(uri) {
        when {
            uri == null -> "Default sound"
            uri == SILENT -> "Silent"
            else -> runCatching {
                RingtoneManager.getRingtone(context, Uri.parse(uri))?.getTitle(context)
            }.getOrNull() ?: "Default sound"
        }
    }
}

@Composable
private fun DayToggles(order: List<DayOfWeek>, selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = EDGE - 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        order.forEach { day ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val isChecked = day in selected

            val pressScale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                label = "dayTogglePressScale"
            )
            
            val selectionScale by animateFloatAsState(
                targetValue = if (isChecked) 1.05f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "dayToggleSelectionScale"
            )

            val targetContainer = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            val targetContent = if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            val containerColor by animateColorAsState(targetContainer, spring(stiffness = Spring.StiffnessMediumLow), label = "dayBg")
            val contentColor by animateColorAsState(targetContent, spring(stiffness = Spring.StiffnessMediumLow), label = "dayFg")

            Surface(
                onClick = { onToggle(day) },
                interactionSource = interactionSource,
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .graphicsLayer {
                        scaleX = pressScale * selectionScale
                        scaleY = pressScale * selectionScale
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

fun systemFirstDay(): DayOfWeek =
    java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
