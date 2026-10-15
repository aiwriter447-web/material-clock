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
    // ARTIST: Soft pink/magenta base with a vibrant popping magenta FAB.
    ARTIST(
        displayName = "Artist",
        light = lightColorScheme(
            primary = Color(0xFFB30059),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9EB), // Top Pill
            onPrimaryContainer = Color(0xFF33001A),
            secondary = Color(0xFF006670),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCFF5FF), // Work Card
            onSecondaryContainer = Color(0xFF001F22),
            tertiary = Color(0xFFFF007F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF007F), // Identical Vibrant FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFF8FA), // Softest pink-white
            onBackground = Color(0xFF1C1A1A),
            surface = Color(0xFFFFF8FA),
            onSurface = Color(0xFF1C1A1A),
            surfaceVariant = Color(0xFFF3E8EC), // Soft light card
            onSurfaceVariant = Color(0xFF4A4446),
            surfaceTint = Color(0xFFB30059),
            inverseSurface = Color(0xFF312D2E),
            inverseOnSurface = Color(0xFFF5EEF0),
            inversePrimary = Color(0xFFFFB3D6),
            outline = Color(0xFF7C7275),
            outlineVariant = Color(0xFFCCC4C6),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB3D6),
            onPrimary = Color(0xFF660033),
            primaryContainer = Color(0xFF3D1226), // Muted dark card
            onPrimaryContainer = Color(0xFFFFD9EB),
            secondary = Color(0xFF4DF9FF),
            onSecondary = Color(0xFF003338),
            secondaryContainer = Color(0xFF1A3B40), // Muted dark card
            onSecondaryContainer = Color(0xFFCFF5FF),
            tertiary = Color(0xFFFF80C2),
            onTertiary = Color(0xFF4D0026),
            tertiaryContainer = Color(0xFFFF007F), // Identical Vibrant FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF141112), // Restful dark charcoal
            onBackground = Color(0xFFEBE5E7),
            surface = Color(0xFF141112),
            onSurface = Color(0xFFEBE5E7),
            surfaceVariant = Color(0xFF2B2527), // Restful dark card
            onSurfaceVariant = Color(0xFFCCC4C6),
            surfaceTint = Color(0xFFFFB3D6),
            inverseSurface = Color(0xFFEBE5E7),
            inverseOnSurface = Color(0xFF1C1A1A),
            inversePrimary = Color(0xFFB30059),
            outline = Color(0xFF968E91),
            outlineVariant = Color(0xFF4A4446),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // EARTH: Soft beige and olive with a warm terracotta FAB.
    EARTH(
        displayName = "Earth",
        light = lightColorScheme(
            primary = Color(0xFF45523D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDDE5D1), // Soft Top Pill
            onPrimaryContainer = Color(0xFF161F0F),
            secondary = Color(0xFF5A6652),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8DBC5), // Soft Work Card
            onSecondaryContainer = Color(0xFF2E200B),
            tertiary = Color(0xFFB85F47),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC26E57), // Identical Rust FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9F3), // Softest beige-white
            onBackground = Color(0xFF191A18),
            surface = Color(0xFFF8F9F3),
            onSurface = Color(0xFF191A18),
            surfaceVariant = Color(0xFFE8EAE1), // Soft light card
            onSurfaceVariant = Color(0xFF434540),
            surfaceTint = Color(0xFF45523D),
            inverseSurface = Color(0xFF2E2F2C),
            inverseOnSurface = Color(0xFFEFF1E9),
            inversePrimary = Color(0xFFB5C4A3),
            outline = Color(0xFF747670),
            outlineVariant = Color(0xFFC4C6BE),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB5C4A3),
            onPrimary = Color(0xFF23311A),
            primaryContainer = Color(0xFF2A3620), // Muted dark card
            onPrimaryContainer = Color(0xFFDDE5D1),
            secondary = Color(0xFFC5D3B8),
            onSecondary = Color(0xFF273420),
            secondaryContainer = Color(0xFF382B18), // Muted dark card
            onSecondaryContainer = Color(0xFFE8DBC5),
            tertiary = Color(0xFFE2A493),
            onTertiary = Color(0xFF4C2213),
            tertiaryContainer = Color(0xFFC26E57), // Identical Rust FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF131411), // Restful deep olive-grey
            onBackground = Color(0xFFE2E3DD),
            surface = Color(0xFF131411),
            onSurface = Color(0xFFE2E3DD),
            surfaceVariant = Color(0xFF282A25), // Restful dark card
            onSurfaceVariant = Color(0xFFC4C6BE),
            surfaceTint = Color(0xFFB5C4A3),
            inverseSurface = Color(0xFFE2E3DD),
            inverseOnSurface = Color(0xFF191A18),
            inversePrimary = Color(0xFF45523D),
            outline = Color(0xFF8E908A),
            outlineVariant = Color(0xFF434540),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // EXPRESSIVE: Soft lavender base with a popping electric cyan FAB.
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8DEF8), // Soft Top Pill
            onPrimaryContainer = Color(0xFF1D0061),
            secondary = Color(0xFF4A6600),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE1FFB3), // Soft Work Card
            onSecondaryContainer = Color(0xFF141F00),
            tertiary = Color(0xFF00667A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF00E0FF), // Identical Electric Cyan FAB
            onTertiaryContainer = Color(0xFF00333D),
            background = Color(0xFFFAF8FF), // Softest lavender-white
            onBackground = Color(0xFF1A1B1F),
            surface = Color(0xFFFAF8FF),
            onSurface = Color(0xFF1A1B1F),
            surfaceVariant = Color(0xFFEBE5F1), // Soft light card
            onSurfaceVariant = Color(0xFF48454E),
            surfaceTint = Color(0xFF6200EE),
            inverseSurface = Color(0xFF2F3034),
            inverseOnSurface = Color(0xFFF1F0F4),
            inversePrimary = Color(0xFFCCB3FF),
            outline = Color(0xFF79757F),
            outlineVariant = Color(0xFFCAC5D0),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCCB3FF),
            onPrimary = Color(0xFF33008A),
            primaryContainer = Color(0xFF2E223D), // Muted dark card
            onPrimaryContainer = Color(0xFFE8DEF8),
            secondary = Color(0xFFB1F04D),
            onSecondary = Color(0xFF253600),
            secondaryContainer = Color(0xFF2A3B00), // Muted dark card
            onSecondaryContainer = Color(0xFFE1FFB3),
            tertiary = Color(0xFF5CEBFF),
            onTertiary = Color(0xFF003642),
            tertiaryContainer = Color(0xFF00E0FF), // Identical Electric Cyan FAB
            onTertiaryContainer = Color(0xFF00333D),
            background = Color(0xFF141218), // Restful dark midnight violet
            onBackground = Color(0xFFE5E1E6),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE5E1E6),
            surfaceVariant = Color(0xFF2A2831), // Restful dark card
            onSurfaceVariant = Color(0xFFCAC5D0),
            surfaceTint = Color(0xFFCCB3FF),
            inverseSurface = Color(0xFFE5E1E6),
            inverseOnSurface = Color(0xFF1A1B1F),
            inversePrimary = Color(0xFF6200EE),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF48454E),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // GALAXY: Soft blue-lavender base with a glowing gold FAB.
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF49367A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0D8F0), // Soft Top Pill
            onPrimaryContainer = Color(0xFF1B0745),
            secondary = Color(0xFF3D2D66),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD1E0FF), // Soft Work Card
            onSecondaryContainer = Color(0xFF001A40),
            tertiary = Color(0xFF997A00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFC83D), // Identical Gold FAB
            onTertiaryContainer = Color(0xFF261D00),
            background = Color(0xFFF5F4FA), // Softest space-white
            onBackground = Color(0xFF1B1A1F),
            surface = Color(0xFFF5F4FA),
            onSurface = Color(0xFF1B1A1F),
            surfaceVariant = Color(0xFFE6E4EB), // Soft light card
            onSurfaceVariant = Color(0xFF47464B),
            surfaceTint = Color(0xFF49367A),
            inverseSurface = Color(0xFF302F34),
            inverseOnSurface = Color(0xFFF2F0F5),
            inversePrimary = Color(0xFFCBB3FF),
            outline = Color(0xFF78767B),
            outlineVariant = Color(0xFFC8C5CB),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCBB3FF),
            onPrimary = Color(0xFF281156),
            primaryContainer = Color(0xFF241D38), // Muted dark card
            onPrimaryContainer = Color(0xFFE0D8F0),
            secondary = Color(0xFFC6B5E8),
            onSecondary = Color(0xFF291751),
            secondaryContainer = Color(0xFF1A2C4D), // Muted dark card
            onSecondaryContainer = Color(0xFFD1E0FF),
            tertiary = Color(0xFFFFD159),
            onTertiary = Color(0xFF403200),
            tertiaryContainer = Color(0xFFFFC83D), // Identical Gold FAB
            onTertiaryContainer = Color(0xFF261D00),
            background = Color(0xFF0F101A), // Restful deep space
            onBackground = Color(0xFFE5E4E8),
            surface = Color(0xFF0F101A),
            onSurface = Color(0xFFE5E4E8),
            surfaceVariant = Color(0xFF21222E), // Restful dark card
            onSurfaceVariant = Color(0xFFC8C5CB),
            surfaceTint = Color(0xFFCBB3FF),
            inverseSurface = Color(0xFFE5E4E8),
            inverseOnSurface = Color(0xFF1B1A1F),
            inversePrimary = Color(0xFF49367A),
            outline = Color(0xFF918F95),
            outlineVariant = Color(0xFF47464B),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // MOON: Clean slate tones with a luminous silver moon FAB.
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF334155),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E8F0), // Soft Top Pill
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = Color(0xFF475569),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFDBE2E9), // Soft Work Card
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = Color(0xFF64748B),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF94A3B8), // Identical Luminous Silver FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8FAFC), // Softest slate-white
            onBackground = Color(0xFF181A1F),
            surface = Color(0xFFF8FAFC),
            onSurface = Color(0xFF181A1F),
            surfaceVariant = Color(0xFFF1F5F9), // Soft light card
            onSurfaceVariant = Color(0xFF43474E),
            surfaceTint = Color(0xFF334155),
            inverseSurface = Color(0xFF2D3035),
            inverseOnSurface = Color(0xFFEEF0F5),
            inversePrimary = Color(0xFFCBD5E1),
            outline = Color(0xFF73777F),
            outlineVariant = Color(0xFFC3C7CF),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCBD5E1),
            onPrimary = Color(0xFF0F172A),
            primaryContainer = Color(0xFF1E293B), // Muted dark card
            onPrimaryContainer = Color(0xFFE2E8F0),
            secondary = Color(0xFF94A3B8),
            onSecondary = Color(0xFF0F172A),
            secondaryContainer = Color(0xFF23324A), // Muted dark card
            onSecondaryContainer = Color(0xFFDBE2E9),
            tertiary = Color(0xFFE2E8F0),
            onTertiary = Color(0xFF0F172A),
            tertiaryContainer = Color(0xFF94A3B8), // Identical Luminous Silver FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF060912), // Restful midnight slate
            onBackground = Color(0xFFE2E2E5),
            surface = Color(0xFF060912),
            onSurface = Color(0xFFE2E2E5),
            surfaceVariant = Color(0xFF171B24), // Restful dark card
            onSurfaceVariant = Color(0xFFC3C7CF),
            surfaceTint = Color(0xFFCBD5E1),
            inverseSurface = Color(0xFFE2E2E5),
            inverseOnSurface = Color(0xFF181A1F),
            inversePrimary = Color(0xFF334155),
            outline = Color(0xFF8D9199),
            outlineVariant = Color(0xFF43474E),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // NOTHING: Pure monochrome background with the signature pure red FAB.
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFF121212),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0E0), // Soft Top Pill
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0xFF404040),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEAEAEA), // Soft Work Card
            onSecondaryContainer = Color(0xFF000000),
            tertiary = Color(0xFFFF0000),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF0000), // Identical Pure Red FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF), // Pure white
            onBackground = Color(0xFF121212),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFF5F5F5), // Soft light card
            onSurfaceVariant = Color(0xFF404040),
            surfaceTint = Color(0xFF121212),
            inverseSurface = Color(0xFF2E2E2E),
            inverseOnSurface = Color(0xFFF0F0F0),
            inversePrimary = Color(0xFFD4D4D4),
            outline = Color(0xFF757575),
            outlineVariant = Color(0xFFC2C2C2),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color(0xFF121212),
            primaryContainer = Color(0xFF262626), // Muted dark card
            onPrimaryContainer = Color(0xFFE0E0E0),
            secondary = Color(0xFFD4D4D4),
            onSecondary = Color(0xFF121212),
            secondaryContainer = Color(0xFF1A1A1A), // Muted dark card
            onSecondaryContainer = Color(0xFFEAEAEA),
            tertiary = Color(0xFFFF4D4D),
            onTertiary = Color(0xFF330000),
            tertiaryContainer = Color(0xFFFF0000), // Identical Pure Red FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF000000), // Pure black
            onBackground = Color(0xFFE0E0E0),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFE0E0E0),
            surfaceVariant = Color(0xFF121212), // Restful dark card
            onSurfaceVariant = Color(0xFFC2C2C2),
            surfaceTint = Color(0xFFE0E0E0),
            inverseSurface = Color(0xFFE0E0E0),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFF333333),
            outline = Color(0xFF8F8F8F),
            outlineVariant = Color(0xFF404040),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // OCEAN: Soft seafoam base with a striking coral pop FAB.
    OCEAN(
        displayName = "Ocean",
        light = lightColorScheme(
            primary = Color(0xFF005A8C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFCCE5E5), // Soft Top Pill
            onPrimaryContainer = Color(0xFF002222),
            secondary = Color(0xFF006666),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFBDE1E1), // Soft Work Card
            onSecondaryContainer = Color(0xFF001F1F),
            tertiary = Color(0xFFD64A2F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF7F50), // Identical Coral FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF2FAFA), // Softest seafoam
            onBackground = Color(0xFF191C1C),
            surface = Color(0xFFF2FAFA),
            onSurface = Color(0xFF191C1C),
            surfaceVariant = Color(0xFFE0F0F0), // Soft light card
            onSurfaceVariant = Color(0xFF3F4949),
            surfaceTint = Color(0xFF005A8C),
            inverseSurface = Color(0xFF2E3131),
            inverseOnSurface = Color(0xFFEFF1F1),
            inversePrimary = Color(0xFF75C2F5),
            outline = Color(0xFF6F7979),
            outlineVariant = Color(0xFFBEC9C9),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF75C2F5),
            onPrimary = Color(0xFF00304D),
            primaryContainer = Color(0xFF122B2B), // Muted dark card
            onPrimaryContainer = Color(0xFFCCE5E5),
            secondary = Color(0xFF80D6D6),
            onSecondary = Color(0xFF003333),
            secondaryContainer = Color(0xFF173D3D), // Muted dark card
            onSecondaryContainer = Color(0xFFBDE1E1),
            tertiary = Color(0xFFFFB4A1),
            onTertiary = Color(0xFF5E1602),
            tertiaryContainer = Color(0xFFFF7F50), // Identical Coral FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF081313), // Restful oceanic trench
            onBackground = Color(0xFFE0E3E3),
            surface = Color(0xFF081313),
            onSurface = Color(0xFFE0E3E3),
            surfaceVariant = Color(0xFF182626), // Restful dark card
            onSurfaceVariant = Color(0xFFBEC9C9),
            surfaceTint = Color(0xFF75C2F5),
            inverseSurface = Color(0xFFE0E3E3),
            inverseOnSurface = Color(0xFF191C1C),
            inversePrimary = Color(0xFF005A8C),
            outline = Color(0xFF899393),
            outlineVariant = Color(0xFF3F4949),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // PIXEL: True soft peach/pink light mode, muted dark grey dark mode, bright peach FAB.
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFF8C4A2E),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE4DD), // Soft Top Pill
            onPrimaryContainer = Color(0xFF381504),
            secondary = Color(0xFF785648),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD8CC), // Soft Work Card
            onSecondaryContainer = Color(0xFF2C150A),
            tertiary = Color(0xFFD96B43),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFB599), // Identical Peach FAB
            onTertiaryContainer = Color(0xFF3D1605),
            background = Color(0xFFFFF8F6), // Softest peach-white
            onBackground = Color(0xFF231A17),
            surface = Color(0xFFFFF8F6),
            onSurface = Color(0xFF231A17),
            surfaceVariant = Color(0xFFFCEBE6), // Soft light card
            onSurfaceVariant = Color(0xFF53433F),
            surfaceTint = Color(0xFF8C4A2E),
            inverseSurface = Color(0xFF392E2B),
            inverseOnSurface = Color(0xFFFFEDE8),
            inversePrimary = Color(0xFFFFB599),
            outline = Color(0xFF85736E),
            outlineVariant = Color(0xFFD8C2BC),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB599),
            onPrimary = Color(0xFF542007),
            primaryContainer = Color(0xFF382018), // Muted dark card
            onPrimaryContainer = Color(0xFFFFE4DD),
            secondary = Color(0xFFE6BEAD),
            onSecondary = Color(0xFF44291E),
            secondaryContainer = Color(0xFF4A2B1D), // Muted dark card
            onSecondaryContainer = Color(0xFFFFD8CC),
            tertiary = Color(0xFFFFB499),
            onTertiary = Color(0xFF4A1A0A),
            tertiaryContainer = Color(0xFFFFB599), // Identical Peach FAB
            onTertiaryContainer = Color(0xFF3D1605),
            background = Color(0xFF1A1615), // Restful warm grey
            onBackground = Color(0xFFEAE0DD),
            surface = Color(0xFF1A1615),
            onSurface = Color(0xFFEAE0DD),
            surfaceVariant = Color(0xFF2E2624), // Restful dark card
            onSurfaceVariant = Color(0xFFD8C2BC),
            surfaceTint = Color(0xFFFFB599),
            inverseSurface = Color(0xFFEAE0DD),
            inverseOnSurface = Color(0xFF231A17),
            inversePrimary = Color(0xFF8C4A2E),
            outline = Color(0xFFA08C87),
            outlineVariant = Color(0xFF53433F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    ),

    // SUNSET: Very light warm pink light mode, deep plum dark mode, glowing amber FAB.
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFF8C265C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E2), // Soft Top Pill
            onPrimaryContainer = Color(0xFF3B0022),
            secondary = Color(0xFFB3395B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD6CC), // Soft Work Card
            onSecondaryContainer = Color(0xFF3D0018),
            tertiary = Color(0xFFD65C00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFF8C00), // Identical Amber FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFFFF5F5), // Softest sunset pink
            onBackground = Color(0xFF211A1C),
            surface = Color(0xFFFFF5F5),
            onSurface = Color(0xFF211A1C),
            surfaceVariant = Color(0xFFFCE0E3), // Soft light card
            onSurfaceVariant = Color(0xFF534346),
            surfaceTint = Color(0xFF8C265C),
            inverseSurface = Color(0xFF362E30),
            inverseOnSurface = Color(0xFFFDEDED),
            inversePrimary = Color(0xFFFFB0D0),
            outline = Color(0xFF857376),
            outlineVariant = Color(0xFFD8C1C4),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB0D0),
            onPrimary = Color(0xFF560034),
            primaryContainer = Color(0xFF3D1526), // Muted dark card
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = Color(0xFFFFB1C1),
            onSecondary = Color(0xFF65002C),
            secondaryContainer = Color(0xFF4A1E14), // Muted dark card
            onSecondaryContainer = Color(0xFFFFD6CC),
            tertiary = Color(0xFFFFB685),
            onTertiary = Color(0xFF4A1E00),
            tertiaryContainer = Color(0xFFFF8C00), // Identical Amber FAB
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF170D11), // Restful dark plum
            onBackground = Color(0xFFEDE0E2),
            surface = Color(0xFF170D11),
            onSurface = Color(0xFFEDE0E2),
            surfaceVariant = Color(0xFF2B1A20), // Restful dark card
            onSurfaceVariant = Color(0xFFD8C1C4),
            surfaceTint = Color(0xFFFFB0D0),
            inverseSurface = Color(0xFFEDE0E2),
            inverseOnSurface = Color(0xFF211A1C),
            inversePrimary = Color(0xFF8C265C),
            outline = Color(0xFFA08C8F),
            outlineVariant = Color(0xFF534346),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6)
        )
    );
}
