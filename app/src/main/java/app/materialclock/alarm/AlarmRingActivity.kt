package app.materialclock.alarm

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.materialclock.data.ClockStore
import app.materialclock.ui.rememberWallTicker
import app.materialclock.ui.screens.WidePill
import app.materialclock.ui.theme.ClockTheme
import app.materialclock.ui.theme.Numerals
import app.materialclock.data.ClockSettings
import kotlinx.coroutines.flow.map
import java.time.LocalTime

/**
 * The screen a ringing alarm puts in front of you.
 *
 * ## Getting onto a locked phone
 *
 * `setShowWhenLocked` and `setTurnScreenOn` are the modern replacements for the deprecated window
 * flags, and they are the reason this appears over the keyguard with the display off. They are set
 * before `setContent` because the window attributes have to be right before the first frame, and
 * `requestDismissKeyguard` is what lets an insecure lock screen fall away so the buttons are
 * reachable without unlocking. A secure lock screen correctly stays up, because dismissing an
 * alarm is allowed and walking into someone's phone is not.
 *
 * ## No back button
 *
 * Backing out of a ringing alarm would leave it ringing with no way to reach the controls except
 * the shade. The only exits are Snooze and Dismiss, which is the same contract every alarm clock
 * has had since they had a bell on top.
 */
class AlarmRingActivity : ComponentActivity() {

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
            ClockTheme(settings.theme) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
                    Ringing(
                        label = label,
                        snoozeMinutes = settings.alarms.snoozeMinutes,
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

/**
 * The ringing face itself.
 *
 * ## Live time, not a snapshot
 *
 * [rememberWallTicker] is what makes the clock on this screen actually tick — a `LocalTime.now()`
 * read once at composition would freeze the instant the alarm fired, which is wrong for anyone who
 * takes more than a few seconds to reach for Snooze.
 *
 * ## Portrait vs. landscape
 *
 * [androidx.compose.foundation.layout.BoxWithConstraints] decides the split by comparing the
 * available width and height rather than reading device orientation directly, so a foldable or a
 * split-screen window gets the layout that actually fits it. Portrait keeps everything stacked and
 * centred, the way a phone held up to a groggy face wants. Landscape — nightstand orientation —
 * puts the time on the left where a half-open eye lands first, and the two actions in a stack on
 * the right, sized for a thumb rather than a full swipe across the width of the screen.
 */
@androidx.compose.runtime.Composable
private fun Ringing(
    label: String,
    snoozeMinutes: Int,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    val nowMillis by rememberWallTicker()
    val context = androidx.compose.ui.platform.LocalContext.current
    // Matches the format the alarm's own edit sheet already keys off, so what you set is what
    // rings: system default, unless the device itself is in 24-hour mode.
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val time = remember(nowMillis) {
        java.time.Instant.ofEpochMilli(nowMillis).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            RingingLandscape(label, time, is24Hour, snoozeMinutes, onSnooze, onDismiss)
        } else {
            RingingPortrait(label, time, is24Hour, snoozeMinutes, onSnooze, onDismiss)
        }
    }
}

@androidx.compose.runtime.Composable
private fun RingingPortrait(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    snoozeMinutes: Int,
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
        Spacer(Modifier.height(28.dp))
        RingTime(time, is24Hour, capHeight = 132.dp)
        Spacer(Modifier.height(56.dp))
        WidePill(
            text = "Dismiss",
            onClick = onDismiss,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
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
            RingTime(time, is24Hour, capHeight = 88.dp)
        }
        Spacer(Modifier.width(24.dp))
        Column(
            modifier = Modifier.weight(0.62f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WidePill(
                text = "Dismiss",
                onClick = onDismiss,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
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

/** The ultra-condensed clock face the grid uses, plus an AM/PM tag when not in 24-hour mode. */
@androidx.compose.runtime.Composable
private fun RingTime(
    time: LocalTime,
    is24Hour: Boolean,
    capHeight: androidx.compose.ui.unit.Dp,
) {
    val hour = if (is24Hour) time.hour else ((time.hour % 12).takeIf { it != 0 } ?: 12)
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Bottom) {
        Numerals(
            text = "%02d:%02d".format(hour, time.minute),
            capHeight = capHeight,
            color = MaterialTheme.colorScheme.primary,
            width = app.materialclock.ui.theme.ClockFace.CONDENSED,
            weight = app.materialclock.ui.theme.ClockFace.WEIGHT_ON,
            tracking = app.materialclock.ui.theme.ClockFace.CONDENSED_TRACKING,
        )
        if (!is24Hour) {
            Spacer(Modifier.width(8.dp))
            Text(
                if (time.hour < 12) "AM" else "PM",
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = capHeight * 0.14f),
            )
        }
    }
}