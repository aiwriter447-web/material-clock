package app.materialclock.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.GroupRemove
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.rounded.Settings 
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.materialclock.alarm.AlarmScheduler
import app.materialclock.alarm.Notifications
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.core.TimerPreset
import app.materialclock.ui.screens.AlarmsScreen
import app.materialclock.ui.screens.StopwatchScreen
import app.materialclock.ui.screens.TimersScreen
import app.materialclock.ui.screens.WorldClockScreen
import app.materialclock.ui.sheets.AddCitySheet
import app.materialclock.ui.sheets.AlarmEditSheet
import app.materialclock.ui.sheets.AlarmGroupsSheet
import app.materialclock.ui.sheets.AlarmSettingsSheet
import app.materialclock.ui.sheets.PresetEditSheet
import app.materialclock.ui.sheets.StopwatchSettingsSheet
import app.materialclock.ui.sheets.TimerSettingsSheet
import app.materialclock.ui.sheets.WorldSettingsSheet
import app.materialclock.ui.theme.ClockTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DOCK_CLEARANCE = 20.dp
private const val OPEN_SETTLE_DELAY_MS = 400L

enum class Tab(val label: String, val icon: ImageVector, val key: String) {
    ALARMS("Alarms", Icons.Outlined.Alarm, Notifications.TAB_ALARMS),
    WORLD("World Clock", Icons.Outlined.Public, "world"),
    TIMERS("Timers", Icons.Outlined.HourglassEmpty, Notifications.TAB_TIMERS),
    STOPWATCH("Stopwatch", Icons.Outlined.Timer, Notifications.TAB_STOPWATCH),
}

@Composable
fun ClockApp(startTab: String? = null, vm: ClockViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val isLoaded by vm.isLoaded.collectAsStateWithLifecycle()
    
    val selectedAlarms by vm.selectedAlarms.collectAsStateWithLifecycle()
    val selectedGroups by vm.selectedGroups.collectAsStateWithLifecycle()
    val selectedCities by vm.selectedCities.collectAsStateWithLifecycle()

    ClockTheme(settings.theme) {
        if (!isLoaded) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {}
            return@ClockTheme
        }

        var tab by rememberSaveable { mutableStateOf(startTab?.let { k -> Tab.entries.firstOrNull { it.key == k } } ?: Tab.ALARMS) }
        
        val inSelectionMode = when (tab) {
            Tab.ALARMS -> selectedAlarms.isNotEmpty() || selectedGroups.isNotEmpty()
            Tab.WORLD -> selectedCities.isNotEmpty()
            else -> false
        }
        
        val allSelected = when (tab) {
            Tab.ALARMS -> {
                val alarms by vm.alarms.collectAsStateWithLifecycle()
                val groups by vm.groups.collectAsStateWithLifecycle()
                selectedAlarms.size == alarms.size && selectedGroups.size == groups.size && alarms.isNotEmpty()
            }
            Tab.WORLD -> {
                val cities by vm.cities.collectAsStateWithLifecycle()
                selectedCities.size == cities.size && cities.isNotEmpty()
            }
            else -> false
        }

        var editing by remember { mutableStateOf<Alarm?>(null) }
        var editingPreset by remember { mutableStateOf<TimerPreset?>(null) }
        var showSettings by remember { mutableStateOf(false) }
        var showGroupsManager by remember { mutableStateOf(false) }
        var addingCity by remember { mutableStateOf(false) }
        var showBatchDeleteConfirm by remember { mutableStateOf(false) }
        
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val ctx = LocalContext.current
        
        BackHandler(enabled = inSelectionMode) {
            vm.clearSelection()
        }

        LaunchedEffect(tab) {
            vm.clearSelection()
            if (tab == Tab.ALARMS && !AlarmScheduler.canScheduleExact(ctx)) {
                delay(OPEN_SETTLE_DELAY_MS)
                val result = snackbar.showSnackbar(
                    message = "Alarms need the \"Alarms & reminders\" permission to fire exactly on time",
                    actionLabel = "Enable",
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    runCatching {
                        ctx.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.parse("package:${ctx.packageName}")),
                        )
                    }
                }
            }
        }

        var curtainDown by remember { mutableStateOf(false) }
        LaunchedEffect(settings.theme.oneHandMode) {
            if (!settings.theme.oneHandMode) curtainDown = false
        }
        val curtainScale by animateFloatAsState(
            targetValue = if (curtainDown) 0.4f else 1f,
            label = "curtainScale",
        )

        val density = LocalDensity.current
        var dockHeight by remember { mutableStateOf(DOCK_HEIGHT) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) {
                delay(OPEN_SETTLE_DELAY_MS)
                ask.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (showBatchDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showBatchDeleteConfirm = false },
                title = { Text("Delete Items") },
                text = { Text("Are you sure you want to delete the selected items?") },
                confirmButton = {
                    TextButton(onClick = {
                        if (tab == Tab.ALARMS) vm.deleteSelectedAlarms()
                        else if (tab == Tab.WORLD) vm.deleteSelectedCities()
                        showBatchDeleteConfirm = false
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showBatchDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                AnimatedVisibility(visible = inSelectionMode && (tab == Tab.ALARMS || tab == Tab.WORLD)) {
                                    Row {
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            imageVector = if (allSelected) Icons.Outlined.Deselect else Icons.Outlined.SelectAll,
                                            contentDescription = "Select All",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { if (allSelected) vm.clearSelection() else if (tab == Tab.ALARMS) vm.selectAllAlarms() else vm.selectAllCities() }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                Icons.Rounded.Settings, 
                                contentDescription = "${tab.label} settings",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
        ) { padding ->
            val ld = LocalLayoutDirection.current
            val underDock = padding.calculateBottomPadding() + dockHeight + DOCK_CLEARANCE + 12.dp
            val underDockAndFab = underDock + DOCK_HEIGHT + 12.dp
            val body = PaddingValues(
                start = padding.calculateStartPadding(ld) + 16.dp,
                end = padding.calculateEndPadding(ld) + 16.dp,
                top = padding.calculateTopPadding(),
                bottom = underDockAndFab,
            )
            val edgeToEdge = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = underDock,
            )
            val edgeToEdgeWithFab = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = underDockAndFab,
            )

          Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
                label = "tab",
                modifier = Modifier.graphicsLayer {
                    scaleX = curtainScale
                    scaleY = curtainScale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            ) { current ->
                when (current) {
                    Tab.ALARMS -> {
                        val alarms by vm.alarms.collectAsStateWithLifecycle()
                        val groups by vm.groups.collectAsStateWithLifecycle()
                        AlarmsScreen(
                            alarms = alarms,
                            groups = groups,
                            weekStart = settings.alarms.weekStart,
                            inSelectionMode = inSelectionMode,
                            selectedAlarms = selectedAlarms,
                            selectedGroups = selectedGroups,
                            onToggle = vm::toggleAlarm,
                            onToggleGroup = vm::toggleGroup,
                            onTogglePin = vm::togglePinAlarm,
                            onToggleSelect = vm::toggleAlarmSelection,
                            onToggleGroupSelect = vm::toggleGroupSelection,
                            onEdit = { editing = it },
                            onDelete = { alarm -> vm.deleteAlarm(alarm.id) },
                            contentPadding = body,
                        )
                    }

                    Tab.WORLD -> {
                        val cities by vm.cities.collectAsStateWithLifecycle()
                        val homeZone by vm.homeZone.collectAsStateWithLifecycle()
                        val now by rememberWallTicker()
                        WorldClockScreen(
                            cities = cities,
                            home = homeZone,
                            nowUtcMillis = now,
                            settings = settings.world,
                            inSelectionMode = inSelectionMode,
                            selectedCities = selectedCities,
                            onRemove = { city ->
                                vm.removeCity(city.zone)
                                scope.launch {
                                    val r = snackbar.showSnackbar(
                                        message = "Removed ${city.city}",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (r == SnackbarResult.ActionPerformed) vm.addCity(city)
                                }
                            },
                            onTogglePin = { city -> vm.togglePinCity(city.zone) },
                            onToggleSelect = { vm.toggleCitySelection(it) },
                            contentPadding = edgeToEdgeWithFab,
                        )
                    }

                    Tab.TIMERS -> {
                        val timer by vm.timer.collectAsStateWithLifecycle()
                        val presets by vm.presets.collectAsStateWithLifecycle()
                        val now by rememberElapsedTicker(active = timer != null)
                        TimersScreen(
                            timer = timer,
                            draft = vm.draftDuration,
                            nowElapsedMillis = now,
                            presets = presets,
                            onDigit = vm::pressDigit,
                            onBackspace = vm::backspace,
                            onWind = vm::windToMinutes,
                            onStart = { vm.startTimer() },
                            onPauseResume = { vm.pauseOrResumeTimer() },
                            onAddTen = { vm.addTenSeconds() },
                            onCancel = { vm.cancelTimer() },
                            onStartPreset = { vm.startPreset(it) },
                            onEditPreset = { editingPreset = it },
                            onAddPreset = { editingPreset = TimerPreset(id = 0L, name = "", totalSeconds = 25 * 60) },
                            contentPadding = edgeToEdge,
                        )
                    }

                    Tab.STOPWATCH -> {
                        val sw by vm.stopwatch.collectAsStateWithLifecycle()
                        val now by rememberElapsedTicker(active = sw.running)
                        StopwatchScreen(
                            stopwatch = sw,
                            nowElapsedMillis = now,
                            onToggle = { vm.toggleStopwatch() },
                            onLap = { vm.lap() },
                            onReset = { vm.resetStopwatch() },
                            contentPadding = edgeToEdge,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = DOCK_CLEARANCE)
                    .onGloballyPositioned { coords ->
                        with(density) { dockHeight = coords.size.height.toDp() }
                    }
                    .let { base ->
                        if (settings.theme.oneHandMode) {
                            base.pointerInput(Unit) {
                                detectTapGestures(onLongPress = { curtainDown = !curtainDown })
                            }
                        } else {
                            base
                        }
                    }
            ) {
                ClockDock(
                    destinations = Tab.entries,
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.alpha(if (inSelectionMode) 0f else 1f)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = inSelectionMode,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.matchParentSize()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tab == Tab.ALARMS) {
                                IconButton(onClick = { vm.setEnabledSelectedAlarms(true) }) { Icon(Icons.Outlined.AlarmOn, null) }
                                IconButton(onClick = { vm.setEnabledSelectedAlarms(false) }) { Icon(Icons.Outlined.AlarmOff, null) }
                                IconButton(onClick = { vm.ungroupSelectedAlarms() }) { Icon(Icons.Outlined.GroupRemove, null) }
                                IconButton(onClick = { showBatchDeleteConfirm = true }) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            } else if (tab == Tab.WORLD) {
                                IconButton(onClick = { vm.pinSelectedCities() }) { Icon(Icons.Filled.PushPin, null) }
                                IconButton(onClick = { vm.unpinSelectedCities() }) { Icon(Icons.Outlined.PushPin, null) }
                                IconButton(onClick = { showBatchDeleteConfirm = true }) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }

            FloatingAddButton(
                visible = !inSelectionMode && (tab == Tab.ALARMS || tab == Tab.WORLD),
                label = if (tab == Tab.WORLD) "Add city" else "Add alarm",
                onClick = { if (tab == Tab.WORLD) addingCity = true else editing = vm.blankAlarm() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = padding.calculateBottomPadding() + dockHeight + DOCK_CLEARANCE + 12.dp,
                    ),
            )
          }
        }

        editing?.let { draft ->
            val groups by vm.groups.collectAsStateWithLifecycle()
            AlarmEditSheet(
                initial = draft,
                weekStart = settings.alarms.weekStart,
                groups = groups,
                onCreateGroup = { name -> vm.addGroup(name) },
                onDismiss = { editing = null },
                onSave = { vm.saveAlarm(it) },
                onDelete = if (draft.id == 0L) null else ({ id -> vm.deleteAlarm(id) }),
            )
        }

        if (showGroupsManager) {
            val groups by vm.groups.collectAsStateWithLifecycle()
            val alarms by vm.alarms.collectAsStateWithLifecycle()
            AlarmGroupsSheet(
                groups = groups,
                alarms = alarms,
                onAdd = { name -> vm.addGroup(name) },
                onRename = { id, name -> vm.renameGroup(id, name) },
                onDelete = { id -> vm.deleteGroup(id) },
                onDismiss = { showGroupsManager = false },
            )
        }

        editingPreset?.let { draft ->
            PresetEditSheet(
                initial = draft,
                onDismiss = { editingPreset = null },
                onSave = { vm.savePreset(it) },
                onDelete = if (draft.id == 0L) null else ({ id -> vm.deletePreset(id) }),
            )
        }

        if (addingCity) {
            val cities by vm.cities.collectAsStateWithLifecycle()
            val now by rememberWallTicker(60_000L)
            AddCitySheet(
                existing = cities,
                nowUtcMillis = now,
                onAdd = vm::addCity,
                onDismiss = { addingCity = false },
            )
        }

        if (showSettings) {
            when (tab) {
                Tab.ALARMS -> AlarmSettingsSheet(
                    settings = settings,
                    onChange = vm::updateSettings,
                    onManageGroups = { showGroupsManager = true },
                    onDismiss = { showSettings = false },
                )
                Tab.WORLD -> WorldSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.TIMERS -> TimerSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.STOPWATCH -> StopwatchSettingsSheet(settings, vm::updateSettings) { showSettings = false }
            }
        }
    }
}
