package app.materialclock.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val DOCK_CLEARANCE = 8.dp
private const val OPEN_SETTLE_DELAY_MS = 150L

private val TICKER_END_INSET = 38.dp
private val TICKER_START_GAP = 6.dp
private val TICKER_EDGE_FADE = 18.dp

// LIVING FLOW: Calm, weighted spring physics for all transitions
private fun <T> fluidSpringSpec() = spring<T>(
    dampingRatio = 0.92f,
    stiffness = 210f
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
    val sw by vm.stopwatch.collectAsStateWithLifecycle()

    ClockTheme(settings.theme) {
        if (!isLoaded) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {}
            return@ClockTheme
        }

        val isLightTheme = MaterialTheme.colorScheme.surface.luminance() > 0.5f

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
                theme = settings.theme,
                onSoundChosen = { uri ->
                    vm.updateSettings {
                        it.copy(alarms = it.alarms.copy(defaultSoundUri = uri, soundChosen = true))
                    }
                },
            )
            return@ClockTheme
        }

        val initialPageIndex = rememberSaveable {
            startTab?.let { k ->
                Tab.entries.indexOfFirst { it.key == k }.takeIf { it >= 0 }
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

        var showScreensaverSettingsSheet by remember(startWithScreensaver) {
            mutableStateOf(startWithScreensaver)
        }

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

        var pillWidthPx by remember { mutableStateOf(0) }
        val pillWidth = with(density) { pillWidthPx.toDp() }

        val alarmsNow by vm.alarms.collectAsStateWithLifecycle()
        val minuteTick by produceState(0) {
            while (true) {
                delay(30_000L)
                value += 1
            }
        }

        val marqueeText = remember(targetTab, alarmsNow, minuteTick) {
            if (targetTab == Tab.ALARMS) {
                val now = java.time.ZonedDateTime.now()
                val next = alarmsNow.filter { it.enabled }.mapNotNull { it.nextFire(now) }.minOrNull()
                if (next == null) {
                    "All alarms are off"
                } else {
                    val d = java.time.Duration.between(now, next)
                    val days = d.toDays()
                    val hours = d.toHours() % 24
                    val minutes = d.toMinutes() % 60

                    val parts = buildList {
                        if (days > 0) add(if (days == 1L) "1 Day" else "$days Days")
                        if (hours > 0) add(if (hours == 1L) "1 Hour" else "$hours Hours")
                        if (minutes > 0) add(if (minutes == 1L) "1 Minute" else "$minutes Minutes")
                    }
                    if (parts.isEmpty()) "Alarm in less than a minute"
                    else "Alarm in ${parts.joinToString(" ")}"
                }
            } else ""
        }

        val topConfig = LocalConfiguration.current
        val topLandscape = topConfig.screenWidthDp > topConfig.screenHeightDp || topConfig.screenWidthDp >= 600

        val hostView = LocalView.current
        DisposableEffect(isLightTheme) {
            var hostActivity: Context? = hostView.context
            while (hostActivity is ContextWrapper && hostActivity !is Activity) {
                hostActivity = hostActivity.baseContext
            }
            val hostWindow = (hostActivity as? Activity)?.window
            val insetsController = hostWindow?.let { WindowCompat.getInsetsController(it, hostView) }
            if (insetsController != null) {
                insetsController.isAppearanceLightStatusBars = isLightTheme
                insetsController.isAppearanceLightNavigationBars = isLightTheme
            }
            onDispose { }
        }

        val hazeState = remember { HazeState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    modifier = if (topLandscape) Modifier.padding(end = DOCK_HEIGHT + 16.dp) else Modifier,
                    expandedHeight = if (topLandscape) 48.dp else TopAppBarDefaults.TopAppBarExpandedHeight,
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnimatedVisibility(
                                visible = targetTab == Tab.ALARMS && marqueeText.isNotEmpty(),
                                enter = slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth / 3 },
                                    animationSpec = fluidSpringSpec()
                                ) + fadeIn(animationSpec = tween(350)),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth / 3 },
                                    animationSpec = fluidSpringSpec()
                                ) + fadeOut(animationSpec = tween(250)),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth()
                                    .padding(
                                        start = pillWidth + TICKER_START_GAP,
                                        end = TICKER_END_INSET
                                    )
                                    .clipToBounds()
                            ) {
                                LiveNewsHeadlineTicker(text = marqueeText)
                            }

                            IconButton(
                                onClick = { showSettings = true },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = "${currentTab.label} settings",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .onSizeChanged { pillWidthPx = it.width }
                                    .titlePillFrostedGlass(hazeState = hazeState)
                            ) {
                                AnimatedContent(
                                    targetState = targetTab,
                                    transitionSpec = {
                                        (slideInVertically(
                                            animationSpec = fluidSpringSpec()
                                        ) { height -> height / 2 } + fadeIn(animationSpec = tween(400))) togetherWith
                                            (slideOutVertically(
                                                animationSpec = fluidSpringSpec()
                                            ) { height -> -height / 2 } + fadeOut(animationSpec = tween(300)))
                                    },
                                    label = "TopBarTitleAnim"
                                ) { tab ->
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = 20.dp,
                                            vertical = if (topLandscape) 6.dp else 10.dp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
        ) { padding ->

            val ld = LocalLayoutDirection.current
            val config = LocalConfiguration.current
            val landscape = config.screenWidthDp > config.screenHeightDp || config.screenWidthDp >= 600
            val railWidth = DOCK_HEIGHT + 16.dp

            val topClipOffset = (padding.calculateTopPadding() - 6.dp).coerceAtLeast(0.dp)
            val innerTopPadding = 6.dp

            val underDock = if (landscape) {
                padding.calculateBottomPadding() + 12.dp
            } else {
                padding.calculateBottomPadding() + dockHeight + DOCK_CLEARANCE + 14.dp
            }
            val underDockAndFab = if (landscape) {
                padding.calculateBottomPadding() + 92.dp
            } else {
                underDock
            }

            val body = PaddingValues(
                start = padding.calculateStartPadding(ld) + 16.dp,
                end = (if (landscape) 0.dp else padding.calculateEndPadding(ld)) + 16.dp,
                top = innerTopPadding,
                bottom = underDockAndFab,
            )
            val edgeToEdge = PaddingValues(
                top = innerTopPadding,
                bottom = underDock,
            )
            val edgeToEdgeWithFab = PaddingValues(
                top = innerTopPadding,
                bottom = underDockAndFab,
            )

            var initialLaunch by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                initialLaunch = true
            }

            val fabVisible = !selectionActive && (targetTab == Tab.ALARMS || targetTab == Tab.WORLD)
            val fabLabel = when (targetTab) {
                Tab.WORLD -> "Add city"
                Tab.TIMERS -> "Add preset"
                else -> "Add alarm"
            }
            val onFabClick: () -> Unit = {
                when (targetTab) {
                    Tab.WORLD -> addingCity = true
                    Tab.TIMERS -> editingPreset = TimerPreset(
                        id = 0L,
                        name = "",
                        totalSeconds = 25 * 60
                    )
                    else -> editing = vm.blankAlarm()
                }
            }

            AnimatedVisibility(
                visible = initialLaunch,
                enter = slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = fluidSpringSpec()
                ) + fadeIn(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = topClipOffset,
                                end = if (landscape) railWidth + padding.calculateEndPadding(ld) else 0.dp
                            )
                            .clipToBounds()
                            .hazeSource(state = hazeState)
                            .clipScrollableContainer(Orientation.Horizontal),
                        beyondViewportPageCount = 1
                    ) { page ->

                        val isFocused =
                            pagerState.currentPage == page || pagerState.targetPage == page

                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val pageOffset =
                                        (pagerState.currentPage - page) +
                                            pagerState.currentPageOffsetFraction
                                    val absOffset = kotlin.math.abs(pageOffset)
                                    alpha = 1f - (absOffset * 0.4f).coerceIn(0f, 1f)
                                    val scale = 1f - (absOffset * 0.04f).coerceIn(0f, 1f)
                                    scaleX = scale
                                    scaleY = scale
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
                                                if (r == SnackbarResult.ActionPerformed) {
                                                    vm.addCity(city)
                                                }
                                            }
                                        },
                                        onTogglePin = { city ->
                                            vm.togglePinCity(city.zone)
                                        },
                                        onDeleteSelected = vm::deleteSelectedCities,
                                        contentPadding = edgeToEdgeWithFab,
                                    )
                                }

                                Tab.TIMERS -> {
                                    val presets by vm.presets.collectAsStateWithLifecycle()
                                    val now by rememberElapsedTicker(
                                        active = isFocused && timer != null
                                    )
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
                                    val now by rememberElapsedTicker(
                                        active = isFocused && sw.running
                                    )
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

                    if (landscape) {
                        AnimatedVisibility(
                            visible = !selectionActive,
                            enter = fadeIn(animationSpec = fluidSpringSpec()) +
                                slideInHorizontally(
                                    initialOffsetX = { it / 4 },
                                    animationSpec = fluidSpringSpec()
                                ),
                            exit = fadeOut(animationSpec = fluidSpringSpec()) +
                                slideOutHorizontally(
                                    targetOffsetX = { it / 4 },
                                    animationSpec = fluidSpringSpec()
                                ),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp,
                                    bottom = padding.calculateBottomPadding() + 8.dp,
                                    end = padding.calculateEndPadding(ld) + 8.dp
                                )
                        ) {
                            ClockDock(
                                destinations = Tab.entries,
                                selected = targetTab,
                                onSelect = { selectedTab ->
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = Tab.entries.indexOf(selectedTab),
                                            animationSpec = fluidSpringSpec()
                                        )
                                    }
                                },
                                vertical = true,
                                hazeState = hazeState,
                                timerRunning = timer != null,
                                stopwatchRunning = sw.running,
                                stopwatchSnapshot = sw
                            )
                        }

                        FloatingAddButton(
                            visible = fabVisible,
                            label = fabLabel,
                            onClick = onFabClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = railWidth + padding.calculateEndPadding(ld) + 16.dp,
                                    bottom = padding.calculateBottomPadding() + 16.dp,
                                )
                        )
                    } else {
                        AnimatedVisibility(
                            visible = !selectionActive,
                            enter = fadeIn(animationSpec = fluidSpringSpec()) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = fluidSpringSpec()
                                ),
                            exit = fadeOut(animationSpec = fluidSpringSpec()) +
                                slideOutVertically(
                                    targetOffsetY = { it / 4 },
                                    animationSpec = fluidSpringSpec()
                                ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = DOCK_CLEARANCE)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                            ) {
                                ClockDock(
                                    destinations = Tab.entries,
                                    selected = targetTab,
                                    onSelect = { selectedTab ->
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                page = Tab.entries.indexOf(selectedTab),
                                                animationSpec = fluidSpringSpec()
                                            )
                                        }
                                    },
                                    vertical = false,
                                    hazeState = hazeState,
                                    timerRunning = timer != null,
                                    stopwatchRunning = sw.running,
                                    stopwatchSnapshot = sw,
                                    modifier = Modifier.onGloballyPositioned { coords ->
                                        with(density) {
                                            dockHeight = coords.size.height.toDp()
                                        }
                                    },
                                )

                                FloatingAddButton(
                                    visible = fabVisible,
                                    label = fabLabel,
                                    onClick = onFabClick
                                )
                            }
                        }
                    }
                }
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
                onDelete = if (draft.id == 0L) null else {
                    { id -> vm.deleteAlarm(id) }
                },
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
                onDelete = if (draft.id == 0L) null else {
                    { id -> vm.deletePreset(id) }
                },
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
                    screensaverSettings = screensaverSettings,
                    onScreensaverChange = { transform ->
                        scope.launch {
                            screensaverRepository.updateSettings(transform)
                        }
                    },
                    onDismiss = { showSettings = false },
                )

                Tab.TIMERS -> TimerSettingsSheet(
                    settings,
                    vm::updateSettings
                ) { showSettings = false }

                Tab.STOPWATCH -> StopwatchSettingsSheet(
                    settings,
                    vm::updateSettings
                ) { showSettings = false }
            }
        }

        if (showScreensaverSettingsSheet) {
            ScreensaverSettingsSheet(
                settings = screensaverSettings,
                onChange = { transform ->
                    scope.launch {
                        screensaverRepository.updateSettings(transform)
                    }
                },
                onDismiss = {
                    showScreensaverSettingsSheet = false
                }
            )
        }
    }
}

/**
 * The "Alarm in 3 Hours 20 Minutes" line.
 */
@Composable
private fun LiveNewsHeadlineTicker(
    text: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val style = MaterialTheme.typography.titleMedium
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val fade = (TICKER_EDGE_FADE.toPx() / size.width).coerceIn(0f, 0.5f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        fade to Color.Black,
                        1f - fade to Color.Black,
                        1f to Color.Transparent
                    ),
                    blendMode = BlendMode.DstIn
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val viewportWidthPx = constraints.maxWidth.toFloat()
        val safeMarginPx = with(density) { 28.dp.toPx() }
        val velocityPxPerSec = with(density) { 42.dp.toPx() }

        SubcomposeLayout { constraints ->
            val measured = subcompose("ticker_text") {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    maxLines = 1,
                    softWrap = false
                )
            }.first().measure(Constraints())

            val textWidthPx = measured.width.toFloat()
            val needsRunning = textWidthPx > (viewportWidthPx - safeMarginPx)

            layout(constraints.maxWidth, measured.height) {
                if (!needsRunning) {
                    val centerX = ((viewportWidthPx - textWidthPx) / 2f).toInt()
                    measured.placeRelative(centerX, 0)
                }
            }
        }

        var measuredTextWidth by remember(text) { mutableStateOf(0f) }

        SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
            val placeable = subcompose("measure_only") {
                Text(
                    text = text,
                    style = style,
                    color = Color.Transparent,
                    maxLines = 1,
                    softWrap = false
                )
            }.first().measure(Constraints())

            if (measuredTextWidth != placeable.width.toFloat()) {
                measuredTextWidth = placeable.width.toFloat()
            }

            layout(constraints.maxWidth, placeable.height) { }
        }

        val shouldScroll = measuredTextWidth > 0f && measuredTextWidth > (viewportWidthPx - safeMarginPx)
        if (shouldScroll) {
            val offsetX = remember(text, viewportWidthPx, measuredTextWidth) {
                Animatable(viewportWidthPx)
            }

            LaunchedEffect(text, viewportWidthPx, measuredTextWidth) {
                val startX = viewportWidthPx
                val endX = -measuredTextWidth
                val durationMs = (((startX - endX) / velocityPxPerSec) * 1000f)
                    .toInt()
                    .coerceAtLeast(1500)

                while (isActive) {
                    offsetX.snapTo(startX)
                    offsetX.animateTo(
                        targetValue = endX,
                        animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
                    )
                }
            }

            SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
                val placeable = subcompose("scrolling_headline") {
                    Text(
                        text = text,
                        style = style,
                        color = color,
                        maxLines = 1,
                        softWrap = false
                    )
                }.first().measure(Constraints())

                layout(constraints.maxWidth, placeable.height) {
                    placeable.placeRelative(offsetX.value.toInt(), 0)
                }
            }
        }
    }
}
