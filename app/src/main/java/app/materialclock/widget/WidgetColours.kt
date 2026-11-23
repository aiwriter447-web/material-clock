package app.materialclock.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import app.materialclock.data.AnalogLook
import app.materialclock.data.ClockSettings
import app.materialclock.data.ColourSource
import app.materialclock.data.DarkMode
import app.materialclock.data.WidgetAppearance
import app.materialclock.data.WidgetConfig
import kotlin.math.roundToInt
import app.materialclock.ui.theme.toAmoledBlack

/**
 * The widget's colours, resolved without Compose.
 *
 * `ColorScheme` is a plain data class (only the *theming* composable is a composable), so the same
 * `Palette` entries and the same `toAmoledBlack` transform the app uses are reachable from a
 * `BroadcastReceiver`. That matters: "match the app" has to mean the identical colours, not an
 * approximation of them.
 */
object WidgetColours {

    fun resolve(context: Context, config: WidgetConfig, appSettings: ClockSettings): FaceRenderer.Colours {
        val scheme = scheme(context, config, appSettings)

        // Seven roles, not one accent repeated.
        //
        // Every one of these is drawn on the *neutral* surface family, and that is deliberate:
        // M3 guarantees contrast within a container/on-container pair and for the accent roles
        // against `surface`, but it guarantees nothing for tertiary-on-primaryContainer. So the
        // face stays a surface role, one that in a dynamic scheme is already wallpaper-tinted, and
        // the three hands are free to be genuinely different colours on top of it.
        //
        // The hands run primary → tertiary → secondary in that order, and the order is the point.
        // In a *dynamic* scheme `secondary` is a desaturated near-copy of `primary`, and putting
        // those two on the hour and minute hands makes the pair read as one colour. By contrast
        // `tertiary` is rotated well off the seed. So the two hands the eye actually reads take
        // primary and tertiary, and secondary goes to the second hand, which is a hairline in
        // motion and does not have to fight for separation.
        //
        // The minor indices drop to `onSurfaceVariant` so a dial with four numerals and eight dots
        // reads as two levels rather than twelve equal marks.
        val classic = FaceRenderer.Colours(
            face = scheme.surfaceContainerHigh.toArgb(),
            onFace = scheme.onSurface.toArgb(),
            minor = scheme.onSurfaceVariant.toArgb(),
            outline = scheme.outlineVariant.toArgb(),
            accent = scheme.primary.toArgb(),
            hourHand = scheme.primary.toArgb(),
            minuteHand = scheme.tertiary.toArgb(),
            secondHand = scheme.secondary.toArgb(),
            // Neutral, because the pin sits on top of all three hands and matching any one of them
            // would make that hand look severed at the centre.
            pin = scheme.onSurface.toArgb(),
        )
        if (config.look == AnalogLook.CLASSIC) return classic

        fun mix(a: Int, b: Int, t: Float) = ColorUtils.blendARGB(a, b, t)

        // Watch: a pure black dial with white marks and hands (dark), or pure white with black
        // ones (light), and a red second hand. Fixed colours on purpose: this one is a classic
        // watch, not a themed dial.
        if (config.look == AnalogLook.WATCH) {
            val dark = isDark(context, config, appSettings)
            val face = card(dark)
            val ink = if (dark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            val red = 0xFFE5342B.toInt()
            return FaceRenderer.Colours(
                face = face,
                onFace = ink,
                minor = mix(ink, face, 0.45f),
                outline = mix(ink, face, 0.80f),
                accent = red,
                hourHand = ink,
                minuteHand = ink,
                secondHand = red,
                pin = red,
            )
        }

        // Day: a plain dial, an outlined hour hand and a small rounded badge with the date.
        if (config.look == AnalogLook.DAY) {
            return FaceRenderer.Colours(
                face = classic.face,
                onFace = classic.onFace,
                minor = classic.minor,
                outline = classic.outline,
                accent = scheme.primary.toArgb(),
                hourHand = scheme.primary.toArgb(),
                minuteHand = scheme.onSurface.toArgb(),
                secondHand = scheme.onSurface.toArgb(),
                pin = scheme.primary.toArgb(),
                // A livelier step of the container colour, not the pale container itself.
                badge = mix(scheme.primaryContainer.toArgb(), scheme.primary.toArgb(), 0.40f),
                onBadge = scheme.onPrimaryContainer.toArgb(),
            )
        }

        // Pebble: one hue. The hour hand is the full colour, the minute hand a softer step of it,
        // and the hairline second hand is the plain text colour.
        if (config.look == AnalogLook.PEBBLE) {
            return FaceRenderer.Colours(
                face = classic.face,
                onFace = classic.onFace,
                minor = classic.minor,
                outline = classic.outline,
                accent = scheme.primary.toArgb(),
                hourHand = scheme.primary.toArgb(),
                minuteHand = mix(scheme.primary.toArgb(), classic.face, 0.30f),
                secondHand = scheme.onSurface.toArgb(),
                pin = scheme.primary.toArgb(),
            )
        }

        // The finished designs (Ripple, Bold, Digit, Orbit) share one colour story: a dark dial,
        // the main colour for the minute hand, a softer step of it for the hour hand, and the
        // tertiary colour as the one accent. Text and marks that should sit *behind* the hands are
        // drawn in a tone close to the dial's own.
        val face = classic.face
        val primary = scheme.primary.toArgb()
        // Bold and Digit draw their big numerals in the same tone, a step darker than the dial's
        // own container colour, so both read clearly behind the hands. (Bold used the bare
        // container colour, which is almost the colour of the dial and nearly vanished.)
        val tonal = mix(scheme.primaryContainer.toArgb(), primary, 0.45f)
        val tonalText = config.look == AnalogLook.BOLD || config.look == AnalogLook.DIGIT
        return FaceRenderer.Colours(
            face = face,
            onFace = if (tonalText) tonal else scheme.onSurface.toArgb(),
            minor = if (config.look == AnalogLook.DIGIT) tonal else mix(primary, face, 0.55f),
            outline = classic.outline,
            accent = primary,
            hourHand = mix(primary, face, 0.40f),
            minuteHand = primary,
            secondHand = scheme.tertiary.toArgb(),
            pin = primary,
            orbitTop = primary,
            orbitOnTop = scheme.onPrimary.toArgb(),
            orbitBottom = scheme.secondary.toArgb(),
            orbitOnBottom = scheme.onSecondary.toArgb(),
            orbitRight = scheme.tertiary.toArgb(),
            orbitOnRight = scheme.onTertiary.toArgb(),
            orbitTrack = scheme.surfaceContainerHighest.toArgb(),
        )
    }

    /** The Material colour scheme a widget should draw with (app theme, wallpaper or palette). */
    /** Whether this widget is dark: its own choice first, then the app's, then the phone's. */
    fun isDark(context: Context, config: WidgetConfig, appSettings: ClockSettings): Boolean =
        when (config.appearance) {
            // This widget's own choice wins over the app and over the phone.
            WidgetAppearance.LIGHT -> false
            WidgetAppearance.DARK -> true
            WidgetAppearance.AUTO -> when {
                config.colour == ColourSource.FOLLOW_APP -> when (appSettings.theme.darkMode) {
                    DarkMode.SYSTEM -> systemDark(context)
                    DarkMode.LIGHT -> false
                    DarkMode.DARK -> true
                }
                else -> systemDark(context)
            }
        }

    /** The plain card behind a widget: #FFFFFF in light mode, #000000 in dark mode. */
    fun card(dark: Boolean): Int = if (dark) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()

    /** [card] for a widget's own light/dark setting. */
    fun card(context: Context, config: WidgetConfig, appSettings: ClockSettings): Int =
        card(isDark(context, config, appSettings))

    fun scheme(context: Context, config: WidgetConfig, appSettings: ClockSettings): ColorScheme {
        val dark = isDark(context, config, appSettings)

        val base: ColorScheme = when {
            config.colour == ColourSource.FOLLOW_APP -> {
                val t = appSettings.theme
                if (t.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamic(context, dark)
                } else if (dark) t.palette.dark else t.palette.light
            }
            config.colour == ColourSource.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamic(context, dark)
            // Below API 31 "wallpaper colours" has nothing to read, so it falls back to the picked
            // palette rather than to grey.
            else -> if (dark) config.palette.dark else config.palette.light
        }

        val amoled = config.colour == ColourSource.FOLLOW_APP && appSettings.theme.amoledBlack
        val scheme = if (dark && amoled) base.toAmoledBlack() else base
        return scheme
    }

    /** [color] with its own alpha scaled by [opacity], 0 (gone) to 1 (as it is). */
    fun withOpacity(color: Int, opacity: Float): Int {
        val o = opacity.coerceIn(0f, 1f)
        if (o >= 1f) return color
        return ColorUtils.setAlphaComponent(color, (android.graphics.Color.alpha(color) * o).roundToInt())
    }

    private fun dynamic(context: Context, dark: Boolean): ColorScheme =
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    /**
     * The host's night mode, not the app's.
     *
     * A widget is drawn into the launcher's window, so it has to follow the *system's* mode even
     * when the app itself is pinned to light or dark. Otherwise a dark home screen carries a white
     * clock.
     */
    private fun systemDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
}