package app.materialclock.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.materialclock.BuildConfig

/**
 * About section for Material Clock.
 *
 * Provides information about privacy practices,
 * app permissions, open-source licences,
 * third-party acknowledgements and app version.
 *
 * Designed using Material 3 rounded surfaces
 * for consistency with the application's
 * settings interface.
 */
@Composable
fun AboutRows() {

    var open by remember {
        mutableStateOf<AboutPage?>(null)
    }

    // ─────────────────────────────────────
    // About Section
    // ─────────────────────────────────────

    SectionLabel("About")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp
            ),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
    ) {

        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {

            // Privacy

            NavigateRow(
                title = "Privacy",
                subtitle = "Your data stays private and on your device",
                onClick = {
                    open = AboutPage.PRIVACY
                },
            )

            AboutDivider()

            // Permissions

            NavigateRow(
                title = "Permissions",
                subtitle = "Understand how app permissions are used",
                onClick = {
                    open = AboutPage.PERMISSIONS
                },
            )

            AboutDivider()

            // Open-source licences

            NavigateRow(
                title = "Licences",
                subtitle = "Open-source software and acknowledgements",
                onClick = {
                    open = AboutPage.LICENCES
                },
            )
        }
    }

    // ─────────────────────────────────────
    // Application Version
    // ─────────────────────────────────────

    Text(
        text = "Clock • Version ${BuildConfig.VERSION_NAME}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp
            ),
    )

    // ─────────────────────────────────────
    // Information Dialog
    // ─────────────────────────────────────

    open?.let { page ->

        AlertDialog(
            onDismissRequest = {
                open = null
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        open = null
                    }
                ) {
                    Text("Close")
                }
            },

            title = {
                Text(page.title)
            },

            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {

                    page.body.forEach { paragraph ->

                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
        )
    }
}

/**
 * Divider consistent with the application's
 * Material 3 settings cards.
 */
@Composable
private fun AboutDivider() {

    HorizontalDivider(
        modifier = Modifier.padding(
            horizontal = 20.dp
        ),
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant
            .copy(alpha = 0.4f)
    )
}

/**
 * Information displayed in the About dialogs.
 *
 * Each page provides clear explanations
 * of the application's functionality,
 * permissions and third-party components.
 */
private enum class AboutPage(
    val title: String,
    val body: List<String>
) {

    // ─────────────────────────────────────
    // Privacy
    // ─────────────────────────────────────

    PRIVACY(
        "Privacy",
        listOf(

            "Your privacy is important. Material Clock is designed to " +
                "operate entirely on your device without requiring an " +
                "account, collecting personal information or tracking " +
                "your activity.",

            "The application does not include analytics, advertising " +
                "identifiers, crash-reporting services or other " +
                "third-party tracking technologies.",

            "Material Clock does not request the Android INTERNET " +
                "permission. As a result, the application cannot " +
                "establish ordinary network connections or transmit " +
                "information directly to external servers.",

            "Your alarms, timers, world clocks and application " +
                "preferences are stored locally within the " +
                "application's private storage. This information " +
                "is not accessible to other ordinary applications.",

            "If Android device backup is enabled, eligible application " +
                "data may be included in backups managed by the " +
                "operating system. Backup availability and behaviour " +
                "depend on your device settings and Android version.",

            "Uninstalling the application removes its locally stored " +
                "application data. Previously created system backups " +
                "may remain subject to your device's backup settings.",

            "Alarm and timer sounds are selected from audio resources " +
                "available on your device. The application stores " +
                "the selected sound reference rather than copying " +
                "the original audio file.",

            "Material Clock is designed to provide its core features " +
                "without requiring an internet connection."
        )
    ),

    // ─────────────────────────────────────
    // Permissions
    // ─────────────────────────────────────

    PERMISSIONS(
        "Permissions",
        listOf(

            "Material Clock requests permissions only to support " +
                "features such as scheduled alarms, notifications, " +
                "vibration and reliable background operation.",

            "Alarms and reminders (USE_EXACT_ALARM): Allows the " +
                "application to schedule alarms for precise times. " +
                "Without exact alarm capabilities, Android may " +
                "delay scheduled events to optimise battery usage.",

            "Notifications (POST_NOTIFICATIONS): Enables notifications " +
                "for alarms, timers and stopwatch activities. " +
                "Notification availability and controls depend " +
                "on your Android version and notification settings.",

            "Full-screen notifications (USE_FULL_SCREEN_INTENT): " +
                "Allows eligible ringing alarms to display a " +
                "full-screen interface, including when the device " +
                "is locked, subject to Android's permission controls.",

            "Run at startup (RECEIVE_BOOT_COMPLETED): Allows the " +
                "application to restore scheduled alarms after " +
                "the device restarts.",

            "Foreground service (FOREGROUND_SERVICE and " +
                "FOREGROUND_SERVICE_MEDIA_PLAYBACK): Supports " +
                "continued alarm audio playback while an alarm " +
                "is actively ringing.",

            "Vibration (VIBRATE): Enables vibration for alarms " +
                "and timers when vibration is enabled in " +
                "their respective settings.",

            "Wake lock (WAKE_LOCK): Helps keep the device awake " +
                "when necessary for active alarm functionality.",

            "The application does not request permissions to " +
                "access your location, contacts, microphone, " +
                "camera or general device storage.",

            "You can review applicable permissions and adjust " +
                "their availability through your device's " +
                "system settings."
        )
    ),

    // ─────────────────────────────────────
    // Licences
    // ─────────────────────────────────────

    LICENCES(
        "Licences",
        listOf(

            "Material Clock incorporates open-source software, " +
                "libraries and fonts developed by independent " +
                "contributors and organisations. Their work " +
                "is acknowledged below.",

            "Application source code: Material Clock is " +
                "distributed under the MIT License.",

            "Bundled fonts: The included font resources are " +
                "licensed under the SIL Open Font License 1.1. " +
                "The complete licence terms are available " +
                "from the official Open Font License project.",

            "Google Sans Flex: Copyright © 2015 Google LLC. " +
                "Licensed under the SIL Open Font License 1.1. " +
                "The font has been modified by combining " +
                "script-specific subsets into a single " +
                "variable font suitable for Android. " +
                "The original glyph outlines and variation " +
                "axes remain unchanged.",

            "Noto Sans Arabic: Copyright © 2022 " +
                "The Noto Project Authors. Licensed under " +
                "the SIL Open Font License 1.1. " +
                "A subset containing Eastern Arabic numerals " +
                "(U+0660–U+0669) is included for clock widgets.",

            "AndroidX, Jetpack Compose, Material Symbols " +
                "and androidx.graphics.shapes: These components " +
                "are distributed under the Apache License 2.0. " +
                "Copyright The Android Open Source Project " +
                "and Google LLC.",

            "Google, Android, Material, Pixel and Google Sans " +
                "are trademarks or registered trademarks " +
                "of Google LLC.",

            "Material Clock is an independent application " +
                "inspired by a publicly presented Material 3 " +
                "Expressive design concept. It is not affiliated " +
                "with, sponsored by or officially endorsed " +
                "by Google LLC.",

            "We acknowledge and appreciate the contributions " +
                "of the open-source community and the developers " +
                "whose work supports this application."
        )
    )
}