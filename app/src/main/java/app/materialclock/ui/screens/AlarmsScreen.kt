package app.materialclock.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.sheets.systemFirstDay
import app.materialclock.ui.theme.CapText
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import app.materialclock.ui.theme.StretchedCaps
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    groups: List<AlarmGroup>,
    weekStart: WeekStart,
    onToggle: (Long) -> Unit,
    onToggleGroup: (Long, Boolean) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val order = remember(weekStart) {
        weekStart.order(systemFirstDay())
    }

    val context = LocalContext.current
    val is24Hour = remember(context) {
        android.text.format.DateFormat.is24HourFormat(context)
    }

    val byGroup = remember(alarms) { alarms.groupBy { it.groupId } }
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    alarmToDelete?.let { alarm ->
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text("Delete Alarm") },
            text = { Text("Are you sure you want to delete this alarm?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(alarm)
                        alarmToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "upcoming-alarm-text") {
            val upcomingInfo = remember(alarms) { calculateTimeUntilNextAlarm(alarms) }
            // Smooth fluid animation for opening and closing text
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
            item(key = "group-cards") {
                GroupCardsSection(
                    groups = groups,
                    byGroup = byGroup,
                    onToggleGroup = onToggleGroup,
                )
            }
        }

        items(alarms, key = { it.id }) { alarm ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    when (value) {
                        SwipeToDismissBoxValue.EndToStart -> {
                            alarmToDelete = alarm
                            false
                        }
                        SwipeToDismissBoxValue.StartToEnd -> {
                            if (alarm.enabled) {
                                Toast.makeText(context, "Alarm snoozed", Toast.LENGTH_SHORT).show()
                            } else {
                                onEdit(alarm)
                            }
                            false
                        }
                        else -> false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true, // LTR enabled for snooze/group
                enableDismissFromEndToStart = true, // RTL enabled for delete
                modifier = Modifier.animateItem(
                    placementSpec = tween(400, easing = FastOutSlowInEasing) // Fluid landing
                ),
                backgroundContent = {
                    val direction = dismissState.dismissDirection
                    val alignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                    
                    val icon = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> if (alarm.enabled) Icons.Rounded.Snooze else Icons.Rounded.FolderSpecial
                        SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
                        else -> Icons.Outlined.Delete
                    }
                    
                    val bgColor = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> if (alarm.enabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                        else -> Color.Transparent
                    }
                    
                    val iconColor = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> if (alarm.enabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                        else -> Color.Transparent
                    }

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            ) {
                AlarmRow(
                    alarm = alarm,
                    order = order,
                    is24Hour = is24Hour,
                    onToggle = { onToggle(alarm.id) },
                    onEdit = { onEdit(alarm) },
                )
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
    onToggleGroup: (Long, Boolean) -> Unit,
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
                    GroupCard(
                        group = group,
                        total = groupAlarms.size,
                        armed = groupAlarms.count { it.enabled },
                        checked = groupAlarms.isNotEmpty() && groupAlarms.all { it.enabled },
                        onToggle = { onToggleGroup(group.id, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: AlarmGroup,
    total: Int,
    armed: Int,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (checked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val ink = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(GROUP_CARD_CORNER),
        modifier = modifier,
    ) {
        Column(Modifier.padding(GROUP_CARD_PADDING)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = checked, onCheckedChange = onToggle)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GroupCount(icon = Icons.Outlined.Alarm, count = total, ink = ink)
                Spacer(Modifier.width(16.dp))
                GroupCount(icon = Icons.Filled.Alarm, count = armed, ink = ink)
            }
        }
    }
}

@Composable
private fun GroupCount(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, ink: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ink.copy(alpha = 0.75f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(count.toString(), style = MaterialTheme.typography.labelLarge, color = ink)
    }
}

private val ROW_HORIZONTAL_PADDING = 16.dp
private val ROW_VERTICAL_PADDING = 10.dp
private val ROW_CORNER_RADIUS = 24.dp
private val ROW_LABEL_TO_TIME = 0.dp
private val ROW_TIME_GAP = 8.dp
private val ROW_TIME_CAP = 104.dp
private const val ROW_MERIDIEM_CAP_FRACTION = 0.30f

@Composable
private fun AlarmRow(
    alarm: Alarm,
    order: List<DayOfWeek>,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    val enabled = alarm.enabled
    val container = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val ink = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val numeralWeight = if (enabled) ClockFace.WEIGHT_ON else ClockFace.WEIGHT_OFF
    val hour12 = (alarm.time.hour % 12).takeIf { it != 0 } ?: 12
    val meridiem = if (alarm.time.hour < 12) "AM" else "PM"

    val displayLabel = if (alarm.label.isNotBlank()) alarm.label else " "
    val labelColor = if (alarm.label.isNotBlank()) ink else Color.Transparent

    Surface(
        color = container,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(ROW_LABEL_TO_TIME))

                if (is24Hour) {
                    RowTime24(hour = alarm.time.hour, minute = alarm.time.minute, ink = ink, weight = numeralWeight)
                } else {
                    RowTime12(hour12 = hour12, minute = alarm.time.minute, meridiem = meridiem, ink = ink, weight = numeralWeight)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
            ) {
                DayLetters(alarm = alarm, order = order, ink = ink)
                Spacer(modifier = Modifier.height(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() },
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
private fun RowTime24(hour: Int, minute: Int, ink: Color, weight: Int) {
    Numerals(
        text = "%02d:%02d".format(hour, minute),
        capHeight = ROW_TIME_CAP,
        color = ink,
        width = ClockFace.CONDENSED,
        weight = weight,
        tracking = ClockFace.CONDENSED_TRACKING,
    )
}

@Composable
private fun RowTime12(hour12: Int, minute: Int, meridiem: String, ink: Color, weight: Int) {
    Row(verticalAlignment = Alignment.Bottom) {
        Numerals(
            text = "%02d".format(hour12), capHeight = ROW_TIME_CAP, color = ink, width = ClockFace.CONDENSED, weight = weight, tracking = ClockFace.CONDENSED_TRACKING
        )
        Spacer(modifier = Modifier.width(ROW_TIME_GAP))
        Numerals(
            text = "%02d".format(minute), capHeight = ROW_TIME_CAP, color = ink, width = ClockFace.CONDENSED, weight = weight, tracking = ClockFace.CONDENSED_TRACKING
        )
        Spacer(modifier = Modifier.width(ROW_TIME_GAP))
        CapText(
            text = meridiem, capHeight = ROW_TIME_CAP * ROW_MERIDIEM_CAP_FRACTION, color = ink, tracking = ClockFace.CONDENSED_TRACKING, modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

private val DAY_CAP = 23.dp
private val DAY_BLOCK_WIDTH = 91.dp
private val DAY_TRACKING = 2.2.dp

private const val DAY_WEIGHT_ON = 700
private const val DAY_WEIGHT_OFF = 400

@Composable
private fun DayLetters(alarm: Alarm, order: List<DayOfWeek>, ink: Color) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val bold = remember { ClockFace.capitals(DAY_CAP, ClockFace.CONDENSED, weight = DAY_WEIGHT_ON) }
    val light = remember { ClockFace.capitals(DAY_CAP, ClockFace.CONDENSED, weight = DAY_WEIGHT_OFF) }

    val text = remember(alarm.days, alarm.isOneShot, order, ink) {
        buildAnnotatedString {
            order.forEach { day ->
                val active = !alarm.isOneShot && day in alarm.days
                withStyle(
                    SpanStyle(
                        fontFamily = if (active) bold.fontFamily else light.fontFamily,
                        color = if (active) ink else ink.copy(alpha = 0.30f),
                    )
                ) {
                    append(day.name.take(1))
                }
            }
        }
    }

    val letterSpacing = with(density) { (DAY_TRACKING / bold.fontSize.toDp()).em }
    val style = bold.copy(letterSpacing = letterSpacing)
    val measuredWidth = measurer.measure(text = text, style = style).size.width
    val targetWidth = with(density) { DAY_BLOCK_WIDTH.toPx() }
    val scaleX = if (measuredWidth > 0) targetWidth / measuredWidth else 1f

    StretchedCaps(text = text, style = style, capHeight = DAY_CAP, scaleX = scaleX)
}