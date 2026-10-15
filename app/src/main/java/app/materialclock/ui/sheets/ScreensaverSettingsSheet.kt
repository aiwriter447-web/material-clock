package app.materialclock.ui.sheets

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.materialclock.data.ScreensaverBackgroundType
import app.materialclock.data.ScreensaverSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreensaverSettingsSheet(
    settings: ScreensaverSettings,
    onChange: ((ScreensaverSettings) -> ScreensaverSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Screensaver Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // Android System Setup Shortcut
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "System Screensaver Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "To use bedside clock while charging, select Material Clock in Android System Screensaver settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open System Screensaver Settings")
                        }
                    }
                }
            }

            // Display Options Section
            item {
                Text(
                    text = "Display Elements",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column {
                        // Show Date Switch
                        ListItem(
                            headlineContent = { Text("Show Date") },
                            supportingContent = { Text("Display current date below clock") },
                            trailingContent = {
                                Switch(
                                    checked = settings.showDate,
                                    onCheckedChange = { v -> onChange { it.copy(showDate = v) } }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        // Show Seconds Switch
                        ListItem(
                            headlineContent = { Text("Show Seconds") },
                            supportingContent = { Text("Update time every second") },
                            trailingContent = {
                                Switch(
                                    checked = settings.showSeconds,
                                    onCheckedChange = { v -> onChange { it.copy(showSeconds = v) } }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        // Show Battery Switch
                        ListItem(
                            headlineContent = { Text("Show Battery") },
                            supportingContent = { Text("Display battery percentage while charging") },
                            trailingContent = {
                                Switch(
                                    checked = settings.showBattery,
                                    onCheckedChange = { v -> onChange { it.copy(showBattery = v) } }
                                )
                            }
                        )
                    }
                }
            }

            // Appearance & Night Mode
            item {
                Text(
                    text = "Appearance & Night Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column {
                        // Background Type Selection
                        ListItem(
                            headlineContent = { Text("OLED Pure Black Background") },
                            supportingContent = { Text("True black (#000000) surfaces for OLED screen protection") },
                            trailingContent = {
                                Switch(
                                    checked = settings.backgroundType == ScreensaverBackgroundType.OLED_BLACK,
                                    onCheckedChange = { isOled ->
                                        val type = if (isOled) ScreensaverBackgroundType.OLED_BLACK else ScreensaverBackgroundType.LCD_DARK_GRAY
                                        onChange { it.copy(backgroundType = type) }
                                    }
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        // Night Mode Toggle
                        ListItem(
                            headlineContent = { Text("Night Mode (Dim Clock)") },
                            supportingContent = { Text("Reduce clock brightness for bedside use") },
                            trailingContent = {
                                Switch(
                                    checked = settings.nightModeEnabled,
                                    onCheckedChange = { v -> onChange { it.copy(nightModeEnabled = v) } }
                                )
                            }
                        )
                        if (settings.nightModeEnabled) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Clock Dimming Level", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${(settings.clockDimmingFactor * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = settings.clockDimmingFactor,
                                    onValueChange = { v -> onChange { it.copy(clockDimmingFactor = v) } },
                                    valueRange = 0.1f..0.8f
                                )
                            }
                        }
                    }
                }
            }

            // Protection Section
            item {
                Text(
                    text = "Protection",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    ListItem(
                        headlineContent = { Text("Smart Adaptive Pixel Shift") },
                        supportingContent = { Text("Shift clock position every few minutes to reduce burn-in risk") },
                        trailingContent = {
                            Switch(
                                checked = settings.pixelShiftEnabled,
                                onCheckedChange = { v -> onChange { it.copy(pixelShiftEnabled = v) } }
                            )
                        }
                    )
                }
            }
        }
    }
}
