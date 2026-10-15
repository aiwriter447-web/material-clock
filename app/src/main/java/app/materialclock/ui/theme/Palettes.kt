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
    // ARTIST: High contrast. Pure Cyan and Magenta accents. Distinct from Sunset.
    ARTIST(
        displayName = "Artist",
        light = lightColorScheme(
            primary = Color(0xFFE60073),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9EB), // Light Magenta Card
            onPrimaryContainer = Color(0xFF33001A),
            secondary = Color(0xFF008C99),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCCFAFF), // Light Cyan Card
            onSecondaryContainer = Color(0xFF003338),
            tertiary = Color(0xFFFF007F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF007F), // Bright Magenta FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF121212),
            surface = Color(0xFFFAFAFA),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFEBEBEB), // Light default card
            onSurfaceVariant = Color(0xFF121212),
            surfaceTint = Color(0xFFE60073),
            inverseSurface = Color(0xFF242424),
            inverseOnSurface = Color(0xFFFAFAFA),
            inversePrimary = Color(0xFFFF80C2),
            outline = Color(0xFF757575),
            outlineVariant = Color(0xFFC2C2C2),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFF80C2),
            onPrimary = Color(0xFF4D0026),
            primaryContainer = Color(0xFFFFD9EB), // Identical to Light Mode
            onPrimaryContainer = Color(0xFF33001A), // Identical to Light Mode
            secondary = Color(0xFF4DF9FF),
            onSecondary = Color(0xFF003338),
            secondaryContainer = Color(0xFFCCFAFF), // Identical to Light Mode
            onSecondaryContainer = Color(0xFF003338), // Identical to Light Mode
            tertiary = Color(0xFFFF80C2),
            onTertiary = Color(0xFF4D0026),
            tertiaryContainer = Color(0xFFFF007F), // Identical FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF121212), // Restful Dark
            onBackground = Color(0xFFEBEBEB),
            surface = Color(0xFF121212),
            onSurface = Color(0xFFEBEBEB),
            surfaceVariant = Color(0xFF242424), // Restful Dark default card
            onSurfaceVariant = Color(0xFFEBEBEB),
            surfaceTint = Color(0xFFFF80C2),
            inverseSurface = Color(0xFFEBEBEB),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFFE60073),
            outline = Color(0xFFA3A3A3),
            outlineVariant = Color(0xFF424242),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // EARTH: Olive greens, light tan, and warm terracotta.
    EARTH(
        displayName = "Earth",
        light = lightColorScheme(
            primary = Color(0xFF45523D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1DBC2),
            onPrimaryContainer = Color(0xFF161F0F),
            secondary = Color(0xFF5A6652),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE3CDA4),
            onSecondaryContainer = Color(0xFF2E200B),
            tertiary = Color(0xFFB85F47),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC26E57),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF9F9F4),
            onBackground = Color(0xFF171A15),
            surface = Color(0xFFF9F9F4),
            onSurface = Color(0xFF171A15),
            surfaceVariant = Color(0xFFEAECE2),
            onSurfaceVariant = Color(0xFF2E332B),
            surfaceTint = Color(0xFF45523D),
            inverseSurface = Color(0xFF2B2E28),
            inverseOnSurface = Color(0xFFF0F2EB),
            inversePrimary = Color(0xFFB5C4A3),
            outline = Color(0xFF757A6F),
            outlineVariant = Color(0xFFC5C9BF),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB5C4A3),
            onPrimary = Color(0xFF23311A),
            primaryContainer = Color(0xFFD1DBC2), // Identical
            onPrimaryContainer = Color(0xFF161F0F), // Identical
            secondary = Color(0xFFC5D3B8),
            onSecondary = Color(0xFF273420),
            secondaryContainer = Color(0xFFE3CDA4), // Identical
            onSecondaryContainer = Color(0xFF2E200B), // Identical
            tertiary = Color(0xFFE2A493),
            onTertiary = Color(0xFF4C2213),
            tertiaryContainer = Color(0xFFC26E57), // Identical FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF151714), // Soft dark olive-black
            onBackground = Color(0xFFE2E6DD),
            surface = Color(0xFF151714),
            onSurface = Color(0xFFE2E6DD),
            surfaceVariant = Color(0xFF282C25),
            onSurfaceVariant = Color(0xFFE2E6DD),
            surfaceTint = Color(0xFFB5C4A3),
            inverseSurface = Color(0xFFE0E3D9),
            inverseOnSurface = Color(0xFF171A15),
            inversePrimary = Color(0xFF45523D),
            outline = Color(0xFF8F9489),
            outlineVariant = Color(0xFF44493F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // EXPRESSIVE: Energetic violet, punchy lime green, and electric cyan.
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF7210FF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEEDBFF),
            onPrimaryContainer = Color(0xFF25005E),
            secondary = Color(0xFF5C8000),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD7FF7A),
            onSecondaryContainer = Color(0xFF182600),
            tertiary = Color(0xFF00667A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF00E0FF),
            onTertiaryContainer = Color(0xFF00333D),
            background = Color(0xFFFDFBFF),
            onBackground = Color(0xFF1A1B20),
            surface = Color(0xFFFDFBFF),
            onSurface = Color(0xFF1A1B20),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            surfaceTint = Color(0xFF7210FF),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFD0BCFF),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF3B0090),
            primaryContainer = Color(0xFFEEDBFF), // Identical
            onPrimaryContainer = Color(0xFF25005E), // Identical
            secondary = Color(0xFFBFFF2E),
            onSecondary = Color(0xFF2F4500),
            secondaryContainer = Color(0xFFD7FF7A), // Identical
            onSecondaryContainer = Color(0xFF182600), // Identical
            tertiary = Color(0xFF5CEBFF),
            onTertiary = Color(0xFF003642),
            tertiaryContainer = Color(0xFF00E0FF), // Identical FAB
            onTertiaryContainer = Color(0xFF00333D),
            background = Color(0xFF16151A), // Soft dark
            onBackground = Color(0xFFE6E0E9),
            surface = Color(0xFF16151A),
            onSurface = Color(0xFFE6E0E9),
            surfaceVariant = Color(0xFF2D2A33),
            onSurfaceVariant = Color(0xFFE6E0E9),
            surfaceTint = Color(0xFFD0BCFF),
            inverseSurface = Color(0xFFE6E0E9),
            inverseOnSurface = Color(0xFF313033),
            inversePrimary = Color(0xFF7210FF),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF49454F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // GALAXY: Deep space midnight blue, purple, and starlight gold.
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF49367A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD0C4E8),
            onPrimaryContainer = Color(0xFF1B0745),
            secondary = Color(0xFF3D2D66),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFA4C8FF),
            onSecondaryContainer = Color(0xFF001D45),
            tertiary = Color(0xFF997A00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFC83D),
            onTertiaryContainer = Color(0xFF261D00),
            background = Color(0xFFF8F6FC),
            onBackground = Color(0xFF1A181F),
            surface = Color(0xFFF8F6FC),
            onSurface = Color(0xFF1A181F),
            surfaceVariant = Color(0xFFE9E5F2),
            onSurfaceVariant = Color(0xFF231E33),
            surfaceTint = Color(0xFF49367A),
            inverseSurface = Color(0xFF2F2C33),
            inverseOnSurface = Color(0xFFF2EFF7),
            inversePrimary = Color(0xFFCBB3FF),
            outline = Color(0xFF787285),
            outlineVariant = Color(0xFFC9C4D4),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCBB3FF),
            onPrimary = Color(0xFF281156),
            primaryContainer = Color(0xFFD0C4E8), // Identical
            onPrimaryContainer = Color(0xFF1B0745), // Identical
            secondary = Color(0xFFC6B5E8),
            onSecondary = Color(0xFF291751),
            secondaryContainer = Color(0xFFA4C8FF), // Identical
            onSecondaryContainer = Color(0xFF001D45), // Identical
            tertiary = Color(0xFFFFD159),
            onTertiary = Color(0xFF403200),
            tertiaryContainer = Color(0xFFFFC83D), // Identical FAB
            onTertiaryContainer = Color(0xFF261D00),
            background = Color(0xFF0D0B14), // Restful midnight
            onBackground = Color(0xFFE6E2F0),
            surface = Color(0xFF0D0B14),
            onSurface = Color(0xFFE6E2F0),
            surfaceVariant = Color(0xFF231F2E),
            onSurfaceVariant = Color(0xFFE6E2F0),
            surfaceTint = Color(0xFFCBB3FF),
            inverseSurface = Color(0xFFE4E1E8),
            inverseOnSurface = Color(0xFF1A181F),
            inversePrimary = Color(0xFF49367A),
            outline = Color(0xFF938D9E),
            outlineVariant = Color(0xFF484252),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // MOON: Pure white full moon contrast against deep midnight slate.
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF334155),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E8F0),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = Color(0xFF475569),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCBD5E1),
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = Color(0xFF64748B),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFFFFF),
            onTertiaryContainer = Color(0xFF020617),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFF8FAFC),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF1E293B),
            surfaceTint = Color(0xFF334155),
            inverseSurface = Color(0xFF1E293B),
            inverseOnSurface = Color(0xFFF1F5F9),
            inversePrimary = Color(0xFFCBD5E1),
            outline = Color(0xFF94A3B8),
            outlineVariant = Color(0xFFE2E8F0),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCBD5E1),
            onPrimary = Color(0xFF0F172A),
            primaryContainer = Color(0xFFE2E8F0), // Identical
            onPrimaryContainer = Color(0xFF0F172A), // Identical
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFFCBD5E1), // Identical
            onSecondaryContainer = Color(0xFF0F172A), // Identical
            tertiary = Color(0xFFE2E8F0),
            onTertiary = Color(0xFF0F172A),
            tertiaryContainer = Color(0xFFFFFFFF), // Identical FAB
            onTertiaryContainer = Color(0xFF000000),
            background = Color(0xFF0B101A), // Soft midnight blue
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF0B101A),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1A2333),
            onSurfaceVariant = Color(0xFFF1F5F9),
            surfaceTint = Color(0xFFCBD5E1),
            inverseSurface = Color(0xFFE2E8F0),
            inverseOnSurface = Color(0xFF0F172A),
            inversePrimary = Color(0xFF334155),
            outline = Color(0xFF64748B),
            outlineVariant = Color(0xFF334155),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // NOTHING: Pure monochrome with strong red.
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFF121212),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0E0),
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0xFF262626),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEAEAEA),
            onSecondaryContainer = Color(0xFF000000),
            tertiary = Color(0xFFFF0000),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF0000),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF000000),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF000000),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF000000),
            surfaceTint = Color(0xFF121212),
            inverseSurface = Color(0xFF121212),
            inverseOnSurface = Color(0xFFFFFFFF),
            inversePrimary = Color(0xFFD4D4D4),
            outline = Color(0xFF757575),
            outlineVariant = Color(0xFFE0E0E0),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color(0xFF121212),
            primaryContainer = Color(0xFFE0E0E0), // Identical
            onPrimaryContainer = Color(0xFF000000), // Identical
            secondary = Color(0xFFD4D4D4),
            onSecondary = Color(0xFF121212),
            secondaryContainer = Color(0xFFEAEAEA), // Identical
            onSecondaryContainer = Color(0xFF000000), // Identical
            tertiary = Color(0xFFFF4D4D),
            onTertiary = Color(0xFF330000),
            tertiaryContainer = Color(0xFFFF0000), // Identical FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFFFFFFFF),
            surfaceTint = Color(0xFFE0E0E0),
            inverseSurface = Color(0xFFE8E8E8),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFF333333),
            outline = Color(0xFF999999),
            outlineVariant = Color(0xFF333333),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // OCEAN: Deep navy, aqua, seafoam, and striking coral.
    OCEAN(
        displayName = "Ocean",
        light = lightColorScheme(
            primary = Color(0xFF005A8C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFBDE6E6),
            onPrimaryContainer = Color(0xFF001F3F),
            secondary = Color(0xFF006666),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF99CCFF), // Light Water Blue
            onSecondaryContainer = Color(0xFF002244),
            tertiary = Color(0xFFD64A2F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF7F50),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF2FAFA),
            onBackground = Color(0xFF0F1A1A),
            surface = Color(0xFFF2FAFA),
            onSurface = Color(0xFF0F1A1A),
            surfaceVariant = Color(0xFFE0F2F2),
            onSurfaceVariant = Color(0xFF0F1A1A),
            surfaceTint = Color(0xFF005A8C),
            inverseSurface = Color(0xFF1E2E2E),
            inverseOnSurface = Color(0xFFEBF5F5),
            inversePrimary = Color(0xFF75C2F5),
            outline = Color(0xFF708C8C),
            outlineVariant = Color(0xFFC2D6D6),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF75C2F5),
            onPrimary = Color(0xFF00304D),
            primaryContainer = Color(0xFFBDE6E6), // Identical
            onPrimaryContainer = Color(0xFF001F3F), // Identical
            secondary = Color(0xFF80D6D6),
            onSecondary = Color(0xFF003333),
            secondaryContainer = Color(0xFF99CCFF), // Identical
            onSecondaryContainer = Color(0xFF002244), // Identical
            tertiary = Color(0xFFFFB4A1),
            onTertiary = Color(0xFF5E1602),
            tertiaryContainer = Color(0xFFFF7F50), // Identical FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF09141A), // Soft dark navy
            onBackground = Color(0xFFDFEBEB),
            surface = Color(0xFF09141A),
            onSurface = Color(0xFFDFEBEB),
            surfaceVariant = Color(0xFF1A262E),
            onSurfaceVariant = Color(0xFFDFEBEB),
            surfaceTint = Color(0xFF75C2F5),
            inverseSurface = Color(0xFFDFEBEB),
            inverseOnSurface = Color(0xFF0F1A1A),
            inversePrimary = Color(0xFF005A8C),
            outline = Color(0xFF8AA6A6),
            outlineVariant = Color(0xFF3B5454),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // PIXEL: True soft Peach and Light Pink tones. 
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFF8C4A2E),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDBCF), // Soft Peach Card
            onPrimaryContainer = Color(0xFF381504),
            secondary = Color(0xFF785648),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFE4E1), // Light Pink/Misty Rose Card
            onSecondaryContainer = Color(0xFF4A1515),
            tertiary = Color(0xFFD96B43),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFB599), // Vibrant Peach FAB
            onTertiaryContainer = Color(0xFF3D1605),
            background = Color(0xFFFFF8F6),
            onBackground = Color(0xFF241D1A),
            surface = Color(0xFFFFF8F6),
            onSurface = Color(0xFF241D1A),
            surfaceVariant = Color(0xFFF7EAE6), // Light default card
            onSurfaceVariant = Color(0xFF382E2B),
            surfaceTint = Color(0xFF8C4A2E),
            inverseSurface = Color(0xFF382E2B),
            inverseOnSurface = Color(0xFFFFEDE8),
            inversePrimary = Color(0xFFFFB599),
            outline = Color(0xFF85736C),
            outlineVariant = Color(0xFFD8C2BB),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB599),
            onPrimary = Color(0xFF542007),
            primaryContainer = Color(0xFFFFDBCF), // Identical Soft Peach
            onPrimaryContainer = Color(0xFF381504), // Identical
            secondary = Color(0xFFE6BEAD),
            onSecondary = Color(0xFF44291E),
            secondaryContainer = Color(0xFFFFE4E1), // Identical Light Pink
            onSecondaryContainer = Color(0xFF4A1515), // Identical
            tertiary = Color(0xFFFFB499),
            onTertiary = Color(0xFF4A1A0A),
            tertiaryContainer = Color(0xFFFFB599), // Identical FAB
            onTertiaryContainer = Color(0xFF3D1605), // Identical
            background = Color(0xFF1C1817), // Restful warm dark grey
            onBackground = Color(0xFFEDDFDA),
            surface = Color(0xFF1C1817),
            onSurface = Color(0xFFEDDFDA),
            surfaceVariant = Color(0xFF2E2624), // Restful warm default card
            onSurfaceVariant = Color(0xFFF5EBE8),
            surfaceTint = Color(0xFFFFB599),
            inverseSurface = Color(0xFFE8D6D0),
            inverseOnSurface = Color(0xFF241D1A),
            inversePrimary = Color(0xFF8C4A2E),
            outline = Color(0xFFA08C85),
            outlineVariant = Color(0xFF52443F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // SUNSET: Real sunset vibes - Orange, Warm Rose, and Gold.
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFF8C265C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDAB9), // Peach Puff / Light Orange
            onPrimaryContainer = Color(0xFF4A1C00),
            secondary = Color(0xFFB3395B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFC0CB), // Warm Pink
            onSecondaryContainer = Color(0xFF4A0018),
            tertiary = Color(0xFFD65C00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF8C00), // Dark Orange FAB
            onTertiaryContainer = Color(0xFF2E1200),
            background = Color(0xFFFFF5F2),
            onBackground = Color(0xFF21151A),
            surface = Color(0xFFFFF5F2),
            onSurface = Color(0xFF21151A),
            surfaceVariant = Color(0xFFFCE0D9),
            onSurfaceVariant = Color(0xFF21151A),
            surfaceTint = Color(0xFF8C265C),
            inverseSurface = Color(0xFF362A2E),
            inverseOnSurface = Color(0xFFFDECEF),
            inversePrimary = Color(0xFFFFB0D0),
            outline = Color(0xFF857378),
            outlineVariant = Color(0xFFD6C2C6),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB0D0),
            onPrimary = Color(0xFF560034),
            primaryContainer = Color(0xFFFFDAB9), // Identical
            onPrimaryContainer = Color(0xFF4A1C00), // Identical
            secondary = Color(0xFFFFB1C1),
            onSecondary = Color(0xFF65002C),
            secondaryContainer = Color(0xFFFFC0CB), // Identical
            onSecondaryContainer = Color(0xFF4A0018), // Identical
            tertiary = Color(0xFFFFB685),
            onTertiary = Color(0xFF4A1E00),
            tertiaryContainer = Color(0xFFFF8C00), // Identical FAB
            onTertiaryContainer = Color(0xFF2E1200),
            background = Color(0xFF1C1316), // Restful dark plum/black
            onBackground = Color(0xFFEFDFE2),
            surface = Color(0xFF1C1316),
            onSurface = Color(0xFFEFDFE2),
            surfaceVariant = Color(0xFF2D1E24), // Restful dark card
            onSurfaceVariant = Color(0xFFEFDFE2),
            surfaceTint = Color(0xFFFFB0D0),
            inverseSurface = Color(0xFFE5D6DA),
            inverseOnSurface = Color(0xFF21151A),
            inversePrimary = Color(0xFF8C265C),
            outline = Color(0xFFA08C91),
            outlineVariant = Color(0xFF524348),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    );
}
