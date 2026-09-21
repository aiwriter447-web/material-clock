package app.materialclock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.materialclock.data.Alarm
import app.materialclock.util.TimeUtils
import java.time.DayOfWeek

@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    weekStart: DayOfWeek = DayOfWeek.MONDAY,
    onToggle: (Alarm) -> Unit,
    onEdit: (Alarm) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    is24Hour: Boolean = false
) {
    val groupedAlarms = alarms.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        groupedAlarms.forEach { (category, alarmList) ->
            item(key = category) {
                var isExpanded by remember { mutableStateOf(true) }
                val isGroupOn = alarmList.any { it.isEnabled }

                Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = isGroupOn,
                                    onCheckedChange = { enabled ->
                                        alarmList.forEach { alarm ->
                                            if (alarm.isEnabled != enabled) onToggle(alarm)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            alarmList.forEach { alarm ->
                                AlarmItemCard(
                                    alarm = alarm,
                                    is24Hour = is24Hour,
                                    onToggle = { onToggle(alarm) },
                                    onClick = { onEdit(alarm) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: Alarm,
    is24Hour: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = TimeUtils.formatAlarmTime(alarm.hour, alarm.minute, is24Hour),
                    style = MaterialTheme.typography.headlineMedium
                )
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
