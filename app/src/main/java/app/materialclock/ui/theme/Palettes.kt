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
    // ARTIST: Soft periwinkle/lavender with sunny yellow/amber cards & dark charcoal accents
    ARTIST(
        displayName = "Artist",
        light = lightColorScheme(
            primary = Color(0xFF4A4E69),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFC6C9FF),
            onPrimaryContainer = Color(0xFF191A2E),
            secondary = Color(0xFFFFB74D),
            onSecondary = Color(0xFF3E2723),
            secondaryContainer = Color(0xFFFFE0B2),
            onSecondaryContainer = Color(0xFFE65100),
            tertiary = Color(0xFF2D2E40),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF2D2E40),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF3F4FF),
            onBackground = Color(0xFF191A2E),
            surface = Color(0xFFF3F4FF),
            onSurface = Color(0xFF191A2E),
            surfaceVariant = Color(0xFFE2E4FF),
            onSurfaceVariant = Color(0xFF43455B),
            outline = Color(0xFF75768F)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFC6C9FF),
            onPrimary = Color(0xFF1D2038),
            primaryContainer = Color(0xFF2D304B),
            onPrimaryContainer = Color(0xFFE2E4FF),
            secondary = Color(0xFFFFB74D),
            onSecondary = Color(0xFF3E2723),
            secondaryContainer = Color(0xFF4A3200),
            onSecondaryContainer = Color(0xFFFFE0B2),
            tertiary = Color(0xFFFFB74D),
            onTertiary = Color(0xFF201A00),
            tertiaryContainer = Color(0xFFFFB74D),
            onTertiaryContainer = Color(0xFF201A00),
            background = Color(0xFF121320),
            onBackground = Color(0xFFE2E3F5),
            surface = Color(0xFF121320),
            onSurface = Color(0xFFE2E3F5),
            surfaceVariant = Color(0xFF232538),
            onSurfaceVariant = Color(0xFFC6C7DC),
            outline = Color(0xFF8F90A6)
        )
    ),

    // EARTH: Forest green active elements with sage moss tones
    EARTH(
        displayName = "Earth",
        light = lightColorScheme(
            primary = Color(0xFF2D5A27),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD4E8CE),
            onPrimaryContainer = Color(0xFF092106),
            secondary = Color(0xFF4A6343),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE3E8DF),
            onSecondaryContainer = Color(0xFF182015),
            tertiary = Color(0xFF2D5A27),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF2D5A27),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF2F5F0),
            onBackground = Color(0xFF181C17),
            surface = Color(0xFFF2F5F0),
            onSurface = Color(0xFF181C17),
            surfaceVariant = Color(0xFFE0E5DC),
            onSurfaceVariant = Color(0xFF424940),
            outline = Color(0xFF72796F)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF98D48E),
            onPrimary = Color(0xFF003908),
            primaryContainer = Color(0xFF164312),
            onPrimaryContainer = Color(0xFFD4E8CE),
            secondary = Color(0xFFB7CCB0),
            onSecondary = Color(0xFF23351E),
            secondaryContainer = Color(0xFF1E261E),
            onSecondaryContainer = Color(0xFFD4E8CE),
            tertiary = Color(0xFF98D48E),
            onTertiary = Color(0xFF003908),
            tertiaryContainer = Color(0xFF2D5A27),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF111410),
            onBackground = Color(0xFFE1E4DC),
            surface = Color(0xFF111410),
            onSurface = Color(0xFFE1E4DC),
            surfaceVariant = Color(0xFF222821),
            onSurfaceVariant = Color(0xFFC2C9BD),
            outline = Color(0xFF8C9388)
        )
    ),

    // EXPRESSIVE: Soft lavender with Lime Green contrast cards
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF7E22CE),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE9D5FF),
            onPrimaryContainer = Color(0xFF3B0764),
            secondary = Color(0xFF65A30D),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD9F99D),
            onSecondaryContainer = Color(0xFF1A3000),
            tertiary = Color(0xFF7E22CE),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD9F99D),
            onTertiaryContainer = Color(0xFF1A3000),
            background = Color(0xFFF8F5FF),
            onBackground = Color(0xFF1E1A24),
            surface = Color(0xFFF8F5FF),
            onSurface = Color(0xFF1E1A24),
            surfaceVariant = Color(0xFFEADDF7),
            onSurfaceVariant = Color(0xFF4A3F56),
            outline = Color(0xFF7C6F89)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD8B4FE),
            onPrimary = Color(0xFF4C0585),
            primaryContainer = Color(0xFF320E54),
            onPrimaryContainer = Color(0xFFE9D5FF),
            secondary = Color(0xFFA3E635),
            onSecondary = Color(0xFF223A00),
            secondaryContainer = Color(0xFF263D02),
            onSecondaryContainer = Color(0xFFD9F99D),
            tertiary = Color(0xFFA3E635),
            onTertiary = Color(0xFF1A3000),
            tertiaryContainer = Color(0xFFA3E635),
            onTertiaryContainer = Color(0xFF122200),
            background = Color(0xFF120B1A),
            onBackground = Color(0xFFEDE4F5),
            surface = Color(0xFF120B1A),
            onSurface = Color(0xFFEDE4F5),
            surfaceVariant = Color(0xFF271C33),
            onSurfaceVariant = Color(0xFFCEBFDC),
            outline = Color(0xFF9788A5)
        )
    ),

    // GALAXY: Cosmic deep indigo/purple space dark mode with violet hands
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF5B4EB3),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE4E0FF),
            onPrimaryContainer = Color(0xFF16035A),
            secondary = Color(0xFF7C6EE6),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEDE9FF),
            onSecondaryContainer = Color(0xFF1B0B63),
            tertiary = Color(0xFF7C6EE6),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF7C6EE6),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF6F5FC),
            onBackground = Color(0xFF1B1A24),
            surface = Color(0xFFF6F5FC),
            onSurface = Color(0xFF1B1A24),
            surfaceVariant = Color(0xFFE4E1F2),
            onSurfaceVariant = Color(0xFF464455),
            outline = Color(0xFF777587)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFC5C0FF),
            onPrimary = Color(0xFF2A1B82),
            primaryContainer = Color(0xFF231D45),
            onPrimaryContainer = Color(0xFFE4E0FF),
            secondary = Color(0xFF8B7CF6),
            onSecondary = Color(0xFF190061),
            secondaryContainer = Color(0xFF1C1833),
            onSecondaryContainer = Color(0xFFEDE9FF),
            tertiary = Color(0xFF8B7CF6),
            onTertiary = Color(0xFF190061),
            tertiaryContainer = Color(0xFF7C6EE6),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0A0814),
            onBackground = Color(0xFFE5E2F2),
            surface = Color(0xFF0A0814),
            onSurface = Color(0xFFE5E2F2),
            surfaceVariant = Color(0xFF1D1A2B),
            onSurfaceVariant = Color(0xFFC8C4D8),
            outline = Color(0xFF918EA2)
        )
    ),

    // INDIGO: Minimal Royal Blue active elements with crisp contrast (Renamed from former Pixel concept)
    INDIGO(
        displayName = "Indigo",
        light = lightColorScheme(
            primary = Color(0xFF1A5CFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDBE4FF),
            onPrimaryContainer = Color(0xFF00174D),
            secondary = Color(0xFF3F5AA6),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEFF2FF),
            onSecondaryContainer = Color(0xFF001A40),
            tertiary = Color(0xFF1A5CFF),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1A5CFF),
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
            primary = Color(0xFFB4C5FF),
            onPrimary = Color(0xFF002A78),
            primaryContainer = Color(0xFF003EA8),
            onPrimaryContainer = Color(0xFFDBE4FF),
            secondary = Color(0xFFABC7FF),
            onSecondary = Color(0xFF002E69),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFFDBE4FF),
            tertiary = Color(0xFF60A5FA),
            onTertiary = Color(0xFF00326B),
            tertiaryContainer = Color(0xFF1A5CFF),
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

    // MOON: Luminous frosted glass look with deep night blue & sapphire glowing accents
    MOON(
        displayName = "Moon",
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
            tertiaryContainer = Color(0xFF2563EB), // Sapphire Blue FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF0F4F8),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFF0F4F8),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF334155),
            outline = Color(0xFF64748B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color(0xFF002244),
            primaryContainer = Color(0xFF1E293B),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF3B82F6),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF0F172A),
            onSecondaryContainer = Color(0xFFBFDBFE),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF00325B),
            tertiaryContainer = Color(0xFF2563EB), // Glowing Blue FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0B0F19), // Midnight space background
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF0B0F19),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF151D2A),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF475569)
        )
    ),

    // NOTHING: Dot Matrix inspired pure black background with stark bright red accents
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFF121212),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0E0),
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0xFF262626),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF0F0F0),
            onSecondaryContainer = Color(0xFF000000),
            tertiary = Color(0xFFE53935),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE53935),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF121212),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF404040),
            outline = Color(0xFF757575)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFFE53935),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFFE53935),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF212121),
            onSecondaryContainer = Color(0xFFFFFFFF),
            tertiary = Color(0xFFE53935),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE53935),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = Color(0xFFCCCCCC),
            outline = Color(0xFF666666)
        )
    ),

    // OCEAN: Sky Blue active elements on crisp ice-blue background
    OCEAN(
        displayName = "Ocean",
        light = lightColorScheme(
            primary = Color(0xFF1D8DF1),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1E8FF),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF006399),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF001E30),
            tertiary = Color(0xFF1D8DF1),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1D8DF1),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF4F8FA),
            onBackground = Color(0xFF181C1F),
            surface = Color(0xFFF4F8FA),
            onSurface = Color(0xFF181C1F),
            surfaceVariant = Color(0xFFDEE3EA),
            onSurfaceVariant = Color(0xFF41474D),
            outline = Color(0xFF72787E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9ECAFF),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF00497D),
            onPrimaryContainer = Color(0xFFD1E8FF),
            secondary = Color(0xFF38BDF8),
            onSecondary = Color(0xFF00354B),
            secondaryContainer = Color(0xFF132337),
            onSecondaryContainer = Color(0xFFE0F2FE),
            tertiary = Color(0xFF38BDF8),
            onTertiary = Color(0xFF00354B),
            tertiaryContainer = Color(0xFF1D8DF1),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0A121D),
            onBackground = Color(0xFFE0E2E6),
            surface = Color(0xFF0A121D),
            onSurface = Color(0xFFE0E2E6),
            surfaceVariant = Color(0xFF222B35),
            onSurfaceVariant = Color(0xFFC1C7CE),
            outline = Color(0xFF8B9198)
        )
    ),

    // PIXEL: Soft Rose / Blush Pink aesthetic with elegant light tone
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFFE91E63),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFCE4EC),
            onPrimaryContainer = Color(0xFF880E4F),
            secondary = Color(0xFFF48FB1),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFF0F5),
            onSecondaryContainer = Color(0xFF5C0029),
            tertiary = Color(0xFFE91E63),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF06292), // Rose Pink FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFF7F9),
            onBackground = Color(0xFF211A1D),
            surface = Color(0xFFFFF7F9),
            onSurface = Color(0xFF211A1D),
            surfaceVariant = Color(0xFFF8E2E8),
            onSurfaceVariant = Color(0xFF524347),
            outline = Color(0xFF857377)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF48FB1),
            onPrimary = Color(0xFF5C002B),
            primaryContainer = Color(0xFF3B1123),
            onPrimaryContainer = Color(0xFFFCE4EC),
            secondary = Color(0xFFFF80AB),
            onSecondary = Color(0xFF4A001F),
            secondaryContainer = Color(0xFF2A121B),
            onSecondaryContainer = Color(0xFFFCE4EC),
            tertiary = Color(0xFFFF80AB),
            onTertiary = Color(0xFF4A001F),
            tertiaryContainer = Color(0xFFEC407A), // Glowing Rose FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF170E12),
            onBackground = Color(0xFFECE0E3),
            surface = Color(0xFF170E12),
            onSurface = Color(0xFFECE0E3),
            surfaceVariant = Color(0xFF2B1C22),
            onSurfaceVariant = Color(0xFFD5C2C7),
            outline = Color(0xFF9E8C91)
        )
    ),

    // SUNSET: Energetic Sunset Amber/Orange active accents on warm dark charcoal
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFE65100),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDCC8),
            onPrimaryContainer = Color(0xFF341100),
            secondary = Color(0xFFFF6B00),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFF0E6),
            onSecondaryContainer = Color(0xFF3B1200),
            tertiary = Color(0xFFFF6B00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF6B00),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF5F2EC),
            onBackground = Color(0xFF1F1B18),
            surface = Color(0xFFF5F2EC),
            onSurface = Color(0xFF1F1B18),
            surfaceVariant = Color(0xFFEBE0D8),
            onSurfaceVariant = Color(0xFF4F453E),
            outline = Color(0xFF81756D)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB58C),
            onPrimary = Color(0xFF5B1A00),
            primaryContainer = Color(0xFF812800),
            onPrimaryContainer = Color(0xFFFFDCC8),
            secondary = Color(0xFFFFB58C),
            onSecondary = Color(0xFF532200),
            secondaryContainer = Color(0xFF2B2929),
            onSecondaryContainer = Color(0xFFFFDCC8),
            tertiary = Color(0xFFFF8A50),
            onTertiary = Color(0xFF4A1800),
            tertiaryContainer = Color(0xFFFF6B00),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF1F1F1F),
            onBackground = Color(0xFFEAE1DB),
            surface = Color(0xFF1F1F1F),
            onSurface = Color(0xFFEAE1DB),
            surfaceVariant = Color(0xFF2B2929),
            onSurfaceVariant = Color(0xFFD3C4BC),
            outline = Color(0xFF9C8E87)
        )
    );
}
