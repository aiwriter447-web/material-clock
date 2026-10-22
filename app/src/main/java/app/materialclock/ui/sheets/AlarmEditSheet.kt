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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.theme.Numerals
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

    var label by rememberSaveable(initial.id) { mutableStateOf(initial.label) }
    var days by remember(initial.id) { mutableStateOf(initial.days) }
    var vibrate by rememberSaveable(initial.id) { mutableStateOf(initial.vibrate) }
    var soundUri by rememberSaveable(initial.id) { mutableStateOf(initial.soundUri) }
    var groupId by rememberSaveable(initial.id) { mutableStateOf(initial.groupId) }
    var discard by remember(initial.id) { mutableStateOf(false) }

    var manualEntry by rememberSaveable(initial.id) { mutableStateOf(false) }

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
        if (!discard && (explicit || onDelete != null)) {
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
        onDismiss()
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

    BackHandler { commitAndClose() }

    ModalBottomSheet(
        onDismissRequest = { commitAndClose() },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            
            // टॉप स्पेसिंग
            Spacer(Modifier.height(28.dp))

            Box(Modifier.fillMaxWidth().padding(horizontal = EDGE), contentAlignment = Alignment.CenterEnd) {
                val iconInteraction = remember { MutableInteractionSource() }
                val isIconPressed by iconInteraction.collectIsPressedAsState()
                val iconScale by animateFloatAsState(
                    targetValue = if (isIconPressed) 0.85f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                    label = "iconScale"
                )

                IconButton(
                    onClick = { manualEntry = !manualEntry },
                    interactionSource = iconInteraction,
                    modifier = Modifier.graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                ) {
                    Icon(
                        imageVector = if (manualEntry) Icons.Outlined.AccessTime else Icons.Outlined.Keyboard,
                        contentDescription = if (manualEntry) "Use dial" else "Type time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // फिक्स्ड हाइट बॉक्स
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp), 
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = manualEntry,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "TimeInputModeAnim"
                ) { isManual ->
                    if (isManual) {
                        ManualTimeEntry(state = timeState)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            WideTimeField(state = timeState)
                            Spacer(Modifier.height(16.dp))
                            PillDial(
                                state = timeState,
                                modifier = Modifier.padding(horizontal = EDGE),
                                onHourPicked = { timeState.selection = TimePickerSelectionMode.Minute },
                            )
                        }
                    }
                }
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
                    Icon(Icons.Outlined.MusicNote, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        ringtoneName(soundUri),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
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
                        Icons.Outlined.Vibration,
                        contentDescription = if (vibrate) "Vibrate on" else "Vibrate off",
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
                        targetValue = if (isDeletePressed) 0.93f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                        label = "deleteScale"
                    )

                    FilledTonalButton(
                        onClick = { onDelete(initial.id); onDismiss() },
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
                        Icon(Icons.Outlined.Delete, contentDescription = null, Modifier.size(20.dp))
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
                    onClick = { discard = true; onDismiss() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WideTimeField(state: TimePickerState) {
    val hour = if (state.is24hour) state.hour else ((state.hour % 12).takeIf { it != 0 } ?: 12)
    val onHour = state.selection == TimePickerSelectionMode.Hour

    Row(
        Modifier.fillMaxWidth().padding(horizontal = EDGE),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FieldPill(
            text = "%02d".format(hour),
            selected = onHour,
            onClick = { state.selection = TimePickerSelectionMode.Hour },
        )
        Text(
            ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        FieldPill(
            text = "%02d".format(state.minute),
            selected = !onHour,
            onClick = { state.selection = TimePickerSelectionMode.Minute },
        )
        if (!state.is24hour) {
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MeridiemChip("AM", state.hour < 12) { if (state.hour >= 12) state.hour -= 12 }
                MeridiemChip("PM", state.hour >= 12) { if (state.hour < 12) state.hour += 12 }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualTimeEntry(state: TimePickerState) {
    var hourText by remember(state.is24hour) {
        val h = if (state.is24hour) state.hour else (state.hour % 12).takeIf { it != 0 } ?: 12
        mutableStateOf("%02d".format(h))
    }
    var minuteText by remember { mutableStateOf("%02d".format(state.minute)) }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = EDGE),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = hourText,
            onValueChange = { new ->
                val digits = new.filter(Char::isDigit).take(2)
                hourText = digits
                val n = digits.toIntOrNull()
                if (n != null) {
                    if (state.is24hour) {
                        if (n in 0..23) state.hour = n
                    } else if (n in 1..12) {
                        state.hour = (n % 12) + if (state.hour >= 12) 12 else 0
                    }
                }
            },
            label = { Text("Hour") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.large,
            textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center), 
            modifier = Modifier.width(116.dp).height(100.dp), 
        )
        Text(
            ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        OutlinedTextField(
            value = minuteText,
            onValueChange = { new ->
                val digits = new.filter(Char::isDigit).take(2)
                minuteText = digits
                val n = digits.toIntOrNull()
                if (n != null && n in 0..59) state.minute = n
            },
            label = { Text("Min") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.large,
            textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(116.dp).height(100.dp), 
        )
        if (!state.is24hour) {
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MeridiemChip("AM", state.hour < 12) { if (state.hour >= 12) state.hour -= 12 }
                MeridiemChip("PM", state.hour >= 12) { if (state.hour < 12) state.hour += 12 }
            }
        }
    }
}

@Composable
private fun FieldPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "fieldPillScale"
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
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(Modifier.padding(horizontal = 22.dp, vertical = FIELD_PAD_V)) {
            Numerals(
                text = text,
                capHeight = FIELD_CAP,
                color = contentColor,
                width = 151f,
                weight = 460,
            )
        }
    }
}

@Composable
private fun MeridiemChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "meridiemScale"
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
                scaleX = scale
                scaleY = scale
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
            Icon(Icons.Outlined.Group, contentDescription = null, Modifier.size(20.dp))
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

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.90f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
                label = "dayToggleScale"
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
                        scaleX = scale
                        scaleY = scale
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
