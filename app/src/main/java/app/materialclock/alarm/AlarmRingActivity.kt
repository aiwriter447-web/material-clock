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
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.materialclock.data.ClockStore
import app.materialclock.data.DismissMethod
import app.materialclock.ui.rememberWallTicker
import app.materialclock.ui.screens.WidePill
import app.materialclock.ui.theme.ClockTheme
import app.materialclock.data.ClockSettings
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.roundToInt

class AlarmRingActivity : ComponentActivity() {

    @Volatile private var volumeButtonsControlVolume = true

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (volumeButtonsControlVolume &&
            event.action == KeyEvent.ACTION_DOWN &&
            (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        ) {
            val direction = if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                AudioManager.ADJUST_RAISE
            } else {
                AudioManager.ADJUST_LOWER
            }
            getSystemService<AudioManager>()
                ?.adjustStreamVolume(AudioManager.STREAM_ALARM, direction, AudioManager.FLAG_SHOW_UI)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        getSystemService<KeyguardManager>()?.requestDismissKeyguard(this, null)

        val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L)
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val store = ClockStore(applicationContext)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        setContent {
            val settings by store.settings
                .map { it }
                .collectAsStateWithLifecycle(initialValue = ClockSettings())

            LaunchedEffect(settings.alarms.volumeButtonsControlVolume) {
                volumeButtonsControlVolume = settings.alarms.volumeButtonsControlVolume
            }

            ClockTheme(settings.theme) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
                    Ringing(
                        label = label,
                        snoozeMinutes = settings.alarms.snoozeMinutes,
                        dismissMethod = settings.alarms.dismissMethod,
                        onSnooze = {
                            sendBroadcast(action(ClockActionReceiver.ACTION_SNOOZE, id))
                            finish()
                        },
                        onDismiss = {
                            sendBroadcast(action(ClockActionReceiver.ACTION_DISMISS, id))
                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun action(name: String, id: Long) =
        Intent(this, ClockActionReceiver::class.java)
            .setPackage(packageName)
            .setAction(name)
            .putExtra(AlarmReceiver.EXTRA_ID, id)

    companion object {
        const val EXTRA_LABEL = "label"

        fun intent(context: Context, id: Long, label: String): PendingIntent =
            PendingIntent.getActivity(
                context,
                id.toInt(),
                Intent(context, AlarmRingActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(AlarmReceiver.EXTRA_ID, id)
                    .putExtra(EXTRA_LABEL, label),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
    }
}

@androidx.compose.runtime.Composable
private fun Ringing(
    label: String,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    val nowMillis by rememberWallTicker()
    val context = androidx.compose.ui.platform.LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val time = remember(nowMillis) {
        java.time.Instant.ofEpochMilli(nowMillis).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            RingingLandscape(label, time, is24Hour, snoozeMinutes, dismissMethod, onSnooze, onDismiss)
        } else {
            RingingPortrait(label, time, is24Hour, snoozeMinutes, dismissMethod, onSnooze, onDismiss)
        }
    }
}

@androidx.compose.runtime.Composable
private fun RingingPortrait(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label.ifBlank { "Alarm" },
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        RingTime(time, is24Hour)
        Spacer(Modifier.height(56.dp))
        DismissControl(
            dismissMethod = dismissMethod,
            onDismiss = onDismiss,
            height = 96.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        WidePill(
            text = "Snooze $snoozeMinutes min",
            onClick = onSnooze,
            outlined = true,
            content = MaterialTheme.colorScheme.onSurface,
            height = 72.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@androidx.compose.runtime.Composable
private fun RingingLandscape(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    snoozeMinutes: Int,
    dismissMethod: DismissMethod,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                label.ifBlank { "Alarm" },
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            RingTime(time, is24Hour)
        }
        Spacer(Modifier.width(24.dp))
        Column(
            modifier = Modifier.weight(0.62f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DismissControl(
                dismissMethod = dismissMethod,
                onDismiss = onDismiss,
                height = 76.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            WidePill(
                text = "Snooze $snoozeMinutes min",
                onClick = onSnooze,
                outlined = true,
                content = MaterialTheme.colorScheme.onSurface,
                height = 60.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun DismissControl(
    dismissMethod: DismissMethod,
    onDismiss: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    when (dismissMethod) {
        DismissMethod.TAP -> WidePill(
            text = "Dismiss",
            onClick = onDismiss,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            height = height,
            modifier = modifier,
        )
        DismissMethod.SWIPE -> SwipeToDismissPill(onDismiss = onDismiss, height = height, modifier = modifier)
    }
}

@androidx.compose.runtime.Composable
private fun SwipeToDismissPill(
    onDismiss: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val handleInset = 4.dp
    val handleSize = height - handleInset * 2
    val density = androidx.compose.ui.platform.LocalDensity.current
    val handlePx = with(density) { handleSize.toPx() }
    val insetPx = with(density) { handleInset.toPx() }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val maxOffset = (trackWidthPx - handlePx - insetPx * 2).coerceAtLeast(0f)

    val offsetX = remember { Animatable(0f) }
    var dismissed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val handleColor = MaterialTheme.colorScheme.tertiaryContainer
    val handleContent = MaterialTheme.colorScheme.onTertiaryContainer
    val label = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
            .onSizeChanged { trackWidthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart,
    ) {
        val labelAlpha = if (maxOffset > 0f) 1f - (offsetX.value / maxOffset).coerceIn(0f, 1f) else 1f
        Text(
            "Swipe to dismiss",
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = label.copy(alpha = labelAlpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .padding(start = handleInset)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(handleSize)
                .clip(CircleShape)
                .background(handleColor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !dismissed,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxOffset))
                        }
                    },
                    onDragStopped = {
                        if (maxOffset > 0f) {
                            if (offsetX.value >= maxOffset * 0.7f) {
                                dismissed = true
                                onDismiss()
                                offsetX.animateTo(maxOffset)
                            } else {
                                offsetX.animateTo(0f)
                            }
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("›", style = MaterialTheme.typography.headlineSmallEmphasized, color = handleContent)
        }
    }
}

/** 
 * FONT FIX: 
 * Removed the old Numerals() that used a condensed custom font.
 * Replaced it with standard MaterialTheme typography (Google Sans Flex).
 * Increased the size of AM/PM considerably to match modern design language.
 */
@androidx.compose.runtime.Composable
private fun RingTime(
    time: LocalTime,
    is24Hour: Boolean,
) {
    val hour = if (is24Hour) time.hour else ((time.hour % 12).takeIf { it != 0 } ?: 12)
    val timeText = "%02d:%02d".format(hour, time.minute)
    
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = timeText,
            fontSize = 96.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alignByBaseline()
        )
        
        if (!is24Hour) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (time.hour < 12) "AM" else "PM",
                fontSize = 32.sp, // INCREASED SIZE
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline().padding(bottom = 8.dp)
            )
        }
    }
}
