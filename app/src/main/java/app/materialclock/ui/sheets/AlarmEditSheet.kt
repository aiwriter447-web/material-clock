package app.materialclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val scope = rememberCoroutineScope()
    
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var label by rememberSaveable(initial.id) { mutableStateOf(initial.label) }
    var days by remember(initial.id) { mutableStateOf(initial.days) }
    var vibrate by rememberSaveable(initial.id) { mutableStateOf(initial.vibrate) }
    var soundUri by rememberSaveable(initial.id) { mutableStateOf(initial.soundUri) }
    var groupId by rememberSaveable(initial.id) { mutableStateOf(initial.groupId) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var specificDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = specificDateMillis ?: System.currentTimeMillis()
    )
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
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

    fun commitAndClose(explicit: Boolean = false) {
        scope.launch {
            sheetState.hide()
            if (explicit || onDelete != null) {
                onSave(
                    initial.copy(
                        time = LocalTime.of(timeState.hour, timeState.minute),
                        label = label.trim(),
                        days = days,
                        enabled = true,
                        vibrate = vibrate,
                        soundUri = soundUri,
                        groupId = groupId,
                    )
                )
            }
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    specificDateMillis = datePickerState.selectedDateMillis
                    if (specificDateMillis != null) {
                        days = emptySet() 
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false 
            )
        }
    }

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

    
    val isDark = isSystemInDarkTheme()
    
    val targetDeleteBgColor = if (isDark) Color(0xFFFF5252) else Color(0xFFFFEBEE)
    val targetDeleteContentColor = if (isDark) Color(0xFF310000) else Color(0xFFC62828)
    val customDeleteBgColor by animateColorAsState(targetDeleteBgColor, spring(stiffness = Spring.StiffnessLow), label = "delBg")
    val customDeleteContentColor by animateColorAsState(targetDeleteContentColor, spring(stiffness = Spring.StiffnessLow), label = "delFg")
    
    val targetCancelBgColor = if (isDark) Color(0xFF757575) else Color(0xFFF5F5F5)
    val targetCancelContentColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF424242)
    val customCancelBgColor by animateColorAsState(targetCancelBgColor, spring(stiffness = Spring.StiffnessLow), label = "canBg")
    val customCancelContentColor by animateColorAsState(targetCancelContentColor, spring(stiffness = Spring.StiffnessLow), label = "canFg")

    // LIVING FLOW: Global Physics Spec for Buttons
    val fluidSpringSpec = spring<Float>(dampingRatio = 0.6f, stiffness = 200f)

    ModalBottomSheet(
        onDismissRequest = { scope.launch { sheetState.hide(); onDismiss() } },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() 
                .imePadding()
                .verticalScroll(rememberScrollState()) 
        ) {
            Spacer(Modifier.height(16.dp)) 
            
            SwipeTimeEntry(state = timeState)
            
            Spacer(Modifier.height(24.dp)) 

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(36.dp), 
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 24.dp) 
                    // LIVING FLOW: Fluid Content Sizing (कंटेनर पानी की तरह अपनी जगह बनाएगा)
                    .animateContentSize(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp) 
                ) {
                    
                    val dayText = remember(days, specificDateMillis) {
                        if (specificDateMillis != null && days.isEmpty()) {
                            val inst = Instant.ofEpochMilli(specificDateMillis!!)
                            val ld = inst.atZone(ZoneId.systemDefault()).toLocalDate()
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM")
                            ld.format(formatter)
                        } else {
                            when {
                                days.size == 7 -> "Every day"
                                days.isEmpty() -> "Tomorrow"
                                days == setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY) -> "Weekdays"
                                days == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekends"
                                else -> {
                                    val sortedDays = days.sortedBy { it.value }
                                    "Every " + sortedDays.joinToString(", ") { it.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()) }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayText, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showDatePicker = true }, 
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth, 
                                contentDescription = "Choose specific date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, 
                    ) {
                        weekStart.order(systemFirstDay()).forEach { day ->
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val isChecked = day in days

                           
                            val dayScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.75f else if (isChecked) 1.05f else 1f,
                                animationSpec = fluidSpringSpec,
                                label = "dayToggleScale"
                            )

                            val targetContainer = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                            val targetContent = if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            // LIVING FLOW: Color Morphing
                            val containerColor by animateColorAsState(targetContainer, spring(stiffness = Spring.StiffnessLow), label = "dayBg")
                            val contentColor by animateColorAsState(targetContent, spring(stiffness = Spring.StiffnessLow), label = "dayFg")

                            Surface(
                                onClick = { 
                                    days = if (day in days) days - day else days + day 
                                    specificDateMillis = null 
                                },
                                interactionSource = interactionSource,
                                shape = CircleShape,
                                color = containerColor,
                                contentColor = contentColor,
                                modifier = Modifier
                                    .size(44.dp) 
                                    .graphicsLayer {
                                        scaleX = dayScale
                                        scaleY = dayScale
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        day.getDisplayName(JavaTextStyle.NARROW, Locale.getDefault()),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Name", style = MaterialTheme.typography.titleMedium) },
                        textStyle = MaterialTheme.typography.titleMedium,
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    scope.launch {
                                        delay(300) 
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                    )

                    GroupPicker(
                        groups = groups,
                        selectedId = groupId,
                        onSelect = { groupId = it },
                        onCreateGroup = { name -> onCreateGroup(name); pendingNewGroupName = name },
                        modifier = Modifier.fillMaxWidth(),
                        fluidSpringSpec = fluidSpringSpec // Pass the fluid spring down
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val soundInteraction = remember { MutableInteractionSource() }
                        val isSoundPressed by soundInteraction.collectIsPressedAsState()
                        
                        // LIVING FLOW: Button Breathing
                        val soundScale by animateFloatAsState(
                            targetValue = if (isSoundPressed) 0.90f else 1f,
                            animationSpec = fluidSpringSpec,
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
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
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
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        // LIVING FLOW: Organic Haptic Resonance (घटती हुई प्राकृतिक वेव)
                        val vibrateAnim = remember { Animatable(0f) }
                        LaunchedEffect(vibrate) {
                            if (vibrate) {
                                vibrateAnim.animateTo(12f, spring(dampingRatio = 0.2f, stiffness = 500f))
                                vibrateAnim.animateTo(-12f, spring(dampingRatio = 0.3f, stiffness = 400f))
                                vibrateAnim.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
                            }
                        }

                        FilledIconToggleButton(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it },
                            shape = RoundedCornerShape(16.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (onDelete != null) {
                            val deleteInteraction = remember { MutableInteractionSource() }
                            val isDeletePressed by deleteInteraction.collectIsPressedAsState()
                            
                            // LIVING FLOW: Deep Tactile Press
                            val deleteScale by animateFloatAsState(
                                targetValue = if (isDeletePressed) 0.85f else 1f,
                                animationSpec = fluidSpringSpec,
                                label = "deleteScale"
                            )

                            FilledTonalButton(
                                onClick = { showDeleteConfirm = true },
                                interactionSource = deleteInteraction,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = customDeleteBgColor,
                                    contentColor = customDeleteContentColor 
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
                            targetValue = if (isCancelPressed) 0.90f else 1f,
                            animationSpec = fluidSpringSpec,
                            label = "cancelScale"
                        )

                        FilledTonalButton(
                            onClick = { scope.launch { sheetState.hide(); onDismiss() } },
                            interactionSource = cancelInteraction,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = customCancelBgColor,
                                contentColor = customCancelContentColor
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(ROW_H)
                                .graphicsLayer {
                                    scaleX = cancelScale
                                    scaleY = cancelScale
                                },
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }

                        val saveInteraction = remember { MutableInteractionSource() }
                        val isSavePressed by saveInteraction.collectIsPressedAsState()
                        
                        val saveScale by animateFloatAsState(
                            targetValue = if (isSavePressed) 0.90f else 1f,
                            animationSpec = fluidSpringSpec,
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
                            Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeTimeEntry(state: TimePickerState) {
    val is24Hour = state.is24hour
    
    val hoursList = if (is24Hour) (0..23).map { "%02d".format(it) } else (1..12).map { "%02d".format(it) }
    val minutesList = (0..59).map { "%02d".format(it) }
    val amPmList = listOf("am", "pm")

    val currentHourIndex = if (is24Hour) state.hour else {
        val h12 = (state.hour % 12).takeIf { it != 0 } ?: 12
        h12 - 1
    }
    val currentAmPmIndex = if (state.hour < 12) 0 else 1

    val titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Hours", style = titleStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            WheelPicker(
                items = hoursList,
                selectedIndex = currentHourIndex,
                isAmPm = false,
                onIndexChanged = { newIndex ->
                    if (is24Hour) {
                        state.hour = newIndex
                    } else {
                        val newH12 = newIndex + 1
                        val isPm = state.hour >= 12
                        state.hour = (newH12 % 12) + if (isPm) 12 else 0
                    }
                }
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = " ", style = titleStyle) 
            Spacer(Modifier.height(24.dp)) 
            Box(
                modifier = Modifier.height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.width(12.dp)) 

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Minutes", style = titleStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            WheelPicker(
                items = minutesList,
                selectedIndex = state.minute,
                isAmPm = false,
                onIndexChanged = { state.minute = it }
            )
        }

        if (!is24Hour) {
            Spacer(Modifier.width(24.dp)) 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = " ", style = titleStyle) 
                Spacer(Modifier.height(24.dp))
                WheelPicker(
                    items = amPmList,
                    selectedIndex = currentAmPmIndex,
                    isAmPm = true, 
                    onIndexChanged = { newIndex ->
                        val isCurrentlyPm = state.hour >= 12
                        val wantsPm = newIndex == 1
                        if (wantsPm && !isCurrentlyPm) {
                            state.hour += 12
                        } else if (!wantsPm && isCurrentlyPm) {
                            state.hour -= 12
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    isAmPm: Boolean,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val virtualCount = 100_000 
    val initialCenter = virtualCount / 2
    val initialIndex = initialCenter - (initialCenter % items.size) + selectedIndex.coerceAtLeast(0)
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val realIndex = listState.firstVisibleItemIndex % items.size
            if (realIndex != selectedIndex) {
                onIndexChanged(realIndex) 
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        val currentlyShowing = listState.firstVisibleItemIndex % items.size
        if (currentlyShowing != selectedIndex) {
            val currentCenter = listState.firstVisibleItemIndex
            val targetOffset = selectedIndex - (currentCenter % items.size)
            var diff = targetOffset
            
            if (diff > items.size / 2) diff -= items.size
            else if (diff < -items.size / 2) diff += items.size
            
            listState.animateScrollToItem(currentCenter + diff)
        }
    }

    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    val itemHeight = 60.dp 
    val pickerHeight = 180.dp 

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        modifier = modifier
            .height(pickerHeight) 
            .width(80.dp), 
        contentPadding = PaddingValues(vertical = itemHeight), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(virtualCount) { index ->
            val realIndex = index % items.size
            val itemText = items[realIndex]
            val isSelected = index == firstVisible
            
            val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light
            val textAlpha = if (isSelected) 1f else 0.3f
            
            val baseStyle = if (isAmPm) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayMedium
            val textStyle = baseStyle.copy(fontWeight = fontWeight)

            // LIVING FLOW: Liquid Scroll Scale
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 0.85f, 
                // बहुत ही सॉफ्ट स्प्रिंग ताकि नंबर एक बुलबुले की तरह बड़े हों
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                label = "scale"
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(itemHeight) 
                    .fillMaxWidth()
            ) {
                Text(
                    text = itemText,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupPicker(
    groups: List<AlarmGroup>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    onCreateGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
    fluidSpringSpec: androidx.compose.animation.core.SpringSpec<Float> // LIVING FLOW parameter
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.id == selectedId }?.name ?: "No group"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // LIVING FLOW: Deep tactile press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = fluidSpringSpec,
        label = "groupPickerScale"
    )

    Box(modifier) {
        FilledTonalButton(
            onClick = { expanded = true },
            interactionSource = interactionSource,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
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
                style = MaterialTheme.typography.titleMedium,
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

private val EDGE = 24.dp
private val ROW_H = 56.dp
const val SILENT = "silent"

fun systemFirstDay(): DayOfWeek =
    java.time.temporal.WeekFields.of(Locale.getDefault()).firstDayOfWeek
