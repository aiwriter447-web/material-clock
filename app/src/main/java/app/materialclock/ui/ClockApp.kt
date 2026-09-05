package app.materialclock.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.materialclock.alarm.Notifications
import app.materialclock.core.Alarm
import app.materialclock.core.TimerPreset
import app.materialclock.ui.screens.AlarmsScreen
import app.materialclock.ui.screens.StopwatchScreen
import app.materialclock.ui.screens.TimersScreen
import app.materialclock.ui.screens.WorldClockScreen
import app.materialclock.ui.sheets.AddCitySheet
import app.materialclock.ui.sheets.AlarmEditSheet
import app.materialclock.ui.sheets.AlarmSettingsSheet
import app.materialclock.ui.sheets.PresetEditSheet
import app.materialclock.ui.sheets.StopwatchSettingsSheet
import app.materialclock.ui.sheets.TimerSettingsSheet
import app.materialclock.ui.sheets.WorldSettingsSheet
import app.materialclock.ui.theme.ClockTheme

/** Breathing room between the dock and the navigation bar. */
private val DOCK_CLEARANCE = 20.dp

/**
 * How much of the bottom of every screen the floating dock is standing on.
 *
 * This used to be a hand-derived constant (`DOCK_HEIGHT + DOCK_CLEARANCE + 12.dp`) reserved as
 * list padding, and it drifted out of sync with the dock's real size at least once already: the
 * pill grew from 64 dp to 72 dp and the constant stayed put, so the last row of every list sat 8
 * dp under the pill with nothing to notice and nothing to fail. It is now measured off the dock's
 * actual layout instead — see `dockHeight` in [ClockApp] — so there is no second number to forget
 * to update. [DOCK_HEIGHT] itself survives only as the first-frame fallback, before that
 * measurement exists.
 */

enum class Tab(val label: String, val icon: ImageVector, val key: String) {
    ALARMS("Alarms", Icons.Outlined.Alarm, Notifications.TAB_ALARMS),
    WORLD("World Clock", Icons.Outlined.Public, "world"),
    TIMERS("Timers", Icons.Outlined.HourglassEmpty, Notifications.TAB_TIMERS),
    STOPWATCH("Stopwatch", Icons.Outlined.Timer, Notifications.TAB_STOPWATCH),
}

/**
 * The app shell.
 *
 * Navigation is [ClockDock], a floating pill with a detached add button and no navigation bar, so
 * the alarm grid and the world-clock list run full-bleed to the bottom edge. That component owes
 * the accessibility contract a `ShortNavigationBar` would have supplied for free; see its own
 * documentation for what it pays and why.
 *
 * ## Where the settings are
 *
 * In the title. Tapping "Alarms" opens the alarm settings, tapping "World Clock" opens that tab's,
 * and so on; Stopwatch has none and is inert. There is no gear and no ripple. See
 * [app.materialclock.ui.sheets.AlarmSettingsSheet] for why an unmarked affordance is the right
 * trade for preferences you set twice a year.
 */
@Composable
fun ClockApp(startTab: String? = null, vm: ClockViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    ClockTheme(settings.theme) {
        var tab by rememberSaveable { mutableStateOf(startTab?.let { k -> Tab.entries.firstOrNull { it.key == k } } ?: Tab.ALARMS) }
        var editing by remember { mutableStateOf<Alarm?>(null) }
        var editingPreset by remember { mutableStateOf<TimerPreset?>(null) }
        var showSettings by remember { mutableStateOf(false) }
        var addingCity by remember { mutableStateOf(false) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Reachability curtain: long-press the dock to pull the whole screen down into thumb
        // range, the same gesture Samsung's One UI and iOS use. Gated by its own setting rather
        // than always-on, since a screen that suddenly shrinks under a long-press is a surprise
        // to anyone who didn't turn it on.
        var curtainDown by remember { mutableStateOf(false) }
        LaunchedEffect(settings.theme.oneHandMode) {
            if (!settings.theme.oneHandMode) curtainDown = false
        }
        val curtainScale by animateFloatAsState(
            targetValue = if (curtainDown) 0.7f else 1f,
            label = "curtainScale",
        )

        // The dock's real footprint, measured off the live layout rather than a hand-maintained
        // constant. That constant already drifted out of sync once before (see the DOCK_RESERVE
        // history) when the pill grew and nobody updated the number reserving space for it — a
        // measured height can't drift, because it *is* whatever the dock is actually drawing this
        // frame, whatever changes it, RTL, larger accessibility text, a future redesign.
        // DOCK_RESERVE is only the first-frame fallback, before layout has happened once.
        val density = LocalDensity.current
        var dockHeight by remember { mutableStateOf(DOCK_HEIGHT) }

        // Asked once, on first composition. Denying it costs the notifications and nothing else.
        // The alarm still rings, because the ringer is a foreground service and audio, not a post.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(tab.label, style = MaterialTheme.typography.headlineSmall)
                    },
                    actions = {
                        // Used to be title-tap-only, then title-tap-plus-icon for anyone who'd
                        // already learned the old gesture. The icon alone tested fine on its own,
                        // so the invisible fallback is gone — one clear entry point instead of two.
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "${tab.label} settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
        ) { padding ->
            // Keep the bars' insets but let each screen own its horizontal margin, so a grid
            // and a full-bleed dial can differ without fighting the Scaffold.
            //
            // The dock floats over the content rather than displacing it, so the space it occupies
            // has to be reserved here by hand; otherwise the last alarm tile sits under the pill.
            val ld = LocalLayoutDirection.current
            val underDock = padding.calculateBottomPadding() + dockHeight + DOCK_CLEARANCE + 12.dp
            // The add button floats bottom-right, just above the dock, rather than top-right: top
            // put it further from the thumb than the dock itself, which defeats the point of a
            // one-hand-reachable layout. Stacking it above the dock instead means the two tabs
            // that have it need extra bottom clearance, not top.
            val underDockAndFab = underDock + DOCK_HEIGHT + 12.dp
            val body = PaddingValues(
                start = padding.calculateStartPadding(ld) + 16.dp,
                end = padding.calculateEndPadding(ld) + 16.dp,
                top = padding.calculateTopPadding(),
                bottom = underDockAndFab, // Alarms always shows the add button
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
                // `clip = false`: the four tabs are wildly different heights (a 2×2 alarm grid vs
                // a full-bleed world-clock list vs a centred timer dial), and the default
                // SizeTransform clips content to whichever is smaller mid-transition — the same
                // pop/blink fixed in TimersScreen's running/idle crossfade, for the same reason.
                transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
                label = "tab",
                // Anchored at the bottom centre: the dial, the keypad, the day toggles are already
                // near the thumb, so shrinking toward them (rather than toward the screen's centre)
                // is what actually pulls the top of the screen down instead of just shrinking it
                // in place.
                modifier = Modifier.graphicsLayer {
                    scaleX = curtainScale
                    scaleY = curtainScale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            ) { current ->
                when (current) {
                    Tab.ALARMS -> {
                        val alarms by vm.alarms.collectAsStateWithLifecycle()
                        AlarmsScreen(
                            alarms = alarms,
                            weekStart = settings.alarms.weekStart,
                            onToggle = vm::toggleAlarm,
                            onEdit = { editing = it },
                            contentPadding = body,
                        )
                    }

                    Tab.WORLD -> {
                        val cities by vm.cities.collectAsStateWithLifecycle()
                        val now by rememberWallTicker()
                        WorldClockScreen(
                            cities = cities,
                            home = vm.homeZone,
                            nowUtcMillis = now,
                            settings = settings.world,
                            // Swiping a city away is instant and undoable, rather than instant and
                            // final or safe and nagging. The snackbar is the spec's own mitigation
                            // for a destructive swipe, and it costs one tap instead of one per
                            // deletion the way a confirmation dialog did.
                            onRemove = { city ->
                                vm.removeCity(city.zone)
                                scope.launch {
                                    val r = snackbar.showSnackbar(
                                        message = "Removed ${city.city}",
                                        actionLabel = "Undo",
                                        // Short, explicitly. With an action label the default is
                                        // Indefinite, which leaves a bar you have to dismiss by
                                        // hand sitting over the list after every swipe, and the
                                        // separate dismiss affordance it needs then crowds the
                                        // action itself. An undo that expires is the normal one.
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (r == SnackbarResult.ActionPerformed) vm.addCity(city)
                                }
                            },
                            contentPadding = edgeToEdgeWithFab,
                        )
                    }

                    Tab.TIMERS -> {
                        val timer by vm.timer.collectAsStateWithLifecycle()
                        val presets by vm.presets.collectAsStateWithLifecycle()
                        // Only tick while a timer exists; the setting screen has nothing moving.
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

            ClockDock(
                destinations = Tab.entries,
                selected = tab,
                onSelect = { tab = it },
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
                    },
            )

            // Floating bottom-right, just above the dock — see FloatingAddButton's doc for why
            // that beats both the old inline-with-the-dock spot and top-right.
            FloatingAddButton(
                visible = tab == Tab.ALARMS || tab == Tab.WORLD,
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
            AlarmEditSheet(
                initial = draft,
                weekStart = settings.alarms.weekStart,
                onDismiss = { editing = null },
                onSave = { vm.saveAlarm(it) },
                onDelete = if (draft.id == 0L) null else ({ id -> vm.deleteAlarm(id) }),
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
                Tab.ALARMS -> AlarmSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.WORLD -> WorldSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.TIMERS -> TimerSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.STOPWATCH ->
                    StopwatchSettingsSheet(settings, vm::updateSettings) { showSettings = false }
            }
        }
    }
}
