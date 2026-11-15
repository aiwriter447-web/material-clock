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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.ui.platform.LocalConfiguration
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

    val screenConfig = LocalConfiguration.current
    val wide = screenConfig.screenWidthDp > screenConfig.screenHeightDp

    ModalBottomSheet(
        onDismissRequest = { scope.launch { sheetState.hide(); onDismiss() } },
        sheetState = sheetState,
        sheetMaxWidth = if (wide) 900.dp else BottomSheetDefaults.SheetMaxWidth,
    ) {
        val detailsBlock: @Composable () -> Unit = {
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

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
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
                                    .widthIn(max = 44.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
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

                  