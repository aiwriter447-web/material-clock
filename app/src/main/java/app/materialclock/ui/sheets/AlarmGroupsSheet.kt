package app.materialclock.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup

/**
 * Create, rename and delete alarm groups from one place, rather than only ever able to make one
 * from inside a single alarm's own edit sheet. Reached from the Alarms tab's own settings sheet.
 *
 * Deleting a group here asks first — see [DeleteGroupDialog] — because unlike deleting an alarm,
 * where the thing you tapped delete on is the thing that goes away, deleting a group quietly moves
 * every alarm inside it rather than deleting them, which is exactly the kind of side effect a
 * confirmation is for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmGroupsSheet(
    groups: List<AlarmGroup>,
    alarms: List<Alarm>,
    onAdd: (String) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var renaming by remember { mutableStateOf<AlarmGroup?>(null) }
    var deleting by remember { mutableStateOf<AlarmGroup?>(null) }
    var creating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 24.dp)) {
            SheetTitle("Alarm groups")

            if (groups.isEmpty()) {
                Text(
                    "No groups yet. Groups let you arm or disarm a whole set of alarms — " +
                        "\"Morning\", \"Night shift\" — with one switch.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }

            groups.forEach { group ->
                val count = alarms.count { it.groupId == group.id }
                ListItem(
                    headlineContent = { Text(group.name) },
                    supportingContent = {
                        Text(if (count == 1) "1 alarm" else "$count alarms")
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { renaming = group }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Rename ${group.name}")
                            }
                            IconButton(onClick = { deleting = group }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete ${group.name}")
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            ListItem(
                headlineContent = { Text("Add group", color = MaterialTheme.colorScheme.primary) },
                leadingContent = {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().clickable { creating = true },
            )
        }
    }

    if (creating) {
        NameGroupDialog(
            title = "New group",
            initial = "",
            onConfirm = { name -> onAdd(name); creating = false },
            onDismiss = { creating = false },
        )
    }

    renaming?.let { group ->
        NameGroupDialog(
            title = "Rename group",
            initial = group.name,
            onConfirm = { name -> onRename(group.id, name); renaming = null },
            onDismiss = { renaming = null },
        )
    }

    deleting?.let { group ->
        DeleteGroupDialog(
            group = group,
            alarmCount = alarms.count { it.groupId == group.id },
            onConfirm = { onDelete(group.id); deleting = null },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun NameGroupDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * [alarmCount] is what makes this dialog say something the person actually needs to know rather
 * than a generic "are you sure": it is the difference between deleting an empty label and quietly
 * un-grouping a dozen alarms.
 */
@Composable
private fun DeleteGroupDialog(
    group: AlarmGroup,
    alarmCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete \"${group.name}\"?") },
        text = {
            Text(
                if (alarmCount > 0) {
                    "The group is removed. Its " +
                        (if (alarmCount == 1) "1 alarm keeps" else "$alarmCount alarms keep") +
                        " ringing as normal — they just won't be in a group anymore."
                } else {
                    "This group has no alarms in it."
                },
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
