package app.materialclock.alarm

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.materialclock.data.ClockSettings
import app.materialclock.data.ClockStore
import app.materialclock.data.DismissMethod
import app.materialclock.ui.rememberWallTicker
import app.materialclock.ui.theme.ClockTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt

/* ───────────────────────────────────────────────
   EXPRESSIVE ANIMATION CONFIGURATION
   ─────────────────────────────────────────────── */

private const val EXIT_DURATION = 240L

private val ExpressiveSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

private val SoftSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)

private val FastSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

/* ───────────────────────────────────────────────
   ALARM ACTIVITY
   ─────────────────────────────────────────────── */

class AlarmRingActivity : ComponentActivity() {

    @Volatile
    private var volumeButtonsControlVolume = true

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (
            volumeButtonsControlVolume &&
            event.action == KeyEvent.ACTION_DOWN &&
            (
                event.keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
            )
        ) {

            val direction =
                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    AudioManager.ADJUST_RAISE
                } else {
                    AudioManager.ADJUST_LOWER
                }

            getSystemService<AudioManager>()
                ?.adjustStreamVolume(
                    AudioManager.STREAM_ALARM,
                    direction,
                    AudioManager.FLAG_SHOW_UI
                )

            return true
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /* Lock screen support */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true)
            setTurnScreenOn(true)

        } else {

            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        getSystemService<KeyguardManager>()
            ?.requestDismissKeyguard(this, null)

        val id = intent.getLongExtra(
            AlarmReceiver.EXTRA_ID,
            -1L
        )

        val label = intent.getStringExtra(
            EXTRA_LABEL
        ).orEmpty()

        val store = ClockStore(applicationContext)

        /* Disable accidental back dismissal */

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            }
        )

        setContent {

            val settings by store.settings
                .collectAsStateWithLifecycle(
                    initialValue = ClockSettings()
                )

            LaunchedEffect(
                settings.alarms.volumeButtonsControlVolume
            ) {
                volumeButtonsControlVolume =
                    settings.alarms.volumeButtonsControlVolume
            }

            ClockTheme(settings.theme) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {

                    ExpressiveRingingScreen(
                        label = label,
                        snoozeMinutes = settings.alarms.snoozeMinutes,
                        dismissMethod = settings.alarms.dismissMethod,

                        onSnooze = {

                            sendBroadcast(
                                action(
                                    ClockActionReceiver.ACTION_SNOOZE,
                                    id
                                )
                            )

                            finishAfterAnimation()
                        },

                        onDismiss = {

                            sendBroadcast(
                                action(
                                    ClockActionReceiver.ACTION_DISMISS,
                                    id
                                )
                            )

                            finishAfterAnimation()
                        }
                    )
                }
            }
        }
    }

    private fun finishAfterAnimation() {

        lifecycleScope.launch {
            delay(EXIT_DURATION)
            if (!isFinishing && !isDestroyed) {
                finish()
            }
        }
    }

    private fun action(
        name: String,
        id: Long
    ): Intent {

        return Intent(
            this,
            ClockActionReceiver::class.java
        )
            .setPackage(packageName)
            .setAction(name)
            .putExtra(
                AlarmReceiver.EXTRA_ID,
                id
            )
    }

    companion object {

        const val EXTRA_LABEL = "label"

        fun intent(
            context: Context,
            id: Long,
            label: String
        ): PendingIntent {

            return PendingIntent.getActivity(
                context,
                id.toInt(),
                Intent(
                    context,
                    AlarmRingActivity::class.java
                )
                    .setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                    .putExtra(
                        AlarmReceiver.EXTRA_ID,
                        id
                    )
                    .putExtra(
                        EXTRA_LABEL,
                        label
                    ),

                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

/* ───────────────────────────────────────────────
   EXPRESSIVE RINGING SCREEN
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveRingingScreen(
    label: String,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {

    val nowMillis by rememberWallTicker()

    val context = LocalContext.current

    val is24Hour = android.text.format.DateFormat
        .is24HourFormat(context)

    val time = remember(nowMillis) {
        Instant.ofEpochMilli(nowMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
    }

    var entered by remember {
        mutableStateOf(false)
    }

    var exiting by remember {
        mutableStateOf(false)
    }

    var actionTaken by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        entered = true
    }

    val screenAlpha by animateFloatAsState(
        targetValue = when {
            exiting -> 0f
            entered -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 220
        ),
        label = "screenAlpha"
    )

    val screenScale by animateFloatAsState(
        targetValue = when {
            exiting -> 0.94f
            entered -> 1f
            else -> 0.92f
        },
        animationSpec = ExpressiveSpring,
        label = "screenScale"
    )

    val handleSnooze = {
        if (!actionTaken) {
            actionTaken = true
            exiting = true
            onSnooze()
        }
    }

    val handleDismiss = {
        if (!actionTaken) {
            actionTaken = true
            exiting = true
            onDismiss()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = screenAlpha
                scaleX = screenScale
                scaleY = screenScale
            }
    ) {

        AnimatedAmbientBackground()

        if (maxWidth > maxHeight) {

            ExpressiveLandscape(
                label = label,
                time = time,
                is24Hour = is24Hour,
                snoozeMinutes = snoozeMinutes,
                dismissMethod = dismissMethod,
                onSnooze = handleSnooze,
                onDismiss = handleDismiss
            )

        } else {

            ExpressivePortrait(
                label = label,
                time = time,
                is24Hour = is24Hour,
                snoozeMinutes = snoozeMinutes,
                dismissMethod = dismissMethod,
                onSnooze = handleSnooze,
                onDismiss = handleDismiss
            )
        }
    }
}

/* ───────────────────────────────────────────────
   ANIMATED AMBIENT BACKGROUND
   ─────────────────────────────────────────────── */

@Composable
private fun AnimatedAmbientBackground() {

    val infinite = rememberInfiniteTransition(
        label = "ambientAnimation"
    )

    val glowAlpha by infinite.animateFloat(
        initialValue = 0.025f,
        targetValue = 0.085f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientGlow"
    )

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = glowAlpha),
                        Color.Transparent,
                        primary.copy(
                            alpha = glowAlpha * 0.45f
                        )
                    )
                )
            )
    )
}

/* ───────────────────────────────────────────────
   PORTRAIT LAYOUT
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressivePortrait(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 36.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AnimatedEntrance(
            delayMillis = 0
        ) {

            Text(
                text = label.ifBlank { "Alarm" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(
            Modifier.height(24.dp)
        )

        AnimatedEntrance(
            delayMillis = 90
        ) {

            ExpressiveClock(
                time = time,
                is24Hour = is24Hour,
                compact = false
            )
        }

        Spacer(
            Modifier.height(56.dp)
        )

        AnimatedEntrance(
            delayMillis = 170
        ) {

            ExpressiveDismissControl(
                dismissMethod = dismissMethod,
                onDismiss = onDismiss,
                height = 96.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(
            Modifier.height(14.dp)
        )

        AnimatedEntrance(
            delayMillis = 250
        ) {

            ExpressiveSnoozeButton(
                snoozeMinutes = snoozeMinutes,
                onSnooze = onSnooze,
                height = 72.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* ───────────────────────────────────────────────
   LANDSCAPE LAYOUT
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveLandscape(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 32.dp,
                vertical = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            AnimatedEntrance(
                delayMillis = 0
            ) {

                Text(
                    text = label.ifBlank { "Alarm" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )

            AnimatedEntrance(
                delayMillis = 90
            ) {

                ExpressiveClock(
                    time = time,
                    is24Hour = is24Hour,
                    compact = true
                )
            }
        }

        Spacer(
            Modifier.width(24.dp)
        )

        Column(
            modifier = Modifier.weight(0.75f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AnimatedEntrance(
                delayMillis = 170
            ) {

                ExpressiveDismissControl(
                    dismissMethod = dismissMethod,
                    onDismiss = onDismiss,
                    height = 76.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedEntrance(
                delayMillis = 250
            ) {

                ExpressiveSnoozeButton(
                    snoozeMinutes = snoozeMinutes,
                    onSnooze = onSnooze,
                    height = 60.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/* ───────────────────────────────────────────────
   STAGGERED ENTRANCE ANIMATION
   ─────────────────────────────────────────────── */

@Composable
private fun AnimatedEntrance(
    delayMillis: Int,
    content: @Composable () -> Unit
) {

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "entranceAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = SoftSpring,
        label = "entranceScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 35f,
        animationSpec = ExpressiveSpring,
        label = "entranceTranslation"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
            this.translationY = translationY
        }
    ) {
        content()
    }
}

/* ───────────────────────────────────────────────
   EXPRESSIVE CLOCK
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveClock(
    time: LocalTime,
    is24Hour: Boolean,
    compact: Boolean
) {

    val hour = if (is24Hour) {
        time.hour
    } else {
        (time.hour % 12).takeIf {
            it != 0
        } ?: 12
    }

    val timeText = "%02d:%02d".format(
        hour,
        time.minute
    )

    val infinite = rememberInfiniteTransition(
        label = "clockBreathing"
    )

    val breathingScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockScale"
    )

    val glowAlpha by infinite.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockGlow"
    )

    val primary = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.graphicsLayer {
            scaleX = breathingScale
            scaleY = breathingScale
        }
    ) {

        Box(
            modifier = Modifier
                .size(
                    width = if (compact) 180.dp else 250.dp,
                    height = if (compact) 80.dp else 120.dp
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(
                                alpha = glowAlpha
                            ),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = timeText,
                fontSize = if (compact) 64.sp else 88.sp,
                fontWeight = FontWeight.Medium,
                color = primary,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.alignByBaseline()
            )

            if (!is24Hour) {

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    text = if (time.hour < 12) {
                        "AM"
                    } else {
                        "PM"
                    },
                    fontSize = if (compact) 22.sp else 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

/* ───────────────────────────────────────────────
   DISMISS METHOD SELECTOR
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveDismissControl(
    dismissMethod: DismissMethod,
    onDismiss: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier
) {

    when (dismissMethod) {

        DismissMethod.TAP -> {

            ExpressiveDismissButton(
                onDismiss = onDismiss,
                height = height,
                modifier = modifier
            )
        }

        DismissMethod.SWIPE -> {

            ExpressiveSwipeToDismiss(
                onDismiss = onDismiss,
                height = height,
                modifier = modifier
            )
        }
    }
}

/* ───────────────────────────────────────────────
   BOUNCY DISMISS BUTTON
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveDismissButton(
    onDismiss: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()

    val haptic = LocalHapticFeedback.current

    var activated by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = when {
            activated -> 0.96f
            pressed -> 0.92f
            else -> 1f
        },
        animationSpec = ExpressiveSpring,
        label = "dismissScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (pressed || activated) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        animationSpec = tween(180),
        label = "dismissBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = if (pressed || activated) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onTertiaryContainer
        },
        label = "dismissContent"
    )

    Surface(
        onClick = {

            if (!activated) {

                activated = true

                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )

                onDismiss()
            }
        },
        enabled = !activated,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = backgroundColor,
        interactionSource = interactionSource
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.AlarmOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Text(
                text = "Dismiss",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/* ───────────────────────────────────────────────
   EXPRESSIVE SNOOZE BUTTON
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveSnoozeButton(
    snoozeMinutes: Int,
    onSnooze: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()

    val haptic = LocalHapticFeedback.current

    var activated by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = ExpressiveSpring,
        label = "snoozeScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (pressed) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(180),
        label = "snoozeBackground"
    )

    Surface(
        onClick = {

            if (!activated) {

                activated = true

                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )

                onSnooze()
            }
        },
        enabled = !activated,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        interactionSource = interactionSource
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.Snooze,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(26.dp)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Text(
                text = "Snooze $snoozeMinutes min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/* ───────────────────────────────────────────────
   FLUID SWIPE TO DISMISS
   ─────────────────────────────────────────────── */

@Composable
private fun ExpressiveSwipeToDismiss(
    onDismiss: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier
) {

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val inset = 4.dp

    val handleSize = height - inset * 2

    val handlePx = with(density) {
        handleSize.toPx()
    }

    val insetPx = with(density) {
        inset.toPx()
    }

    var trackWidth by remember {
        mutableFloatStateOf(0f)
    }

    val maxOffset = (
        trackWidth - handlePx - insetPx * 2
    ).coerceAtLeast(0f)

    var dragOffset by remember {
        mutableFloatStateOf(0f)
    }

    var dragging by remember {
        mutableStateOf(false)
    }

    var dismissed by remember {
        mutableStateOf(false)
    }

    var thresholdReached by remember {
        mutableStateOf(false)
    }

    var springJob by remember {
        mutableStateOf<Job?>(null)
    }

    val progress = if (maxOffset > 0f) {
        (dragOffset / maxOffset).coerceIn(0f, 1f)
    } else {
        0f
    }

    val backgroundColor = lerp(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primaryContainer,
        progress
    )

    val handleColor = lerp(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.primary,
        progress
    )

    val handleContentColor = lerp(
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.onPrimary,
        progress
    )

    val handleScale by animateFloatAsState(
        targetValue = when {
            dismissed -> 1.08f
            dragging -> 1.05f
            else -> 1f
        },
        animationSpec = ExpressiveSpring,
        label = "swipeHandleScale"
    )

    DisposableEffect(Unit) {
        onDispose {
            springJob?.cancel()
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(backgroundColor)
            .onSizeChanged {
                trackWidth = it.width.toFloat()
            },
        contentAlignment = Alignment.CenterStart
    ) {

        Text(
            text = if (thresholdReached) {
                "Release to dismiss"
            } else {
                "Swipe to dismiss"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 1f - progress * 0.65f
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .padding(start = inset)
                .offset {
                    IntOffset(
                        dragOffset.roundToInt(),
                        0
                    )
                }
                .size(handleSize)
                .graphicsLayer {
                    scaleX = handleScale
                    scaleY = handleScale
                }
                .clip(CircleShape)
                .background(handleColor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !dismissed,

                    onDragStarted = {

                        springJob?.cancel()
                        springJob = null

                        dragging = true
                    },

                    state = rememberDraggableState { delta ->

                        if (!dismissed) {

                            dragOffset = (
                                dragOffset + delta
                            ).coerceIn(0f, maxOffset)

                            val reached =
                                maxOffset > 0f &&
                                    dragOffset >= maxOffset * 0.7f

                            if (
                                reached &&
                                !thresholdReached
                            ) {

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            }

                            thresholdReached = reached
                        }
                    },

                    onDragStopped = {

                        dragging = false

                        if (dismissed) {
                            return@draggable
                        }

                        val shouldDismiss =
                            maxOffset > 0f &&
                                dragOffset >= maxOffset * 0.7f

                        if (shouldDismiss) {

                            dismissed = true

                            springJob?.cancel()

                            springJob = scope.launch {

                                val animation = Animatable(
                                    dragOffset
                                )

                                animation.animateTo(
                                    targetValue = maxOffset,
                                    animationSpec = FastSpring
                                ) {
                                    dragOffset = value
                                }

                                onDismiss()
                            }

                        } else {

                            thresholdReached = false

                            springJob?.cancel()

                            springJob = scope.launch {

                                val animation = Animatable(
                                    dragOffset
                                )

                                animation.animateTo(
                                    targetValue = 0f,
                                    animationSpec = SoftSpring
                                ) {
                                    dragOffset = value
                                }
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Swipe to dismiss",
                tint = handleContentColor,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
