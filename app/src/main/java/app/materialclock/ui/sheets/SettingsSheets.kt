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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/* ── Modern Card & Row UI Helpers ─────────────────────────────────────────────────── */

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItemRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "itemScale"
    )

    Surface(
        color = Color.Transparent,
        onClick = onClick ?: {},
        enabled = onClick != null,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
private fun ValueBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
private fun NavigateRow(title: String, subtitle: String? = null, onClick: () -> Unit) {
    SettingsItemRow(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
    )
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    value: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        SettingsItemRow(
            title = title,
            onClick = { expanded = true },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ValueBadge(text = label(value))
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            options.forEach { option ->
                val selected = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label(option),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = if (selected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    SettingsItemRow(
        title = title,
        subtitle = subtitle,
        onClick = { onChange(!checked) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    )
}

/* ── Sheets Implementation ──────────────────────────────────────────────────────── */

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
                        subtitle = "Alarm groups • Create, rename or delete",
                        onClick = onManageGroups,
                    )
                    ItemDivider()
                    ChoiceRow(
                        title = "Start week on",
                        value = settings.alarms.weekStart,
                        options = WeekStart.entries,
                        label = { it.label },
                        onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(weekStart = v)) } },
                    )
                    ItemDivider()
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
                Spacer(Modifier.height(14.dp))
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
                    ItemDivider()
                    ChoiceRow(
                        title = "Snooze length",
                        value = settings.alarms.snoozeMinutes,
                        options = listOf(1, 5, 10, 15, 20, 30),
                        label = ::minutesLabel,
                        onSelect = { v -> onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = v)) } },
                    )
                    ItemDivider()
                    ChoiceRow(
                        title = "Upcoming alarm notification",
                        value = settings.alarms.upcomingNotificationMinutes,
                        options = listOf(0, 5, 10, 15, 30, 60),
                        label = { if (it == 0) "Off" else "$it minutes before" },
                        onSelect = { v ->
                            onChange { it.copy(alarms = it.alarms.copy(upcomingNotificationMinutes = v)) }
                        },
                    )
                    ItemDivider()
                    DefaultToneRow(
                        soundUri = settings.alarms.defaultSoundUri,
                        onPick = { v -> onChange { it.copy(alarms = it.alarms.copy(defaultSoundUri = v)) } },
                    )
                    ItemDivider()
                    VolumeRow(
                        volume = settings.alarms.volume,
                        onChange = { v -> onChange { it.copy(alarms = it.alarms.copy(volume = v)) } },
                    )
                    ItemDivider()
                    SwitchRow(
                        title = "Volume buttons during alarm",
                        subtitle = "Control alarm volume with hardware keys",
                        checked = settings.alarms.volumeButtonsControlVolume,
                        onChange = { v ->
                            onChange { it.copy(alarms = it.alarms.copy(volumeButtonsControlVolume = v)) }
                        },
                    )
                }
            }

            item { 
                Spacer(Modifier.height(14.dp))
                SectionLabel("Permissions & Alerts") 
            }
            item {
                SettingsCard {
                    NotificationPermissionRow()
                    ItemDivider()
                    ExactAlarmPermissionRow()
                    ItemDivider()
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
            soundUri == null -> "Default sound"
            soundUri == SILENT -> "Silent"
            else -> runCatching {
                RingtoneManager.getRingtone(context, Uri.parse(soundUri))?.getTitle(context)
            }.getOrNull() ?: "Default sound"
        }
    }
    SettingsItemRow(
        title = "Default alarm tone",
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
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ValueBadge(text = name)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
private fun VolumeRow(volume: Float, onChange: (Float) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Alarm volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        Slider(
            value = volume, 
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NotificationPermissionRow() {
    val context = LocalContext.current
    val granted = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    NavigateRow(
        title = "Notifications",
        subtitle = if (granted) "Allowed" else "Off — controls won't show on lockscreen",
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
        subtitle = "Opens system date & time settings",
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
        subtitle = if (granted) "Allowed" else "Not allowed — alarms may fire late",
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
            subtitle = "Long-press on home screen to customize",
            onClick = { WidgetEntryPoints.requestPin(context) },
        )
    } else {
        NavigateRow(
            title = "Clock widget",
            subtitle = "Add from your launcher's widget picker",
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
        subtitle = if (granted) "Allowed to open full screen when ringing" else "Not allowed — ringing alarm shows as banner",
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
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
                    ItemDivider()
                    SwitchRow(
                        title = "Pure black",
                        subtitle = "True black surfaces in dark mode for OLED",
                        checked = settings.theme.amoledBlack,
                        onChange = { v -> onChange { it.copy(theme = it.theme.copy(amoledBlack = v)) } },
                    )
                    ItemDivider()
                    SwitchRow(
                        title = "One-hand mode",
                        subtitle = "Long-press dock to pull screen into thumb reach",
                        checked = settings.theme.oneHandMode,
                        onChange = { v -> onChange { it.copy(theme = it.theme.copy(oneHandMode = v)) } },
                    )
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
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
                Spacer(Modifier.height(14.dp))
                SectionLabel("Widgets") 
            }
            item {
                SettingsCard { WidgetRows() }
            }

            item {
                Spacer(Modifier.height(14.dp))
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
                    ItemDivider()
                    SwitchRow(
                        title = "Display time with seconds",
                        checked = settings.world.showSeconds,
                        onChange = { v -> onChange { it.copy(world = it.world.copy(showSeconds = v)) } },
                    )
                    ItemDivider()
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
                Spacer(Modifier.height(14.dp))
                SectionLabel("Home clock") 
            }
            item {
                SettingsCard {
                    SwitchRow(
                        title = "Automatic home clock",
                        subtitle = "Add a clock for home while travelling",
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
                        ItemDivider()
                        NavigateRow(
                            title = "Home time zone",
                            subtitle = homeCityName ?: "Not set",
                            onClick = { pickingHome = true },
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
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
                    ItemDivider()
                    SwitchRow(
                        title = "Timer vibrate",
                        checked = settings.timers.vibrate,
                        onChange = { v -> onChange { it.copy(timers = it.timers.copy(vibrate = v)) } },
                    )
                    ItemDivider()
                    SwitchRow(
                        title = "Gradually increase volume",
                        subtitle = "Starts quiet and builds up over first 20s",
                        checked = settings.timers.gradualVolume,
                        onChange = { v -> onChange { it.copy(timers = it.timers.copy(gradualVolume = v)) } },
                    )
                }
            }
        }
    }
}

private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}
