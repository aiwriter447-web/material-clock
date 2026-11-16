package app.materialclock.ui.theme

import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import kotlin.math.pow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.materialclock.data.DarkMode
import app.materialclock.data.ThemeSettings

/** The short-side width, in dp, that every layout in the app was designed on (1080 px phone). */
private const val UI_REFERENCE_DP = 384f

/** Phones are scaled between these factors: about 326 dp up to about 422 dp short side. */
private const val UI_SCALE_MIN = 0.85f
private const val UI_SCALE_MAX = 1.1f

/**
 * Text shrinks less than the boxes around it on small phones (and grows less on big ones), so
 * it stays readable: 0 would scale text exactly like everything else, 1 would not scale it at all.
 */
private const val TEXT_DAMPING = 0.4f

/**
 * How much the whole UI is scaled so it keeps the same proportions on every phone. 1.0 on the
 * reference phone, 0.85 (the lowest) on a 320 dp phone, about 1.09 on a 419 dp phone.
 */
val LocalUiScale = staticCompositionLocalOf { 1f }

private val LocalUiScaleApplied = staticCompositionLocalOf { false }

@Composable
fun ClockTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val dark = when (settings.darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }

    val context = LocalContext.current
    val base = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> settings.palette.dark
        else -> settings.palette.light
    }
    val amoled = dark && settings.amoledBlack
    val scheme = remember(base, amoled) { if (amoled) base.toAmoledBlack() else base }

    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(dark, activity) {
        val bar = if (dark) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = bar, navigationBarStyle = bar)
    }

    // Proportional scaling for phones: the design is laid out for a 384 dp short side, and on
    // other phone sizes every dp and sp grows or shrinks by the same factor, so the screens look
    // the same instead of getting cramped or loose. The short side is used so that landscape
    // behaves exactly like portrait. Tablets and unfolded folds (600 dp and up) are not scaled.
    val outerDensity = LocalDensity.current
    val screen = LocalConfiguration.current
    val alreadyScaled = LocalUiScaleApplied.current
    val shortSideDp = minOf(screen.screenWidthDp, screen.screenHeightDp)
    val uiScale = when {
        alreadyScaled -> LocalUiScale.current
        shortSideDp < 600 -> (shortSideDp / UI_REFERENCE_DP).coerceIn(UI_SCALE_MIN, UI_SCALE_MAX)
        else -> 1f
    }
    val scaledDensity = if (alreadyScaled) {
        outerDensity
    } else {
        Density(
            density = outerDensity.density * uiScale,
            fontScale = outerDensity.fontScale * uiScale.pow(-TEXT_DAMPING)
        )
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalUiScale provides uiScale,
        LocalUiScaleApplied provides true,
        LocalTonalElevationEnabled provides !amoled,
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            typography = remember { clockTypography() },
            content = content,
        )
    }
}

fun ColorScheme.toAmoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = this.surfaceContainer,
    surfaceContainerHighest = this.surfaceContainerHighest,
    scrim = Color.Black,
)
