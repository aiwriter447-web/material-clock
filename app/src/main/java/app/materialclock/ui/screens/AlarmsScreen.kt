package app.materialclock.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.GroupRemove
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.DOCK_HEIGHT
import app.materialclock.ui.sheets.systemFirstDay
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    groups: List<AlarmGroup>,
    weekStart: WeekStart,
    onSelectionChange: (Boolean) -> Unit,
    onToggle: (Long) -> Unit,
    onToggleGroup: (Long, Boolean) -> Unit,
    onTogglePin: (Long) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onDeleteSelected: (Set<Long>, Set<Long>) -> Unit,
    onSetEnabledSelected: (Set<Long>, Set<Long>, Boolean) -> Unit,
    onUngroupSelected: (Set<Long>, Set<Long>) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val order = remember(weekStart) { weekStart.order(systemFirstDay()) }
    val context = LocalContext.current
    val is24Hour = remember(context) { android.text.format.DateFormat.is24HourFormat(context) }
    val byGroup = remember(alarms) { alarms.groupBy { it.groupId } }
    
    var selectedAlarms by remember { mutableStateOf(emptySet<Long>()) }
    var selectedGroups by remember { mutableStateOf(emptySet<Long>()) }
    val inSelectionMode = selectedAlarms.isNotEmpty() || selectedGroups.isNotEmpty()
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(inSelectionMode) {
        onSelectionChange(inSelectionMode)
    }

    BackHandler(enabled = inSelectionMode) {
        selectedAlarms = emptySet()
        selectedGroups = emptySet()
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Delete Items") },
            text = { Text("Are you sure you want to delete the selected ${selectedAlarms.size} alarms and ${selectedGroups.size} groups?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelected(selectedAlarms, selectedGroups)
                        selectedAlarms = emptySet()
                        selectedGroups = emptySet()
                        showBatchDeleteConfirm = false
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "upcoming-alarm-text", contentType = "upcoming") {
                val upcomingInfo = remember(alarms) { calculateTimeUntilNextAlarm(alarms) }
                AnimatedVisibility(
                    visible = upcomingInfo != null,
                    enter = expandVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(400)),
                    exit = shrinkVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeOut(tween(400))
                ) {
                    if (upcomingInfo != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = upcomingInfo.first,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = upcomingInfo.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            if (groups.isNotEmpty()) {
                item(key = "group-cards", contentType = "groups") {
                    GroupCardsSection(
                        groups = groups,
                        byGroup = byGroup,
                        selectedGroups = selectedGroups,
                        inSelectionMode = inSelectionMode,
                        onToggleGroup = onToggleGroup,
                        onToggleGroupSelect = { groupId ->
                            selectedGroups = if (groupId in selectedGroups) selectedGroups - groupId else selectedGroups + groupId
                        }
                    )
                }
            }

            items(alarms, key = { it.id }, contentType = { "alarm" }) { alarm ->
                val isSelected = alarm.id in selectedAlarms
                
                AlarmRow(
                    alarm = alarm,
                    order = order,
                    is24Hour = is24Hour,
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    onToggle = { onToggle(alarm.id) },
                    onEdit = { onEdit(alarm) },
                    onToggleSelect = {
                        selectedAlarms = if (isSelected) selectedAlarms - alarm.id else selectedAlarms + alarm.id
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }

        AnimatedVisibility(
            visible = inSelectionMode,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp) 
        ) {
            val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = if (dark) {
                        FloatingToolbarDefaults.standardFloatingToolbarColors(
                            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FloatingToolbarDefaults.vibrantFloatingToolbarColors()
                    },
                    expandedShadowElevation = 6.dp,
                    modifier = Modifier.height(DOCK_HEIGHT),
                ) {
                    val allSelected = selectedAlarms.size == alarms.size && selectedGroups.size == groups.size
                    IconButton(onClick = {
                        if (allSelected) {
                            selectedAlarms = emptySet()
                            selectedGroups = emptySet()
                        } else {
                            selectedAlarms = alarms.map { it.id }.toSet()
                            selectedGroups = groups.map { it.id }.toSet()
                        }
                    }) {
                        Icon(if (allSelected) Icons.Rounded.Deselect else Icons.Rounded.SelectAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = { 
                        onSetEnabledSelected(selectedAlarms, selectedGroups, true) 
                        selectedAlarms = emptySet()
                        selectedGroups = emptySet()
                    }) {
                        Icon(Icons.Rounded.AlarmOn, contentDescription = "Turn On")
                    }
                    IconButton(onClick = { 
                        onSetEnabledSelected(selectedAlarms, selectedGroups, false) 
                        selectedAlarms = emptySet()
                        selectedGroups = emptySet()
                    }) {
                        Icon(Icons.Rounded.AlarmOff, contentDescription = "Turn Off")
                    }
                    IconButton(onClick = { 
                        onUngroupSelected(selectedAlarms, selectedGroups) 
                        selectedAlarms = emptySet()
                        selectedGroups = emptySet()
                    }) {
                        Icon(Icons.Rounded.GroupRemove, contentDescription = "Ungroup")
                    }
                    IconButton(onClick = { showBatchDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun calculateTimeUntilNextAlarm(alarms: List<Alarm>): Pair<String, String>? {
    val activeAlarms = alarms.filter { it.enabled }
    if (activeAlarms.isEmpty()) return null

    val now = LocalDateTime.now()
    var minDuration: Duration? = null
    var nextAlarmTime: LocalDateTime? = null

    for (alarm in activeAlarms) {
        val alarmTime = LocalTime.of(alarm.time.hour, alarm.time.minute)
        for (i in 0..7) {
            val checkDate = now.plusDays(i.toLong())
            val checkDay = checkDate.dayOfWeek
            val isActiveDay = alarm.isOneShot || alarm.days.contains(checkDay) || alarm.days.isEmpty()

            if (isActiveDay) {
                val candidateDateTime = checkDate.withHour(alarmTime.hour).withMinute(alarmTime.minute).withSecond(0).withNano(0)
                if (candidateDateTime.isAfter(now)) {
                    val duration = Duration.between(now, candidateDateTime)
                    if (minDuration == null || duration < minDuration) {
                        minDuration = duration
                        nextAlarmTime = candidateDateTime
                    }
                    break
                }
            }
        }
    }

    if (minDuration == null || nextAlarmTime == null) return null
    
    val hours = minDuration.toHours()
    val minutes = minDuration.toMinutes() % 60

    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM, h:mm a")
    val formattedDate = nextAlarmTime.format(formatter).replace("AM", "am").replace("PM", "pm")

    val durationString = buildString {
        append("Alarm in ")
        if (hours > 0) append("$hours hours ")
        append("$minutes minutes")
    }
    return Pair(durationString, formattedDate)
}

private val GROUP_SECTION_PADDING_H = 4.dp
private val GROUP_CARD_GAP = 10.dp
private val GROUP_CARD_PADDING = 16.dp
private val GROUP_CARD_CORNER = 22.dp

@Composable
private fun GroupCardsSection(
    groups: List<AlarmGroup>,
    byGroup: Map<Long?, List<Alarm>>,
    selectedGroups: Set<Long>,
    inSelectionMode: Boolean,
    onToggleGroup: (Long, Boolean) -> Unit,
    onToggleGroupSelect: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = GROUP_SECTION_PADDING_H),
        verticalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
    ) {
        groups.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
            ) {
                pair.forEach { group ->
                    val groupAlarms = byGroup[group.id].orEmpty()
                    val isSelected = group.id in selectedGroups
                    val armedCount = groupAlarms.count { it.enabled }
                    val unarmedCount = groupAlarms.size - armedCount
                    GroupCard(
                        group = group,
                        armedCount = armedCount,
                        unarmedCount = unarmedCount,
                        // Fix: Agar ek bhi alarm on hai toh group toggle ON rahega
                        checked = groupAlarms.any { it.enabled },
                        isSelected = isSelected,
                        inSelectionMode = inSelectionMode,
                        onToggle = { onToggleGroup(group.id, it) },
                        onToggleSelect = { onToggleGroupSelect(group.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupCard(
    group: AlarmGroup,
    armedCount: Int,
    unarmedCount: Int,
    checked: Boolean,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggle: (Boolean) -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (checked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val ink = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (checked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val titleFontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal

    Surface(
        color = container,
        shape = RoundedCornerShape(GROUP_CARD_CORNER),
        modifier = modifier.combinedClickable(
            onClick = { if (inSelectionMode) onToggleSelect() else onToggle(!checked) },
            onLongClick = { onToggleSelect() }
        ),
    ) {
        Column(Modifier.padding(GROUP_CARD_PADDING)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = titleFontWeight),
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = checked, 
                    onCheckedChange = { if (!inSelectionMode) onToggle(it) },
                    enabled = !inSelectionMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = container,
                        checkedTrackColor = ink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = ink,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = ink,
                        disabledCheckedThumbColor = container,
                        disabledCheckedTrackColor = ink,
                        disabledUncheckedThumbColor = ink,
                        disabledUncheckedTrackColor = Color.Transparent,
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GroupCount(armed = armedCount, unarmed = unarmedCount, ink = ink)
            }
        }
    }
}

@Composable
private fun GroupCount(armed: Int, unarmed: Int, ink: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Alarm, contentDescription = "On Alarms", tint = ink.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(armed.toString(), style = MaterialTheme.typography.labelLarge, color = ink)
        
        Spacer(Modifier.width(12.dp))
        
        Icon(Icons.Rounded.AlarmOff, contentDescription = "Off Alarms", tint = ink.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(unarmed.toString(), style = MaterialTheme.typography.labelLarge, color = ink)
    }
}

private val ROW_HORIZONTAL_PADDING = 16.dp
private val ROW_VERTICAL_PADDING = 16.dp
private val ROW_CORNER_RADIUS = 24.dp
private val ROW_LABEL_TO_TIME = 8.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmRow(
    alarm: Alarm,
    order: List<DayOfWeek>,
    is24Hour: Boolean,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = alarm.enabled
    val container = if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val ink = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val hour12 = (alarm.time.hour % 12).takeIf { it != 0 } ?: 12
    val meridiem = if (alarm.time.hour < 12) "AM" else "PM"

    val displayLabel = if (alarm.label.isNotBlank()) alarm.label else " "
    val labelColor = if (alarm.label.isNotBlank()) ink else Color.Transparent
    
    val timeFontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
    val titleFontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal

    Surface(
        color = container,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        modifier = modifier.fillMaxWidth().combinedClickable(
            onClick = { if (inSelectionMode) onToggleSelect() else onEdit() },
            onLongClick = { onToggleSelect() }
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = titleFontWeight),
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (alarm.pinnedAt != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ROW_LABEL_TO_TIME))

                val timeStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = timeFontWeight, fontFeatureSettings = "tnum")
                val amPmStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = timeFontWeight)

                if (is24Hour) {
                    Text(
                        text = "%02d:%02d".format(alarm.time.hour, alarm.time.minute),
                        style = timeStyle,
                        color = ink
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%02d:%02d".format(hour12, alarm.time.minute),
                            style = timeStyle,
                            color = ink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = meridiem,
                            style = amPmStyle,
                            color = ink,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
            ) {
                DayLetters(alarm = alarm, order = order, ink = ink, enabled = enabled)
                Spacer(modifier = Modifier.height(16.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = { if (!inSelectionMode) onToggle() },
                    enabled = !inSelectionMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = container,
                        checkedTrackColor = ink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = ink,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = ink,
                        disabledCheckedThumbColor = container,
                        disabledCheckedTrackColor = ink,
                        disabledUncheckedThumbColor = ink,
                        disabledUncheckedTrackColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DayLetters(alarm: Alarm, order: List<DayOfWeek>, ink: Color, enabled: Boolean) {
    val text = remember(alarm.days, alarm.isOneShot, order, ink, enabled) {
        buildAnnotatedString {
            order.forEach { day ->
                val active = !alarm.isOneShot && day in alarm.days
                val isBold = enabled && active
                withStyle(
                    SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) ink else ink.copy(alpha = 0.30f),
                    )
                ) {
                    append(day.name.take(1) + " ")
                }
            }
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, letterSpacing = 2.sp), 
        maxLines = 1
    )
}
