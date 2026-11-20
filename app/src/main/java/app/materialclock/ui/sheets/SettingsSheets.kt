package app.materialclock.ui.sheets

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.materialclock.data.ClockSettings
import app.materialclock.data.DarkMode
import app.materialclock.data.DismissMethod
import app.materialclock.data.HourFormat
import app.materialclock.data.ScreensaverBackgroundType
import app.materialclock.data.ScreensaverSettings
import app.materialclock.data.WeekStart
import app.materialclock.data.WorldClockStyle
import app.materialclock.ui.theme.Palette
import app.materialclock.widget.WidgetEntryPoints
import java.time.ZoneId

/* ── Living Flow - Fluid Motion Physics & Expressive Segmented Shapes ─────────────── */

private fun <T> fluidSpring() = spring<T>(
    dampingRatio = 0.82f,
    stiffness = Spring.StiffnessMediumLow
)

private fun segmentedShape(index: Int, count: Int): Shape {
    val outer = 24.dp
    val inner = 6.dp
    return when {
        count <= 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(
            topStart = outer,
            topEnd = outer,
            bottomStart = inner,
            bottomEnd = inner
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = inner,
            topEnd = inner,
            bottomStart = outer,
            bottomEnd = outer
        )
        else -> RoundedCornerShape(inner)
    }
}

@Composable
private fun ExpressiveSheetHeader(
    title: String,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (onBack != null) 48.dp else 12.dp)
        )
    }
}

@Composable
private fun ExpressiveSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SegmentedSurface(
    index: Int,
    count: Int,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "segmentedItemScale"
    )

    val shape = segmentedShape(index, count)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = shape,
        color = containerColor,
        onClick = onClick ?: {},
        enabled = onClick != null,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

@Composable
private fun SegmentedNavigateRow(
    index: Int,
    count: Int,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingIcon: ImageVector = Icons.Rounded.ChevronRight,
    onClick: () -> Unit,
) {
    SegmentedSurface(index = index, count = count, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
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
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SegmentedSwitchRow(
    index: Int,
    count: Int,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    SegmentedSurface(
        index = index,
        count = count,
        onClick = { onChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
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
            Switch(
                checked = checked,
                onCheckedChange = onChange
            )
        }
    }
}

@Composable
private fun <T> SegmentedChoiceRow(
    index: Int,
    count: Int,
    icon: ImageVector,
    title: String,
    value: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    SegmentedSurface(
        index = index,
        count = count,
        onClick = { open = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.widthIn(max = 132.dp)
            ) {
                Text(
                    text = label(value),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    options.forEachIndexed { optIdx, option ->
                        val selected = option == value
                        val rowBg by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            animationSpec = tween(200),
                            label = "choiceOptionBg"
                        )
                        Surface(
                            onClick = {
                                onSelect(option)
                                open = false
                            },
                            shape = segmentedShape(optIdx, options.size),
                            color = rowBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = label(option),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

/* ── 1. Alarm Settings Sheet ──────────────────────────────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onManageGroups: () -> Unit,
    onDismiss: () -> Unit,
) {
    val hasFullScreenSetting = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val permissionsCount = if (hasFullScreenSetting) 3 else 2

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ExpressiveSheetHeader("Alarm settings")
            }

            // Group 1: Schedule & Behavior (3 items)
            item { ExpressiveSectionHeader("Schedule & Behavior") }
            item {
                SegmentedNavigateRow(
                    index = 0,
                    count = 3,
                    icon = Icons.Rounded.Label,
                    title = "Groups",
                    subtitle = "Alarm groups • Create, rename or delete",
                    onClick = onManageGroups,
                )
            }
            item {
                SegmentedChoiceRow(
                    index = 1,
                    count = 3,
                    icon = Icons.Rounded.CalendarToday,
                    title = "Start week on",
                    value = settings.alarms.weekStart,
                    options = WeekStart.entries,
                    label = { it.label },
                    onSelect = { v: WeekStart ->
                        onChange { it.copy(alarms = it.alarms.copy(weekStart = v)) }
                    },
                )
            }
            item {
                SegmentedChoiceRow(
                    index = 2,
                    count = 3,
                    icon = Icons.Rounded.TouchApp,
                    title = "Dismiss alarm with",
                    value = settings.alarms.dismissMethod,
                    options = DismissMethod.entries,
                    label = { it.label },
                    onSelect = { v: DismissMethod ->
                        onChange { it.copy(alarms = it.alarms.copy(dismissMethod = v)) }
                    },
                )
            }

            // Group 2: Ringing & Sound (6 items)
            item { ExpressiveSectionHeader("Ringing & Sound") }
            item {
                SegmentedChoiceRow(
                    index = 0,
                    count = 6,
                    icon = Icons.Rounded.TimerOff,
                    title = "Silence after",
                    value = settings.alarms.silenceAfterMinutes,
                    options = listOf(1, 5, 10, 15, 20, 25, 30, 0),
                    label = ::minutesLabel,
                    onSelect = { v: Int ->
                        onChange { it.copy(alarms = it.alarms.copy(silenceAfterMinutes = v)) }
                    },
                )
            }
            item {
                SegmentedChoiceRow(
                    index = 1,
                    count = 6,
                    icon = Icons.Rounded.Snooze,
                    title = "Snooze length",
                    value = settings.alarms.snoozeMinutes,
                    options = listOf(1, 5, 10, 15, 20, 30),
                    label = ::minutesLabel,
                    onSelect = { v: Int ->
                        onChange { it.copy(alarms = it.alarms.copy(snoozeMinutes = v)) }
                    },
                )
            }
            item {
                SegmentedChoiceRow(
                    index = 2,
                    count = 6,
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Upcoming alarm notification",
                    value = settings.alarms.upcomingNotificationMinutes,
                    options = listOf(0, 5, 10, 15, 30, 60),
                    label = { if (it == 0) "Off" else "$it minutes before" },
                    onSelect = { v: Int ->
                        onChange { it.copy(alarms = it.alarms.copy(upcomingNotificationMinutes = v)) }
                    },
                )
            }
            item {
                DefaultToneRow(
                    index = 3,
                    count = 6,
                    soundUri = settings.alarms.defaultSoundUri,
                    onPick = { v: String? ->
                        onChange { it.copy(alarms = it.alarms.copy(defaultSoundUri = v)) }
                    },
                )
            }
            item {
                VolumeRow(
                    index = 4,
                    count = 6,
                    volume = settings.alarms.volume,
                    onChange = { v: Float ->
                        onChange { it.copy(alarms = it.alarms.copy(volume = v)) }
                    },
                )
            }
            item {
                SegmentedSwitchRow(
                    index = 5,
                    count = 6,
                    icon = Icons.Rounded.VolumeDown,
                    title = "Volume buttons during alarm",
                    subtitle = "Control alarm volume with hardware keys",
                    checked = settings.alarms.volumeButtonsControlVolume,
                    onChange = { v: Boolean ->
                        onChange { it.copy(alarms = it.alarms.copy(volumeButtonsControlVolume = v)) }
                    },
                )
            }

            // Group 3: Permissions & Alerts
            item { ExpressiveSectionHeader("Permissions & Alerts") }
            item {
                NotificationPermissionRow(index = 0, count = permissionsCount)
            }
            item {
                ExactAlarmPermissionRow(index = 1, count = permissionsCount)
            }
            if (hasFullScreenSetting) {
                item {
                    FullScreenIntentRow(index = 2, count = permissionsCount)
                }
            }
        }
    }
}

@Composable
private fun DefaultToneRow(
    index: Int,
    count: Int,
    soundUri: String?,
    onPick: (String?) -> Unit
) {
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

    SegmentedSurface(
        index = index,
        count = count,
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
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Default alarm tone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.widthIn(max = 128.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun VolumeRow(
    index: Int,
    count: Int,
    volume: Float,
    onChange: (Float) -> Unit
) {
    SegmentedSurface(index = index, count = count) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Alarm volume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(6.dp))
            Slider(
                value = volume,
                onValueChange = onChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun NotificationPermissionRow(index: Int, count: Int) {
    val context = LocalContext.current
    val granted = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    SegmentedNavigateRow(
        index = index,
        count = count,
        icon = Icons.Rounded.NotificationsActive,
        title = "Notifications",
        subtitle = if (granted) "Allowed" else "Off — controls won't show on lockscreen",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }
        },
    )
}

@Composable
private fun ExactAlarmPermissionRow(index: Int, count: Int) {
    val context = LocalContext.current
    val granted = app.materialclock.alarm.AlarmScheduler.canScheduleExact(context)
    SegmentedNavigateRow(
        index = index,
        count = count,
        icon = Icons.Rounded.Alarm,
        title = "Alarms and reminders",
        subtitle = if (granted) "Allowed" else "Not allowed — alarms may fire late",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:${context.packageName}"))
                )
            }
        },
    )
}

@Composable
private fun FullScreenIntentRow(index: Int, count: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val context = LocalContext.current
    val nm = context.getSystemService(android.app.NotificationManager::class.java)
    val granted = nm?.canUseFullScreenIntent() != false
    SegmentedNavigateRow(
        index = index,
        count = count,
        icon = Icons.Rounded.Fullscreen,
        title = "Full screen alerts",
        subtitle = if (granted) "Allowed to open full screen when ringing" else "Not allowed — ringing alarm shows as banner",
        onClick = {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(Uri.parse("package:${context.packageName}"))
                )
            }
        },
    )
}

/* ── 2. Appearance / Stopwatch Settings Sheet ─────────────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ExpressiveSheetHeader("Appearance")
            }

            // Group 1: Theme & Pure Black (2 items)
            item { ExpressiveSectionHeader("Display Mode") }
            item {
                SegmentedChoiceRow(
                    index = 0,
                    count = 2,
                    icon = Icons.Rounded.DarkMode,
                    title = "Theme",
                    value = settings.theme.darkMode,
                    options = DarkMode.entries,
                    label = { it.label },
                    onSelect = { v: DarkMode ->
                        onChange { it.copy(theme = it.theme.copy(darkMode = v)) }
                    },
                )
            }
            item {
                SegmentedSwitchRow(
                    index = 1,
                    count = 2,
                    icon = Icons.Rounded.Contrast,
                    title = "Pure black",
                    subtitle = "True black surfaces in dark mode for OLED",
                    checked = settings.theme.amoledBlack,
                    onChange = { v: Boolean ->
                        onChange { it.copy(theme = it.theme.copy(amoledBlack = v)) }
                    },
                )
            }

            // Group 2: Theme Colors (1 standalone card)
            item { ExpressiveSectionHeader("Theme Colors") }
            item {
                SegmentedSurface(index = 0, count = 1) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Color scheme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        PaletteRow(
                            dynamic = settings.theme.dynamicColor,
                            selected = settings.theme.palette,
                            onDynamic = {
                                onChange { it.copy(theme = it.theme.copy(dynamicColor = true)) }
                            },
                            onSelect = { v: Palette ->
                                onChange { it.copy(theme = it.theme.copy(palette = v, dynamicColor = false)) }
                            },
                        )
                    }
                }
            }

            // Group 3: Widgets (1 standalone card)
            item { ExpressiveSectionHeader("Widgets") }
            item {
                if (WidgetEntryPoints.canPin(context)) {
                    SegmentedNavigateRow(
                        index = 0,
                        count = 1,
                        icon = Icons.Rounded.Widgets,
                        title = "Add a clock widget",
                        subtitle = "Long-press on home screen to customize",
                        onClick = { WidgetEntryPoints.requestPin(context) },
                    )
                } else {
                    SegmentedNavigateRow(
                        index = 0,
                        count = 1,
                        icon = Icons.Rounded.Widgets,
                        title = "Clock widget",
                        subtitle = "Add from your launcher's widget picker",
                        onClick = {},
                    )
                }
            }

            // Group 4: About (1 standalone card)
            item { ExpressiveSectionHeader("About") }
            item {
                SegmentedSurface(index = 0, count = 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Material Clock $appVersion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteRow(
    dynamic: Boolean,
    selected: Palette,
    onDynamic: () -> Unit,
    onSelect: (Palette) -> Unit,
) {
    val dark = isSystemInDarkTheme()
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
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
        targetValue = if (isPressed) 0.85f else if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swatchScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(54.dp)
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
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/* ── 3. World Clock Settings Sheet (with Seamless In-Sheet Screensaver Page) ──────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingsSheet(
    settings: ClockSettings,
    onChange: ((ClockSettings) -> ClockSettings) -> Unit,
    screensaverSettings: ScreensaverSettings,
    onScreensaverChange: ((ScreensaverSettings) -> ScreensaverSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var showScreensaverSubpage by rememberSaveable { mutableStateOf(false) }
    var pickingHome by rememberSaveable { mutableStateOf(false) }
    val hasCustomHome = settings.world.homeZoneOverride != null
    val homeGroupCount = if (hasCustomHome) 2 else 1

    val homeCityName = remember(settings.world.homeZoneOverride) {
        settings.world.homeZoneOverride?.let { id ->
            runCatching { ZoneId.of(id) }.getOrNull()?.let { zone ->
                zone.id.substringAfterLast('/').replace('_', ' ')
            }
        }
    }

    // System back button/gesture returns smoothly from Screensaver sub-page to World Clock settings
    BackHandler(enabled = showScreensaverSubpage) {
        showScreensaverSubpage = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        AnimatedContent(
            targetState = showScreensaverSubpage,
            transitionSpec = {
                if (targetState) {
                    // Forward into Screensaver settings
                    (slideInHorizontally(animationSpec = fluidSpring()) { it / 3 } +
                        fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(animationSpec = fluidSpring()) { -it / 4 } +
                            fadeOut(tween(180)))
                } else {
                    // Back to World clock settings
                    (slideInHorizontally(animationSpec = fluidSpring()) { -it / 3 } +
                        fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(animationSpec = fluidSpring()) { it / 4 } +
                            fadeOut(tween(180)))
                }
            },
            label = "worldToScreensaverFlow"
        ) { isScreensaverPage ->
            if (isScreensaverPage) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        ExpressiveSheetHeader(
                            title = "Screensaver settings",
                            onBack = { showScreensaverSubpage = false }
                        )
                    }
                    screensaverSettingsItems(
                        settings = screensaverSettings,
                        onChange = onScreensaverChange
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        ExpressiveSheetHeader("World clock settings")
                    }

                    // Group 1: Display (4 items)
                    item { ExpressiveSectionHeader("Display") }
                    item {
                        SegmentedChoiceRow(
                            index = 0,
                            count = 4,
                            icon = Icons.Rounded.Schedule,
                            title = "Style",
                            value = settings.world.style,
                            options = WorldClockStyle.entries,
                            label = { it.label },
                            onSelect = { v: WorldClockStyle ->
                                onChange { it.copy(world = it.world.copy(style = v)) }
                            },
                        )
                    }
                    item {
                        SegmentedSwitchRow(
                            index = 1,
                            count = 4,
                            icon = Icons.Rounded.Timer,
                            title = "Display time with seconds",
                            checked = settings.world.showSeconds,
                            onChange = { v: Boolean ->
                                onChange { it.copy(world = it.world.copy(showSeconds = v)) }
                            },
                        )
                    }
                    item {
                        SegmentedChoiceRow(
                            index = 2,
                            count = 4,
                            icon = Icons.Rounded.AccessTime,
                            title = "Time format",
                            value = settings.world.hourFormat,
                            options = HourFormat.entries,
                            label = { it.label },
                            onSelect = { v: HourFormat ->
                                onChange { it.copy(world = it.world.copy(hourFormat = v)) }
                            },
                        )
                    }
                    item {
                        SegmentedNavigateRow(
                            index = 3,
                            count = 4,
                            icon = Icons.Rounded.Bedtime,
                            title = "Screensaver",
                            subtitle = "Bedside charging clock & setup",
                            onClick = { showScreensaverSubpage = true },
                        )
                    }

                    // Group 2: Home clock (1 or 2 items)
                    item { ExpressiveSectionHeader("Home clock") }
                    item {
                        SegmentedSwitchRow(
                            index = 0,
                            count = homeGroupCount,
                            icon = Icons.Rounded.Public,
                            title = "Automatic home clock",
                            subtitle = "Add a clock for home while travelling",
                            checked = !hasCustomHome,
                            onChange = { v: Boolean ->
                                if (v) {
                                    onChange { it.copy(world = it.world.copy(homeZoneOverride = null)) }
                                } else {
                                    pickingHome = true
                                }
                            },
                        )
                    }
                    if (hasCustomHome) {
                        item {
                            SegmentedNavigateRow(
                                index = 1,
                                count = homeGroupCount,
                                icon = Icons.Rounded.Language,
                                title = "Home time zone",
                                subtitle = homeCityName ?: "Not set",
                                onClick = { pickingHome = true },
                            )
                        }
                    }

                    // Group 3: Date & time (1 standalone item)
                    item { ExpressiveSectionHeader("Date & time") }
                    item {
                        SegmentedNavigateRow(
                            index = 0,
                            count = 1,
                            icon = Icons.Rounded.CalendarToday,
                            title = "Change date & time",
                            subtitle = "Opens system date & time settings",
                            trailingIcon = Icons.Rounded.OpenInNew,
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (pickingHome) {
        HomeTimeZonePickerDialog(
            currentZoneId = settings.world.homeZoneOverride ?: ZoneId.systemDefault().id,
            onSelect = { selectedZoneId ->
                onChange { it.copy(world = it.world.copy(homeZoneOverride = selectedZoneId)) }
                pickingHome = false
            },
            onDismiss = { pickingHome = false }
        )
    }
}

@Composable
private fun HomeTimeZonePickerDialog(
    currentZoneId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val allZones = remember {
        ZoneId.getAvailableZoneIds()
            .filter { '/' in it && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sorted()
    }
    val filtered = remember(query, allZones) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) allZones
        else allZones.filter { it.replace('_', ' ').lowercase().contains(q) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Home time zone",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    shape = CircleShape,
                    placeholder = { Text("Search city or region") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filtered, key = { it }) { id ->
                        val selected = id == currentZoneId
                        val display = id.substringAfterLast('/').replace('_', ' ')
                        val region = id.substringBefore('/').replace('_', ' ')
                        Surface(
                            onClick = { onSelect(id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = display,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = region,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

/* ── 4. Timer Settings Sheet ──────────────────────────────────────────────────────── */

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

    val soundLabel = remember(settings.timers.soundUri) {
        when (val u = settings.timers.soundUri) {
            null -> "Default alarm sound"
            SILENT -> "Silent"
            else -> runCatching {
                RingtoneManager.getRingtone(context, Uri.parse(u))?.getTitle(context)
            }.getOrNull() ?: "Default alarm sound"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ExpressiveSheetHeader("Timer settings")
            }

            item { ExpressiveSectionHeader("Sound & Alerts") }
            item {
                SegmentedNavigateRow(
                    index = 0,
                    count = 3,
                    icon = Icons.Rounded.MusicNote,
                    title = "Timer sound",
                    subtitle = soundLabel,
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
            }
            item {
                SegmentedSwitchRow(
                    index = 1,
                    count = 3,
                    icon = Icons.Rounded.Vibration,
                    title = "Timer vibrate",
                    checked = settings.timers.vibrate,
                    onChange = { v: Boolean ->
                        onChange { it.copy(timers = it.timers.copy(vibrate = v)) }
                    },
                )
            }
            item {
                SegmentedSwitchRow(
                    index = 2,
                    count = 3,
                    icon = Icons.Rounded.TrendingUp,
                    title = "Gradually increase volume",
                    subtitle = "Starts quiet and builds up over first 20s",
                    checked = settings.timers.gradualVolume,
                    onChange = { v: Boolean ->
                        onChange { it.copy(timers = it.timers.copy(gradualVolume = v)) }
                    },
                )
            }
        }
    }
}

/* ── 5. Screensaver Settings (Shared Items + Standalone Sheet for System Dream Entry) ── */

private fun LazyListScope.screensaverSettingsItems(
    settings: ScreensaverSettings,
    onChange: ((ScreensaverSettings) -> ScreensaverSettings) -> Unit,
) {
    // Highlighted Top Promo/Action Card
    item {
        val context = LocalContext.current
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "System Screensaver Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "To use bedside clock while charging, select Material Clock in Android System Screensaver settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Open System Screensaver Settings",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Group 1: Display Elements (3 items)
    item { ExpressiveSectionHeader("Display Elements") }
    item {
        SegmentedSwitchRow(
            index = 0,
            count = 3,
            icon = Icons.Rounded.CalendarToday,
            title = "Show Date",
            subtitle = "Display current date below clock",
            checked = settings.showDate,
            onChange = { v -> onChange { it.copy(showDate = v) } }
        )
    }
    item {
        SegmentedSwitchRow(
            index = 1,
            count = 3,
            icon = Icons.Rounded.Timer,
            title = "Show Seconds",
            subtitle = "Update time every second",
            checked = settings.showSeconds,
            onChange = { v -> onChange { it.copy(showSeconds = v) } }
        )
    }
    item {
        SegmentedSwitchRow(
            index = 2,
            count = 3,
            icon = Icons.Rounded.BatteryChargingFull,
            title = "Show Battery",
            subtitle = "Display battery percentage while charging",
            checked = settings.showBattery,
            onChange = { v -> onChange { it.copy(showBattery = v) } }
        )
    }

    // Group 2: Appearance & Night Mode (2 items)
    item { ExpressiveSectionHeader("Appearance & Night Mode") }
    item {
        SegmentedSwitchRow(
            index = 0,
            count = 2,
            icon = Icons.Rounded.Contrast,
            title = "OLED Pure Black Background",
            subtitle = "True black (#000000) surfaces for OLED screen protection",
            checked = settings.backgroundType == ScreensaverBackgroundType.OLED_BLACK,
            onChange = { isOled ->
                val type = if (isOled) {
                    ScreensaverBackgroundType.OLED_BLACK
                } else {
                    ScreensaverBackgroundType.LCD_DARK_GRAY
                }
                onChange { it.copy(backgroundType = type) }
            }
        )
    }
    item {
        SegmentedSurface(
            index = 1,
            count = 2,
            onClick = { onChange { it.copy(nightModeEnabled = !settings.nightModeEnabled) } }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessLow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Night Mode (Dim Clock)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Reduce clock brightness for bedside use",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.nightModeEnabled,
                        onCheckedChange = { v -> onChange { it.copy(nightModeEnabled = v) } }
                    )
                }

                AnimatedVisibility(
                    visible = settings.nightModeEnabled,
                    enter = expandVertically(animationSpec = fluidSpring()) + fadeIn(tween(250)),
                    exit = shrinkVertically(animationSpec = fluidSpring()) + fadeOut(tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 60.dp, end = 20.dp, bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Clock Dimming Level",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(settings.clockDimmingFactor * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
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

    // Group 3: Protection (1 standalone item)
    item { ExpressiveSectionHeader("Protection") }
    item {
        SegmentedSwitchRow(
            index = 0,
            count = 1,
            icon = Icons.Rounded.Shield,
            title = "Smart Adaptive Pixel Shift",
            subtitle = "Shift clock position every few minutes to reduce burn-in risk",
            checked = settings.pixelShiftEnabled,
            onChange = { v -> onChange { it.copy(pixelShiftEnabled = v) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreensaverSettingsSheet(
    settings: ScreensaverSettings,
    onChange: ((ScreensaverSettings) -> ScreensaverSettings) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ExpressiveSheetHeader("Screensaver settings")
            }
            screensaverSettingsItems(
                settings = settings,
                onChange = onChange
            )
        }
    }
}

private fun minutesLabel(minutes: Int): String = when (minutes) {
    0 -> "Never"
    1 -> "1 minute"
    else -> "$minutes minutes"
}
