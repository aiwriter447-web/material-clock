package app.materialclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.materialclock.data.ClockSettings
import app.materialclock.data.DarkMode
import app.materialclock.data.DismissMethod
import app.materialclock.data.HourFormat
import app.materialclock.data.WeekStart
import app.materialclock.data.WorldClockStyle
import app.materialclock.ui.theme.Palette
import app.materialclock.widget.ClockWidgetProvider
import app.materialclock.widget.WidgetEntryPoints

/**
 * Modern Card Wrapper for Settings Sections
 * Groups related settings visually without clutter.
 */
@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onManageGroups: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        // Changed to LazyColumn to eliminate the "earthquake jitter" bug during scrolling
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SheetTitle("Alarm settings")
                Spacer(Modifier.height(8.dp))
            }

            item { SectionLabel("Schedule & Behavior") }
            item {
                SettingsCard {
                    NavigateRow(
                        title = "Groups",
                        subtitle = "Alarm groups\nCreate, rename or delete",
                        onClick = onManageGroups,
                    )
                    ChoiceRow(
                        title = "Start week on",
                        value = settings.alarms.weekStart,
                        options = WeekStart.entries,
                        label = { it.label },
                        onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(weekStart = v)) } },
                    )
                    ChoiceRow(
                        title = "Dismiss alarm with",
                        value = settings.alarms.dismissMethod,
                        options = DismissMethod.entries,
                        label = { it.label },
                        onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(dismissMethod = v)) } },
                    )
                }
            }

            item { 
                Spacer(Modifier.height(16.dp))
                SectionLabel("Ringing & Sound") 
            }
            item {
                SettingsCard {
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
                    ChoiceRow(
                        title = "Upcoming alarm notification",
                        value = settings.alarms.upcomingNotificationMinutes,
                        options = listOf(0, 5, 10, 15, 30, 60),
                        label = { if (it == 0) "Off" else "$it minutes before" },
                        onSelect = { v ->
                            onChange { it.copy(alarms = it.alarms.copy(upcomingNotificationMinutes = v)) }
                        },
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
                }
            }

            item { 
                Spacer(Modifier.height(16.dp))
                SectionLabel("Permissions & Alerts") 
            }
            item {
                SettingsCard {
                    NotificationPermissionRow()
                    ExactAlarmPermissionRow()
                    FullScreenIntentRow()
                }
            }
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
    Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text("Alarm volume", style = MaterialTheme.typography.bodyLarge)
        androidx.compose.material3.Slider(
            value = volume, 
            onValueChange = onChange,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun NotificationPermissionRow() {
    val context = LocalContext.current
    val granted = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    NavigateRow(
        title = "Notifications",
        subtitle = if (granted) "Allowed" else "Off — a ringing alarm still sounds, but its controls won't show",
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
    val granted = app.materialclock.alarm.AlarmScheduler.canScheduleExact(context)
    NavigateRow(
        title = "Alarms and reminders",
        subtitle = if (granted) "Allowed" else "Not allowed, so alarms may fire late or not at all",
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
    val granted = nm?.canUseFullScreenIntent() != false
    NavigateRow(
        title = "Full screen alerts",
        subtitle = if (granted) {
            "Allow apps to send full screen alerts that cover the entire screen."
        } else {
            "Not allowed, so a ringing alarm will show as a banner instead"
        },
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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth, bouncy tactile scaling effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swatchScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
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
                        Color.Transparent
                    },
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.RadioButton, 
                    onClick = onClick
                ),
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
        Spacer(Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SheetTitle("Appearance")
                Spacer(Modifier.height(8.dp))
            }

            item {
                SettingsCard {
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
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Theme Colors")
                PaletteRow(
                    dynamic = settings.theme.dynamicColor,
                    selected = settings.theme.palette,
                    onDynamic = { onChange { it.copy(theme = it.theme.copy(dynamicColor = true)) } },
                    onSelect = { v ->
                        onChange { it.copy(theme = it.theme.copy(palette = v, dynamicColor = false)) }
                    },
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Widgets") 
            }
            item {
                SettingsCard { WidgetRows() }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SettingsCard { AboutRows() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingHome by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
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
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SheetTitle("World clock settings")
                Spacer(Modifier.height(8.dp))
            }

            item { SectionLabel("Display") }
            item {
                SettingsCard {
                    ChoiceRow(
                        title = "Style",
                        value = settings.world.style,
                        options = WorldClockStyle.entries,
                        label = { it.label },
                        onSelect = { v -> onChange { it.copy(world = it.world.copy(style = v)) } },
                    )
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
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Home clock") 
            }
            item {
                SettingsCard {
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
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Date & time") 
            }
            item {
                SettingsCard { ChangeDateTimeRow() }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
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
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                SheetTitle("Timer settings")
                Spacer(Modifier.height(8.dp))
            }

            item {
                SettingsCard {
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
    }
}

/** 0 is never, 1 is singular. Both of these read wrong the moment they are not special-cased. */
private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}
