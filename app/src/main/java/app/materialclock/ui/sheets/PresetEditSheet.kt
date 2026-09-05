package app.materialclock.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.materialclock.core.TimerPreset

private const val MIN_MINUTES = 1
private const val MAX_MINUTES = 180

/**
 * Add or edit a [TimerPreset]. `initial.id == 0` is a new preset — the same "0 means insert"
 * convention [AlarmEditSheet] uses — and is the only case with no Delete button, since there is
 * nothing on disk yet to delete.
 *
 * No live-commit here unlike the alarm sheet: a preset is two fields and a much smaller
 * accidental-dismiss cost, so the simpler rule — only the Save button writes anything — is the
 * clearer contract for something this small.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PresetEditSheet(
    initial: TimerPreset,
    onDismiss: () -> Unit,
    onSave: (TimerPreset) -> Unit,
    onDelete: ((Long) -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable(initial.id) { mutableStateOf(initial.name) }
    var minutes by rememberSaveable(initial.id) {
        mutableStateOf((initial.totalSeconds / 60).coerceIn(MIN_MINUTES, MAX_MINUTES))
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp).padding(bottom = 20.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("Study, Deep work, Break…") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            // Minutes only, not the full h:m:s field the timer itself uses: a preset is a length
            // you reuse often, and that is a round number of minutes in practice far more often
            // than it is an exact number of seconds.
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = { minutes = (minutes - 1).coerceIn(MIN_MINUTES, MAX_MINUTES) }) {
                    Text("−", style = MaterialTheme.typography.headlineMedium)
                }
                Text(
                    "$minutes min",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(onClick = { minutes = (minutes + 1).coerceIn(MIN_MINUTES, MAX_MINUTES) }) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (onDelete != null) {
                    IconButton(onClick = { onDelete(initial.id); onDismiss() }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete preset")
                    }
                }
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                FilledTonalButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(initial.copy(name = name.trim(), totalSeconds = minutes * 60))
                        }
                        onDismiss()
                    },
                ) { Text("Save") }
            }
        }
    }
}
