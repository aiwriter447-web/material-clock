package app.materialclock.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.materialclock.core.TimerPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditSheet(
    initial: TimerPreset,
    onDismiss: () -> Unit,
    onSave: (TimerPreset) -> Unit,
    onDelete: ((Long) -> Unit)? = null // No longer used in UI, but kept in signature to not break other files
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(initial.name) { mutableStateOf(initial.name) }
    var mins by remember(initial.totalSeconds) { mutableIntStateOf(initial.totalSeconds / 60) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = if (initial.id == 0L) "New Preset" else "Edit Preset",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { if (mins > 1) mins-- }) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = "$mins min",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                IconButton(onClick = { if (mins < 99) mins++ }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Increase")
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank() && mins > 0) {
                            onSave(initial.copy(name = name.trim(), totalSeconds = mins * 60))
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && mins > 0
                ) {
                    Text("Save")
                }
            }
        }
    }
}
