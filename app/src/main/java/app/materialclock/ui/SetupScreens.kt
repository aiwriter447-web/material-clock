package app.materialclock.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** The things the app needs before it opens, in the order they are asked. */
enum class SetupStep(val number: Int) {
    FULL_SCREEN(1),
    NOTIFICATIONS(2),
    SOUND(3),
}

private const val SETUP_STEP_COUNT = 3

/**
 * A full-screen, one-question-at-a-time page shown before the app opens, for as long as any of the
 * three things below is still missing:
 *
 * 1. full-screen alerts, so a ringing alarm can wake the screen;
 * 2. notifications, so alarms, timers and the stopwatch can show their controls;
 * 3. an alarm sound, so the first alarm does not ring with something the user never picked.
 *
 * Each step stops being asked the moment it is satisfied, and the next one appears. The app opens
 * when none is left.
 *
 * [onSoundChosen] is called with the picked ringtone's address, or null for the system's own
 * default alarm sound.
 */
@Composable
fun SetupScreen(step: SetupStep, onSoundChosen: (String?) -> Unit) {
    val ctx = LocalContext.current

    when (step) {
        SetupStep.FULL_SCREEN -> SetupPage(
            step = step,
            icon = Icons.Outlined.Alarm,
            title = "Full Screen Alerts Needed",
            body = "To reliably wake up your screen when an alarm rings, please allow Full Screen " +
                "Intents in settings.",
            primaryLabel = "Grant Permission",
            onPrimary = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ctx.startActivity(
                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                            .setData(Uri.parse("package:${ctx.packageName}")),
                    )
                }
            },
        )

        SetupStep.NOTIFICATIONS -> {
            // The first tap asks the system. If that was refused the system will not ask again, so
            // from then on the button opens the app's notification settings instead.
            var asked by rememberSaveable { mutableStateOf(false) }
            val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                asked = true
            }
            SetupPage(
                step = step,
                icon = Icons.Outlined.Notifications,
                title = "Turn On Notifications",
                body = "Alarms, timers and the stopwatch show their controls in notifications. " +
                    "Without them an alarm can ring with nothing on screen to stop it.",
                footnote = if (asked) "Notifications are still off. Open settings and switch them on." else null,
                primaryLabel = if (asked || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    "Open Settings"
                } else {
                    "Allow Notifications"
                },
                onPrimary = {
                    if (asked || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        openNotificationSettings(ctx)
                    } else {
                        request.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
        }

        SetupStep.SOUND -> {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    }
                    onSoundChosen(picked?.toString())
                }
            }
            SetupPage(
                step = step,
                icon = Icons.Outlined.MusicNote,
                title = "Choose Your Alarm Sound",
                body = "Pick the sound your alarms ring with. You can give any single alarm its " +
                    "own sound later.",
                primaryLabel = "Choose Sound",
                onPrimary = {
                    picker.launch(
                        Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false),
                    )
                },
                secondaryLabel = "Use the default sound",
                onSecondary = { onSoundChosen(null) },
            )
        }
    }
}

private fun openNotificationSettings(ctx: Context) {
    runCatching {
        ctx.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName),
        )
    }
}

/** One question: where we are, an icon, a title, a few words, and what to press. */
@Composable
private fun SetupPage(
    step: SetupStep,
    icon: ImageVector,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    footnote: String? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepDots(step)
            Spacer(Modifier.height(32.dp))
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(body, textAlign = TextAlign.Center)
            if (footnote != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    footnote,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = onPrimary) { Text(primaryLabel) }
            if (secondaryLabel != null && onSecondary != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSecondary) { Text(secondaryLabel) }
            }
        }
    }
}

/** "Step 2 of 3" as three marks, the current one long and in the main colour. */
@Composable
private fun StepDots(step: SetupStep) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..SETUP_STEP_COUNT) {
                val current = i == step.number
                Box(
                    Modifier
                        .height(8.dp)
                        .width(if (current) 28.dp else 8.dp)
                        .clip(if (current) RoundedCornerShape(4.dp) else CircleShape)
                        .background(
                            if (i <= step.number) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Step ${step.number} of $SETUP_STEP_COUNT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
