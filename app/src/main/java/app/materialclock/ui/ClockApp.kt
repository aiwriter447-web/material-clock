package app.materialclock.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.materialclock.alarm.AlarmScheduler
import app.materialclock.alarm.Notifications
import app.materialclock.core.Alarm
import app.materialclock.core.TimerPreset
import app.materialclock.data.ScreensaverRepository
import app.materialclock.data.ScreensaverSettings
import app.materialclock.ui.screens.AlarmsScreen
import app.materialclock.ui.screens.StopwatchScreen
import app.materialclock.ui.screens.TimersScreen
import app.materialclock.ui.screens.WorldClockScreen
import app.materialclock.ui.sheets.AddCitySheet
import app.materialclock.ui.sheets.AlarmEditSheet
import app.materialclock.ui.sheets.AlarmGroupsSheet
import app.materialclock.ui.sheets.AlarmSettingsSheet
import app.materialclock.ui.sheets.PresetEditSheet
import app.materialclock.ui.sheets.ScreensaverSettingsSheet
import app.materialclock.ui.sheets.StopwatchSettingsSheet
import app.materialclock.ui.sheets.TimerSettingsSheet
import app.materialclock.ui.sheets.WorldSettingsSheet
import app.materialclock.ui.theme.ClockTheme
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val DOCK_CLEARANCE = 20.dp
private const val OPEN_SETTLE_DELAY_MS = 150L

private fun <T> fastSnappySpringSpec() = spring<T>(
    stiffness = Spring.StiffnessMedium,
    dampingRatio = Spring.DampingRatioNoBouncy
)

enum class Tab(val label: String, val icon: ImageVector, val key: String) {
    ALARMS("Alarms", Icons.Outlined.Alarm, Notifications.TAB_ALARMS),
    WORLD("World Clock", Icons.Outlined.Public, "world"),
    TIMERS("Timers", Icons.Outlined.HourglassEmpty, Notifications.TAB_TIMERS),
    STOPWATCH("Stopwatch", Icons.Outlined.Timer, Notifications.TAB_STOPWATCH),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockApp(
    startTab: String? = null,
    startWithScreensaver: Boolean = false,
    onOpenWorldSettings: (() -> Unit)? = null,
    onOpenScreensaverSettings: (() -> Unit)? = null,
    vm: ClockViewModel = viewModel()
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val isLoaded by vm.isLoaded.collectAsStateWithLifecycle()

    val ctx = LocalContext.current
    val screensaverRepository = remember { ScreensaverRepository(ctx) }
    val screensaverSettings by screensaverRepository.settingsFlow.collectAsState(
        initial = ScreensaverSettings()
    )

    val timer by vm.timer.collectAsStateWithLifecycle()

    ClockTheme(settings.theme) {
        if (!isLoaded) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {}
            return@ClockTheme
        }

        val nm = remember {
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        var hasFullScreenPermission by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    nm.canUseFullScreenIntent()
                } else true
            )
        }

        var notificationsOn by remember { mutableStateOf(nm.areNotificationsEnabled()) }

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    notificationsOn = nm.areNotificationsEnabled()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        hasFullScreenPermission = nm.canUseFullScreenIntent()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // First-run setup: three things the app cannot do its job without, asked one at a time
        // before it opens (see SetupScreens.kt). An existing install that already has an alarm sound
        // picked is not asked again.
        val soundChosen = settings.alarms.soundChosen || settings.alarms.defaultSoundUri != null
        val setupStep = when {
            !hasFullScreenPermission -> SetupStep.FULL_SCREEN
            !notificationsOn -> SetupStep.NOTIFICATIONS
            !soundChosen -> SetupStep.SOUND
            else -> null
        }
        if (setupStep != null) {
            SetupScreen(
                step = setupStep,
                onSoundChosen = { uri ->
                    vm.updateSettings { it.copy(alarms = it.alarms.copy(defaultSoundUri = uri, soundChosen = true)) }
                },
            )
            return@ClockTheme
        }

        val initialPageIndex = rememberSaveable {
            startTab?.let { k ->
                Tab.entries.indexOfFirst {
                    it.key == k
                }.takeIf { it >= 0 }
            } ?: 0
        }

        val pagerState = rememberPagerState(
            initialPage = initialPageIndex,
            pageCount = { Tab.entries.size }
        )

        val currentTab = Tab.entries[pagerState.currentPage]
        val targetTab = Tab.entries[pagerState.targetPage]

        var editing by remember { mutableStateOf<Alarm?>(null) }
        var editingPreset by remember { mutableStateOf<TimerPreset?>(null) }
        var showSettings by remember { mutableStateOf(false) }
        
        var showScreensaverSettingsSheet by remember(startWithScreensaver) { mutableStateOf(startWithScreensaver) }
        // Memory flag for ideal navigation back-stack (Screensaver -> WorldClock)
        var returnToWorldClock by remember { mutableStateOf(false) }

        var showGroupsManager by remember { mutableStateOf(false) }
        var addingCity by remember { mutableStateOf(false) }
        var selectionActive by remember { mutableStateOf(false) }

        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(currentTab) {
            if (currentTab == Tab.ALARMS && !AlarmScheduler.canScheduleExact(ctx)) {
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

        val density = LocalDensity.current
        var dockHeight by remember { mutableStateOf(DOCK_HEIGHT) }

        // ---- One-hand mode: swipe down to bring the top of the screen into thumb reach ----
        //
        // `reachFraction` is how far the top has been pulled down, from 0 (normal) to 1 (all the
        // way). It is a plain state that only layout and drawing read, never composition, so
        // dragging it moves things without rebuilding the whole screen on every frame.
        val oneHandOn = settings.theme.oneHandMode
        // How far the top comes down on the Alarms screen: about 15% of the screen's height, from the
        // reference screenshots.
        val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
        val reachMaxPx = screenHeightPx * 0.15f

        // One-hand mode belongs to the Alarms screen only. The World Clock, Timers and Stopwatch
        // screens do not use it.
        val reachActive = oneHandOn && !selectionActive && targetTab == Tab.ALARMS

        var reachFraction by remember { mutableFloatStateOf(0f) }
        var reachResting by remember { mutableFloatStateOf(0f) }
        var reachJob by remember { mutableStateOf<Job?>(null) }

        fun reachMoveBy(dy: Float) {
            reachJob?.cancel()
            reachFraction = (reachFraction + dy / reachMaxPx).coerceIn(0f, 1f)
        }

        fun reachSettle() {
            // Let go past a quarter of the way down and it opens fully; past a quarter of the way
            // back up and it closes. The gesture starts easily and finishes by itself.
            val target = if (reachResting < 0.5f) {
                if (reachFraction > 0.30f) 1f else 0f
            } else {
                if (reachFraction < 0.70f) 0f else 1f
            }
            reachResting = target
            reachJob?.cancel()
            reachJob = scope.launch {
                animate(
                    initialValue = reachFraction,
                    targetValue = target,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
                ) { v, _ -> reachFraction = v }
            }
        }

        // Closes itself when it stops applying: the setting is turned off, a selection starts, or
        // the World Clock tab (which has its own large clock) is on its way in.
        LaunchedEffect(reachActive) {
            if (!reachActive && reachFraction > 0f) {
                reachResting = 0f
                reachJob?.cancel()
                animate(reachFraction, 0f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)) { v, _ ->
                    reachFraction = v
                }
            }
        }

        // Every tab starts closed. The amount differs from tab to tab, so carrying a half-open top
        // from one to another would leave it at the wrong height.
        LaunchedEffect(targetTab) {
            if (reachFraction > 0f) {
                reachResting = 0f
                reachJob?.cancel()
                animate(reachFraction, 0f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)) { v, _ ->
                    reachFraction = v
                }
            }
        }

        // For the screens that scroll: a drag down that the list cannot use (it is already at the
        // top) opens the gap, and a drag up closes the gap before the list is allowed to scroll.
        val reachConnection = remember(reachActive, reachMaxPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (!reachActive || source != NestedScrollSource.UserInput) return Offset.Zero
                    if (available.y < 0f && reachFraction > 0f) {
                        val before = reachFraction
                        reachMoveBy(available.y)
                        return Offset(0f, (reachFraction - before) * reachMaxPx)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (!reachActive || source != NestedScrollSource.UserInput) return Offset.Zero
                    if (available.y > 0f && reachFraction < 1f) {
                        val before = reachFraction
                        reachMoveBy(available.y)
                        return Offset(0f, (reachFraction - before) * reachMaxPx)
                    }
                    return Offset.Zero
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    reachSettle()
                    return Velocity.Zero
                }
            }
        }

        val alarmsNow by vm.alarms.collectAsStateWithLifecycle()
        val stopwatchNow by vm.stopwatch.collectAsStateWithLifecycle()
        val is24Hour = android.text.format.DateFormat.is24HourFormat(ctx)
        val minuteTick by produceState(0) {
            while (true) {
                delay(30_000L)
                value += 1
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                Box(Modifier.graphicsLayer { translationY = reachFraction * reachMaxPx }) {
                TopAppBar(
                    title = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            AnimatedContent(
                                targetState = targetTab,
                                transitionSpec = {
                                    if (targetState.ordinal > initialState.ordinal) {
                                        (slideInHorizontally(animationSpec = fastSnappySpringSpec()) { width -> width / 3 } + fadeIn(tween(100))) togetherWith
                                            (slideOutHorizontally(animationSpec = fastSnappySpringSpec()) { width -> -width / 3 } + fadeOut(tween(80)))
                                    } else {
                                        (slideInHorizontally(animationSpec = fastSnappySpringSpec()) { width -> -width / 3 } + fadeIn(tween(100))) togetherWith
                                            (slideOutHorizontally(animationSpec = fastSnappySpringSpec()) { width -> -width / 3 } + fadeOut(tween(80)))
                                    }
                                },
                                label = "TopBarTitleAnim"
                            ) { tab ->
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "${currentTab.label} settings",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                }
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

            Box(
                Modifier
                    .fillMaxSize()
                    .nestedScroll(reachConnection)
                    // For screens with nothing to scroll (a short list, the timer keypad): a plain
                    // vertical drag that nothing else used.
                    .pointerInput(reachActive) {
                        if (!reachActive) return@pointerInput
                        detectVerticalDragGestures(
                            onDragEnd = { reachSettle() },
                            onDragCancel = { reachSettle() },
                        ) { _, dy -> reachMoveBy(dy) }
                    },
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clipScrollableContainer(Orientation.Horizontal),
                    beyondViewportPageCount = 1
                ) { page ->

                    val isFocused = pagerState.currentPage == page || pagerState.targetPage == page

                    Box(
                        Modifier
                            .fillMaxSize()
                            // Only drawing moves, never layout: the page is slid down as a picture, so the
                            // pull-down costs nothing per frame however long the list is.
                            .then(
                                if (Tab.entries[page] == Tab.ALARMS) {
                                    Modifier.graphicsLayer { translationY = reachFraction * reachMaxPx }
                                } else {
                                    Modifier
                                },
                            )
                            .graphicsLayer {
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                alpha = 1f - (kotlin.math.abs(pageOffset) * 0.15f).coerceIn(0f, 1f)
                            }
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        when (Tab.entries[page]) {
                            Tab.ALARMS -> {
                                val alarms by vm.alarms.collectAsStateWithLifecycle()
                                val groups by vm.groups.collectAsStateWithLifecycle()
                                AlarmsScreen(
                                    alarms = alarms,
                                    groups = groups,
                                    weekStart = settings.alarms.weekStart,
                                    onSelectionChange = { selectionActive = it },
                                    onToggle = vm::toggleAlarm,
                                    onToggleGroup = vm::toggleGroup,
                                    onTogglePin = vm::togglePinAlarm,
                                    onEdit = { editing = it },
                                    onDelete = { alarm -> vm.deleteAlarm(alarm.id) },
                                    onDeleteSelected = vm::deleteSelectedAlarms,
                                    onSetEnabledSelected = vm::setAlarmsEnabledState,
                                    onUngroupSelected = vm::ungroupSelectedAlarms,
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
                                    onSelectionChange = { selectionActive = it },
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
                                    onDeleteSelected = vm::deleteSelectedCities,
                                    contentPadding = edgeToEdgeWithFab,
                                )
                            }
                            Tab.TIMERS -> {
                                val presets by vm.presets.collectAsStateWithLifecycle()
                                val now by rememberElapsedTicker(active = isFocused && timer != null)
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
                                    onAddPreset = {},
                                    contentPadding = edgeToEdge,
                                )
                            }
                            Tab.STOPWATCH -> {
                                val sw by vm.stopwatch.collectAsStateWithLifecycle()
                                val now by rememberElapsedTicker(active = isFocused && sw.running)
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
                }

                // Only the Alarms tab has room for the summary: the others move too little.
                if (oneHandOn && targetTab == Tab.ALARMS) {
                    ReachSummary(
                        summary = reachSummaryText(targetTab, alarmsNow, timer, stopwatchNow, is24Hour, minuteTick),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 40.dp)
                            .graphicsLayer { alpha = ((reachFraction - 0.15f) / 0.85f).coerceIn(0f, 1f) },
                    )
                }

                AnimatedVisibility(
                    visible = !selectionActive,
                    enter = fadeIn(animationSpec = tween(100)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = fastSnappySpringSpec()
                        ),
                    exit = fadeOut(animationSpec = tween(80)) +
                        slideOutVertically(
                            targetOffsetY = { it / 4 },
                            animationSpec = fastSnappySpringSpec()
                        ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = DOCK_CLEARANCE)
                ) {
                    ClockDock(
                        destinations = Tab.entries,
                        selected = targetTab,
                        onSelect = { selectedTab ->
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = Tab.entries.indexOf(selectedTab),
                                    animationSpec = fastSnappySpringSpec()
                                )
                            }
                        },
                        modifier = Modifier.onGloballyPositioned { coords ->
                            with(density) { dockHeight = coords.size.height.toDp() }
                        },
                    )
                }

                FloatingAddButton(
                    visible = !selectionActive &&
                        (targetTab == Tab.ALARMS || targetTab == Tab.WORLD),
                    label = when (targetTab) {
                        Tab.WORLD -> "Add city"
                        else -> "Add alarm"
                    },
                    onClick = {
                        when (targetTab) {
                            Tab.WORLD -> addingCity = true
                            else -> editing = vm.blankAlarm()
                        }
                    },
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
                onDelete = if (draft.id == 0L) null else { { id -> vm.deleteAlarm(id) } },
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
                onDelete = if (draft.id == 0L) null else { { id -> vm.deletePreset(id) } },
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
            when (currentTab) {
                Tab.ALARMS -> AlarmSettingsSheet(
                    settings = settings,
                    onChange = vm::updateSettings,
                    onManageGroups = { showGroupsManager = true },
                    onDismiss = { showSettings = false },
                )
                Tab.WORLD -> WorldSettingsSheet(
                    settings = settings,
                    onChange = vm::updateSettings,
                    onOpenScreensaverSettings = {
                        showSettings = false
                        returnToWorldClock = true // Sets the flag to remember where we came from
                        showScreensaverSettingsSheet = true
                    },
                    onDismiss = { showSettings = false },
                )
                Tab.TIMERS -> TimerSettingsSheet(settings, vm::updateSettings) { showSettings = false }
                Tab.STOPWATCH -> StopwatchSettingsSheet(settings, vm::updateSettings) { showSettings = false }
            }
        }

        if (showScreensaverSettingsSheet) {
            ScreensaverSettingsSheet(
                settings = screensaverSettings,
                onChange = { transform ->
                    scope.launch { screensaverRepository.updateSettings(transform) }
                },
                onDismiss = {
                    showScreensaverSettingsSheet = false
                    if (returnToWorldClock) {
                        showSettings = true // Returns directly to WorldSettingsSheet
                        returnToWorldClock = false // Resets the flag
                    }
                }
            )
        }
    }
}
