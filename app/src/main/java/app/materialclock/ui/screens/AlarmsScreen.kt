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
import app.materialclock.ui.ClockViewModel
import app.materialclock.util.TimeUtils

@Composable
fun AlarmsScreen(
    viewModel: ClockViewModel
) {
    val alarms by viewModel.alarms.collectAsState()
    val is24Hour by viewModel.is24HourFormat.collectAsState()
    val groupedAlarms = alarms.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "24-Hour Format",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = is24Hour,
                onCheckedChange = { viewModel.toggleTimeFormat() }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedAlarms.forEach { (category, alarmList) ->
                item(key = category) {
                    AlarmGroupHeader(
                        category = category,
                        alarmList = alarmList,
                        onGroupToggle = { enabled ->
                            viewModel.toggleAlarmGroup(category, enabled)
                        },
                        is24Hour = is24Hour,
                        onAlarmToggle = { alarmId ->
                            viewModel.toggleAlarm(alarmId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmGroupHeader(
    category: String,
    alarmList: List<Alarm>,
    onGroupToggle: (Boolean) -> Unit,
    is24Hour: Boolean,
    onAlarmToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val isGroupOn = alarmList.any { it.isEnabled }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
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
                        onCheckedChange = onGroupToggle
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
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
                        onToggle = { onAlarmToggle(alarm.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: Alarm,
    is24Hour: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
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
