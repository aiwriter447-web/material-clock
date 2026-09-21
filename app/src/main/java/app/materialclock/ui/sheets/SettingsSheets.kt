package app.materialclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.materialclock.data.ClockSettings
import app.materialclock.data.DarkMode
import app.materialclock.data.DismissMethod
import app.materialclock.data.HourFormat
import app.materialclock.data.WeekStart
import app.materialclock.ui.theme.Palette
import app.materialclock.widget.ClockWidgetProvider
import app.materialclock.widget.WidgetEntryPoints
import app.materialclock.ui.rememberWallTicker
import app.materialclock.ui.rememberElapsedTicker

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetTitle("Alarm settings")

            SectionLabel("Ringing")
            ChoiceRow(
                title = "Silence after",
                value = settings.alarms.silenceAfterMinutes,
                options = listOf(1, 5, 10, 15, 20, 25, 30, 0),
                label = ::minutesLabel,
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(silenceAfterMinutes = v)) } },
            )
            ChoiceRow(
                title = "Snooze length",
                value = settings.alarms.snoozeMinutes,
                options = listOf(1, 5, 10, 15, 20, 30),
                label = ::minutesLabel,
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = v)) } },
            )
            DefaultToneRow(
                soundUri = settings.alarms.defaultSoundUri,
                onPick = { v -> onChange { it.copy(alarms = it.alarms.copy(defaultSoundUri = v)) } },
            )
            VolumeRow(
                volume = settings.alarms.volume,
                onChange = { v -> onChange { it.copy(alarms = it.alarms.copy(volume = v)) } },
            )
            SwitchRow(
                title = "Volume buttons during alarm",
                subtitle = "Control volume",
                checked = settings.alarms.volumeButtonsControlVolume,
                onChange = { v ->
                    onChange { it.copy(alarms = it.alarms.copy(volumeButtonsControlVolume = v)) }
                },
            )
            ChoiceRow(
                title = "Dismiss alarm with",
                value = settings.alarms.dismissMethod,
                options = DismissMethod.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(dismissMethod = v)) } },
            )

            SectionLabel("Permissions")
            NotificationPermissionRow()
            ExactAlarmPermissionRow()
            FullScreenIntentRow()

            SectionLabel("Schedule")
            ChoiceRow(
                title = "Start week on",
                value = settings.alarms.weekStart,
                options = WeekStart.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(weekStart = v)) } },
            )

        }
    }
}

@Composable
private fun DefaultToneRow(soundUri: String?, onPick: (String?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            onPick(picked?.toString() ?: SILENT)
        }
    }
    val name = remember(soundUri) {
        when {
            soundUri == null -> "Default alarm sound"
            soundUri == SILENT -> "Silent"
            else -> runCatching {
                RingtoneManager.getRingtone(context, Uri.parse(soundUri))?.getTitle(context)
            }.getOrNull() ?: "Default alarm sound"
        }
    }
    NavigateRow(
        title = "Default alarm tone",
        subtitle = name,
        onClick = {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    soundUri?.takeIf { it != SILENT }?.let(Uri::parse)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                )
            }
            launcher.launch(intent)
        },
    )
}

@Composable
private fun VolumeRow(volume: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
        Text("Alarm volume", style = MaterialTheme.typography.bodyLarge)
        androidx.compose.material3.Slider(value = volume, onValueChange = onChange)
    }
}

@Composable
private fun NotificationPermissionRow() {
    val context = LocalContext.current
    val granted = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    if (granted) return
    NavigateRow(
        title = "Notifications",
        subtitle = "Off — a ringing alarm still sounds, but its controls won't show",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }
        },
    )
}

@Composable
private fun ChangeDateTimeRow() {
    val context = LocalContext.current
    NavigateRow(
        title = "Change date & time",
        subtitle = "Opens your device's date & time settings",
        onClick = {
            runCatching {
                context.startActivity(Intent(android.provider.Settings.ACTION_DATE_SETTINGS))
            }
        },
    )
}

@Composable
private fun ExactAlarmPermissionRow() {
    val context = LocalContext.current
    if (app.materialclock.alarm.AlarmScheduler.canScheduleExact(context)) return
    NavigateRow(
        title = "Alarms & reminders",
        subtitle = "Not allowed, so alarms may fire late or not at all",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(android.net.Uri.parse("package:${context.packageName}"))
                )
            }
        },
    )
}

@Composable
private fun WidgetRows() {
    val context = LocalContext.current
    if (WidgetEntryPoints.canPin(context)) {
        NavigateRow(
            title = "Add a clock widget",
            subtitle = "Long-press it on the home screen to change its look",
            onClick = { WidgetEntryPoints.requestPin(context) },
        )
    } else {
        NavigateRow(
            title = "Clock widget",
            subtitle = "Add one from your home screen's widget picker",
            onClick = {},
        )
    }
}

@Composable
private fun FullScreenIntentRow() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val context = LocalContext.current
    val nm = context.getSystemService(android.app.NotificationManager::class.java)
    if (nm?.canUseFullScreenIntent() != false) return
    NavigateRow(
        title = "Full-screen alarms",
        subtitle = "Not allowed, so a ringing alarm will show as a banner instead",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(android.net.Uri.parse("package:${context.packageName}"))
                )
            }
        },
    )
}

@Composable
private fun PaletteRow(
    dynamic: Boolean,
    selected: Palette,
    onDynamic: () -> Unit,
    onSelect: (Palette) -> Unit,
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val context = LocalContext.current
    val wallpaper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        } else {
            androidx.compose.material3.dynamicLightColorScheme(context)
        }
    } else {
        null
    }

    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (wallpaper != null) {
            item(key = "wallpaper") {
                Swatch("Wallpaper", wallpaper, dynamic, onDynamic)
            }
        }
        items(Palette.entries, key = { it.name }) { palette ->
            Swatch(
                name = palette.displayName,
                scheme = if (dark) palette.dark else palette.light,
                selected = !dynamic && palette == selected,
                onClick = { onSelect(palette) },
            )
        }
    }
}

@Composable
private fun Swatch(
    name: String,
    scheme: androidx.compose.material3.ColorScheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        0f to scheme.primary,
                        0.5f to scheme.tertiary,
                        1f to scheme.secondary,
                    )
                )
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                    shape = CircleShape,
                )
                .clickable(role = Role.RadioButton, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SheetTitle("Appearance")
            ChoiceRow(
                title = "Theme",
                value = settings.theme.darkMode,
                options = DarkMode.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(theme = it.theme.copy(darkMode = v)) } },
            )
            SwitchRow(
                title = "Pure black",
                subtitle = "True black surfaces in dark mode, for OLED screens",
                checked = settings.theme.amoledBlack,
                onChange = { v -> onChange { it.copy(theme = it.theme.copy(amoledBlack = v)) } },
            )
            SwitchRow(
                title = "One-hand mode",
                subtitle = "Long-press the dock to pull the screen down into thumb reach",
                checked = settings.theme.oneHandMode,
                onChange = { v -> onChange { it.copy(theme = it.theme.copy(oneHandMode = v)) } },
            )

            PaletteRow(
                dynamic = settings.theme.dynamicColor,
                selected = settings.theme.palette,
                onDynamic = { onChange { it.copy(theme = it.theme.copy(dynamicColor = true)) } },
                onSelect = { v ->
                    onChange { it.copy(theme = it.theme.copy(palette = v, dynamicColor = false)) }
                },
            )

            SectionLabel("Widgets")
            WidgetRows()

            AboutRows()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingHome by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val now by app.materialclock.ui.rememberWallTicker()
    val homeCityName = remember(settings.world.homeZoneOverride) {
        settings.world.homeZoneOverride?.let { id ->
            runCatching { java.time.ZoneId.of(id) }.getOrNull()?.let { zone ->
                zone.id.substringAfterLast('/').replace('_', ' ')
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            SheetTitle("World clock settings")
            SectionLabel("Display")
            SwitchRow(
                title = "Display time with seconds",
                checked = settings.world.showSeconds,
                onChange = { v -> onChange { it.copy(world = it.world.copy(showSeconds = v)) } },
            )
            ChoiceRow(
                title = "Time format",
                value = settings.world.hourFormat,
                options = HourFormat.entries,
                label = { it.label },
                onSelect = { v -> onChange { it.copy(world = it.world.copy(hourFormat = v)) } },
            )

            SectionLabel("Home clock")
            SwitchRow(
                title = "Automatic home clock",
                subtitle = "While travelling in an area where the time is different, " +
                    "add a clock for home",
                checked = settings.world.homeZoneOverride == null,
                onChange = { v ->
                    if (v) {
                        onChange { it.copy(world = it.world.copy(homeZoneOverride = null)) }
                    } else {
                        pickingHome = true
                    }
                },
            )
            if (settings.world.homeZoneOverride != null) {
                NavigateRow(
                    title = "Home time zone",
                    subtitle = homeCityName ?: "Not set",
                    onClick = { pickingHome = true },
                )
            }

            SectionLabel("Date & time")
            ChangeDateTimeRow()
        }
    }

    if (pickingHome) {
        AddCitySheet(
            existing = emptyList(),
            nowUtcMillis = now,
            onAdd = { city ->
                onChange { it.copy(world = it.world.copy(homeZoneOverride = city.zone.id)) }
            },
            onDismiss = { pickingHome = false },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            onChange { it.copy(timers = it.timers.copy(soundUri = picked?.toString() ?: SILENT)) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            SheetTitle("Timer settings")
            NavigateRow(
                title = "Timer sound",
                subtitle = when (val u = settings.timers.soundUri) {
                    null -> "Default alarm sound"
                    SILENT -> "Silent"
                    else -> runCatching {
                        RingtoneManager.getRingtone(context, Uri.parse(u))?.getTitle(context)
                    }.getOrNull() ?: "Default alarm sound"
                },
                onClick = {
                    picker.launch(
                        Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Timer sound")
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    )
                },
            )
            SwitchRow(
                title = "Timer vibrate",
                checked = settings.timers.vibrate,
                onChange = { v -> onChange { it.copy(timers = it.timers.copy(vibrate = v)) } },
            )
            SwitchRow(
                title = "Gradually increase volume",
                subtitle = "Starts quiet and builds up over the first 20 seconds",
                checked = settings.timers.gradualVolume,
                onChange = { v -> onChange { it.copy(timers = it.timers.copy(gradualVolume = v)) } },
            )
        }
    }
}

private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}
