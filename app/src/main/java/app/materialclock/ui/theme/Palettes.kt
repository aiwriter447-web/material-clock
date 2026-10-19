package app.materialclock.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class Palette(
    val displayName: String,
    val light: ColorScheme,
    val dark: ColorScheme
) {
    // ARTIST: Soft lavender background, warm yellow cards, dark navy FAB
    ARTIST(
        displayName = "Artist",
        light = lightColorScheme(
            primary = Color(0xFF7C83FD),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E3FF),
            onPrimaryContainer = Color(0xFF101452),
            secondary = Color(0xFFFFB74D),
            onSecondary = Color(0xFF4A3200),
            secondaryContainer = Color(0xFFFFDF9E),
            onSecondaryContainer = Color(0xFF261900),
            tertiary = Color(0xFF2C2D43),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF2C2D43),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF4F5FB),
            onBackground = Color(0xFF1A1A24),
            surface = Color(0xFFF4F5FB),
            onSurface = Color(0xFF1A1A24),
            surfaceVariant = Color(0xFFE3E4EF),
            onSurfaceVariant = Color(0xFF464653),
            outline = Color(0xFF767684)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA5ACFF),
            onPrimary = Color(0xFF101452),
            primaryContainer = Color(0xFF2F3485),
            onPrimaryContainer = Color(0xFFE0E3FF),
            secondary = Color(0xFFFFD485),
            onSecondary = Color(0xFF4A3200),
            secondaryContainer = Color(0xFF5C3F00),
            onSecondaryContainer = Color(0xFFFFDF9E),
            tertiary = Color(0xFFE0E1F5),
            onTertiary = Color(0xFF2C2D43),
            tertiaryContainer = Color(0xFF424465),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF16161E),
            onBackground = Color(0xFFE4E1E6),
            surface = Color(0xFF16161E),
            onSurface = Color(0xFFE4E1E6),
            surfaceVariant = Color(0xFF262630),
            onSurfaceVariant = Color(0xFFC7C5D0),
            outline = Color(0xFF90909A)
        )
    ),

    // EARTH: Forest green active elements with sage moss tones
    EARTH(
        displayName = "Earth",
        light = lightColorScheme(
            primary = Color(0xFF386A34),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD8F2D1),
            onPrimaryContainer = Color(0xFF032104),
            secondary = Color(0xFF52634E),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD5E8CE),
            onSecondaryContainer = Color(0xFF101F0E),
            tertiary = Color(0xFF386A34),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF386A34),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF7FBF2),
            onBackground = Color(0xFF181D17),
            surface = Color(0xFFF7FBF2),
            onSurface = Color(0xFF181D17),
            surfaceVariant = Color(0xFFDFE4D8),
            onSurfaceVariant = Color(0xFF43483F),
            outline = Color(0xFF73796E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9CD695),
            onPrimary = Color(0xFF06390A),
            primaryContainer = Color(0xFF20511E),
            onPrimaryContainer = Color(0xFFD8F2D1),
            secondary = Color(0xFFB9CCB3),
            onSecondary = Color(0xFF253422),
            secondaryContainer = Color(0xFF202A1C),
            onSecondaryContainer = Color(0xFFD5E8CE),
            tertiary = Color(0xFF9CD695),
            onTertiary = Color(0xFF06390A),
            tertiaryContainer = Color(0xFF386A34),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF10140F),
            onBackground = Color(0xFFE0E4DB),
            surface = Color(0xFF10140F),
            onSurface = Color(0xFFE0E4DB),
            surfaceVariant = Color(0xFF21251E),
            onSurfaceVariant = Color(0xFFC3C8BC),
            outline = Color(0xFF8D9387)
        )
    ),

    // EXPRESSIVE: Deep Plum and vibrant Coral/Salmon Pink aesthetic
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF612EA3),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF24005A),
            secondary = Color(0xFF7A4A9E),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF5E8FF),
            onSecondaryContainer = Color(0xFF2A0054),
            tertiary = Color(0xFFFF7A85),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF7A85),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F4FA),
            onBackground = Color(0xFF1C1B1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD6BFFF),
            onPrimary = Color(0xFF3A008A),
            primaryContainer = Color(0xFFFF7A85),
            onPrimaryContainer = Color(0xFF3A0012),
            secondary = Color(0xFFC9A5EB),
            onSecondary = Color(0xFF371759),
            secondaryContainer = Color(0xFF4E2C73),
            onSecondaryContainer = Color(0xFFF5E8FF),
            tertiary = Color(0xFFFFB3B8),
            onTertiary = Color(0xFF5C001C),
            tertiaryContainer = Color(0xFFFF7A85),
            onTertiaryContainer = Color(0xFF3A0012),
            background = Color(0xFF160B21),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF160B21),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF29183C),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99)
        )
    ),

    // GALAXIA: Cosmic deep space purple with vibrant violet accents
    GALAXIA(
        displayName = "Galaxia",
        light = lightColorScheme(
            primary = Color(0xFF673AB7),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            secondary = Color(0xFF625B71),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),
            tertiary = Color(0xFF7D5260),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF673AB7),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFDFBFF),
            onBackground = Color(0xFF1C1B1F),
            surface = Color(0xFFFDFBFF),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF23144A),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF1E1928),
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = Color(0xFFEFB8C8),
            onTertiary = Color(0xFF492532),
            tertiaryContainer = Color(0xFF8A59FF),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0F0B17),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF0F0B17),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF1C1824),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99)
        )
    ),

    // INDIGO: Crisp White background with vibrant Royal Blue buttons
    INDIGO(
        displayName = "Indigo",
        light = lightColorScheme(
            primary = Color(0xFF2563EB),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDBEAFE),
            onPrimaryContainer = Color(0xFF1E3A8A),
            secondary = Color(0xFF3B82F6),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEFF6FF),
            onSecondaryContainer = Color(0xFF1E40AF),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1D4ED8),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFF8FAFC),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFF94A3B8)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color(0xFF002855),
            primaryContainer = Color(0xFF1E3A8A),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF93C5FD),
            onSecondary = Color(0xFF00326B),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFFDBEAFE),
            tertiary = Color(0xFF60A5FA),
            onTertiary = Color(0xFF002855),
            tertiaryContainer = Color(0xFF3B82F6),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF0F172A),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF64748B)
        )
    ),

    // MOON: Stark High-Contrast Lunar Grayscale (Pitch Black, Pure White, Silver)
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF212121),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0E0),
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0xFF616161),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF5F5F5),
            onSecondaryContainer = Color(0xFF212121),
            tertiary = Color(0xFF000000),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF000000),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF121212),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFEEEEEE),
            onSurfaceVariant = Color(0xFF424242),
            outline = Color(0xFF9E9E9E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF212121),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFF9E9E9E),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF121212),
            onSecondaryContainer = Color(0xFFE0E0E0),
            tertiary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFFFFFFFF),
            onTertiaryContainer = Color(0xFF000000),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFFBDBDBD),
            outline = Color(0xFF757575)
        )
    ),

    // NUCLEUS: Pure Black, Pure White, Bright Graphic Red
    NUCLEUS(
    displayName = "Nucleus",

    light = lightColorScheme(
        primary = Color(0xFFD32F2F),
        onPrimary = Color(0xFFFFFFFF),

        primaryContainer = Color(0xFFFFEBEE),
        onPrimaryContainer = Color(0xFF410002),

        secondary = Color(0xFF121212),
        onSecondary = Color(0xFFFFFFFF),

        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color(0xFF000000),

        tertiary = Color(0xFFD32F2F),
        onTertiary = Color(0xFFFFFFFF),

        tertiaryContainer = Color(0xFFFFCDD2),
        onTertiaryContainer = Color(0xFF410002),

        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF121212),

        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF121212),

        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF404040),

        outline = Color(0xFF757575)
    ),

    dark = darkColorScheme(
        // Soft Premium Red
        primary = Color(0xFFFF7777),
        onPrimary = Color(0xFF3D0000),

        // Muted Red Container
        primaryContainer = Color(0xFF5C2025),
        onPrimaryContainer = Color(0xFFFFDADA),

        // Soft Neutral Secondary
        secondary = Color(0xFFE0B4B4),
        onSecondary = Color(0xFF352020),

        secondaryContainer = Color(0xFF302426),
        onSecondaryContainer = Color(0xFFFFDADA),

        // Elegant Coral Red
        tertiary = Color(0xFFFF8A80),
        onTertiary = Color(0xFF4A1010),

        tertiaryContainer = Color(0xFF632B27),
        onTertiaryContainer = Color(0xFFFFDAD6),

        // Pure AMOLED Black
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),

        surface = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),

        // Subtle Dark Surfaces
        surfaceVariant = Color(0xFF1C1C1E),
        onSurfaceVariant = Color(0xFFD0C4C4),

        // Balanced Borders
        outline = Color(0xFF756868)
      )
   ),

  // OCEAN: Scuba diving aesthetic: Deepest dark teal/black with glowing Cyan/Aqua accents
    OCEAN(
        displayName = "Ocean",
        light = lightColorScheme(
            primary = Color(0xFF00838F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB2EBF2),
            onPrimaryContainer = Color(0xFF00363A),
            secondary = Color(0xFF006064),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0F7FA),
            onSecondaryContainer = Color(0xFF002022),
            tertiary = Color(0xFF00838F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF00838F),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFE0F7FA),
            onBackground = Color(0xFF041014),
            surface = Color(0xFFE0F7FA),
            onSurface = Color(0xFF041014),
            surfaceVariant = Color(0xFFB2EBF2),
            onSurfaceVariant = Color(0xFF3F484A),
            outline = Color(0xFF6F797A)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF00E5FF),
            onPrimary = Color(0xFF00363A),
            primaryContainer = Color(0xFF004D40),
            onPrimaryContainer = Color(0xFFE0F7FA),
            secondary = Color(0xFF26C6DA),
            onSecondary = Color(0xFF00363A),
            secondaryContainer = Color(0xFF092529),
            onSecondaryContainer = Color(0xFFB2EBF2),
            tertiary = Color(0xFF00E5FF),
            onTertiary = Color(0xFF00363A),
            tertiaryContainer = Color(0xFF00E5FF),
            onTertiaryContainer = Color(0xFF002B30),
            background = Color(0xFF041014),
            onBackground = Color(0xFFE0F7FA),
            surface = Color(0xFF0A1C20),
            onSurface = Color(0xFFE0F7FA),
            surfaceVariant = Color(0xFF122A30),
            onSurfaceVariant = Color(0xFFBFC8CA),
            outline = Color(0xFF899294)
        )
    ),

    // PRIME: Soft pastel Rose & Pink aesthetic
    PRIME(
        displayName = "Prime",
        light = lightColorScheme(
            primary = Color(0xFFF06292),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFCE4EC),
            onPrimaryContainer = Color(0xFF4B001F),
            secondary = Color(0xFFEC407A),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF8BBD0),
            onSecondaryContainer = Color(0xFF5C0028),
            tertiary = Color(0xFFF06292),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFEC407A),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFF0F5),
            onBackground = Color(0xFF201A1C),
            surface = Color(0xFFFFF0F5),
            onSurface = Color(0xFF201A1C),
            surfaceVariant = Color(0xFFF2DFE4),
            onSurfaceVariant = Color(0xFF514347),
            outline = Color(0xFF837377)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF48FB1),
            onPrimary = Color(0xFF5C0028),
            primaryContainer = Color(0xFF880E4F),
            onPrimaryContainer = Color(0xFFFCE4EC),
            secondary = Color(0xFFFF80AB),
            onSecondary = Color(0xFF4A001F),
            secondaryContainer = Color(0xFF28141C),
            onSecondaryContainer = Color(0xFFFCE4EC),
            tertiary = Color(0xFFFF80AB),
            onTertiary = Color(0xFF4A001F),
            tertiaryContainer = Color(0xFFF06292),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF171214),
            onBackground = Color(0xFFEBE0E2),
            surface = Color(0xFF171214),
            onSurface = Color(0xFFEBE0E2),
            surfaceVariant = Color(0xFF2C1F23),
            onSurfaceVariant = Color(0xFFD6C2C6),
            outline = Color(0xFF9E8C90)
        )
    ),

    // SUNSET: Warm Amber and Terracotta accents on dark charcoal
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFD84315),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFCCBC),
            onPrimaryContainer = Color(0xFF4E1400),
            secondary = Color(0xFFEF6C00),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFE0B2),
            onSecondaryContainer = Color(0xFF4B2000),
            tertiary = Color(0xFFEF6C00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFEF6C00),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F5F3),
            onBackground = Color(0xFF201A18),
            surface = Color(0xFFF8F5F3),
            onSurface = Color(0xFF201A18),
            surfaceVariant = Color(0xFFEEDFD8),
            onSurfaceVariant = Color(0xFF51443E),
            outline = Color(0xFF84746C)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFAB91),
            onPrimary = Color(0xFF6B1800),
            primaryContainer = Color(0xFF9E2A00),
            onPrimaryContainer = Color(0xFFFFCCBC),
            secondary = Color(0xFFFFB74D),
            onSecondary = Color(0xFF4B2000),
            secondaryContainer = Color(0xFF2C2422),
            onSecondaryContainer = Color(0xFFFFCCBC),
            tertiary = Color(0xFFFF9800),
            onTertiary = Color(0xFF4B2000),
            tertiaryContainer = Color(0xFFE64A19),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF181514),
            onBackground = Color(0xFFECE0DA),
            surface = Color(0xFF181514),
            onSurface = Color(0xFFECE0DA),
            surfaceVariant = Color(0xFF2C2422),
            onSurfaceVariant = Color(0xFFD6C3B9),
            outline = Color(0xFF9E8D85)
        )
    ),

    // WOODLAND: Elegant warm cream/beige backgrounds with stark pure Black buttons
    WOODLAND(
        displayName = "Woodland",
        light = lightColorScheme(
            primary = Color(0xFF1C1C1C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADAD7),
            onPrimaryContainer = Color(0xFF1C1C1C),
            secondary = Color(0xFF4A4A4A),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF0E5E3),
            onSecondaryContainer = Color(0xFF1C1C1C),
            tertiary = Color(0xFF1C1C1C),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1C1C1C),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F3F1),
            onBackground = Color(0xFF1C1C1C),
            surface = Color(0xFFF8F3F1),
            onSurface = Color(0xFF1C1C1C),
            surfaceVariant = Color(0xFFE8DFDD),
            onSurfaceVariant = Color(0xFF4A4544),
            outline = Color(0xFF7A7574)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF0EBE9),
            onPrimary = Color(0xFF1C1C1C),
            primaryContainer = Color(0xFF3D3331),
            onPrimaryContainer = Color(0xFFF0EBE9),
            secondary = Color(0xFFB0A8A6),
            onSecondary = Color(0xFF1C1C1C),
            secondaryContainer = Color(0xFF2E2726),
            onSecondaryContainer = Color(0xFFD6CCC9),
            tertiary = Color(0xFFF0EBE9),
            onTertiary = Color(0xFF1C1C1C),
            tertiaryContainer = Color(0xFFF0EBE9),
            onTertiaryContainer = Color(0xFF1C1C1C),
            background = Color(0xFF1A1514),
            onBackground = Color(0xFFE8E1DF),
            surface = Color(0xFF1A1514),
            onSurface = Color(0xFFE8E1DF),
            surfaceVariant = Color(0xFF2E2726),
            onSurfaceVariant = Color(0xFFC4BDBB),
            outline = Color(0xFF918A89)
        )
    );
}
