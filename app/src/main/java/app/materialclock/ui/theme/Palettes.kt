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
    CREATIVE(
        displayName = "Creative",
        light = lightColorScheme(
            primary = Color(0xFFB83252), // Rose Pink
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9DF),
            onPrimaryContainer = Color(0xFF3D0010),
            secondary = Color(0xFF58651A), // Olive
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE4EDAA),
            onSecondaryContainer = Color(0xFF191F00),
            tertiary = Color(0xFF006C70), // Deep Teal
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF9CF1F0),
            onTertiaryContainer = Color(0xFF002020),
            background = Color(0xFFFFF8F0),
            onBackground = Color(0xFF211B1B),
            surface = Color(0xFFFFF8F0),
            onSurface = Color(0xFF211B1B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB1C0),
            onPrimary = Color(0xFF650020),
            primaryContainer = Color(0xFF8F1737),
            onPrimaryContainer = Color(0xFFFFD9DF),
            secondary = Color(0xFFC8D18F),
            onSecondary = Color(0xFF2C3400),
            secondaryContainer = Color(0xFF414B00),
            onSecondaryContainer = Color(0xFFE4EDAA),
            tertiary = Color(0xFF80D5D4),
            onTertiary = Color(0xFF003738),
            tertiaryContainer = Color(0xFF004F52),
            onTertiaryContainer = Color(0xFF9CF1F0),
            background = Color(0xFF191516),
            onBackground = Color(0xFFF0E4E0),
            surface = Color(0xFF191516),
            onSurface = Color(0xFFF0E4E0)
        )
    ),

    // 100% UNIQUE: High-contrast Cyberpunk/Neon vibe. Totally different from all others.
    EXPRESSIVE(
        displayName = "Express",
        light = lightColorScheme(
            primary = Color(0xFFAEEA00), // Neon Lime
            onPrimary = Color(0xFF1A2600),
            primaryContainer = Color(0xFFE4FF54),
            onPrimaryContainer = Color(0xFF2D4000),
            secondary = Color(0xFFFF007F), // Hot Magenta
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E6),
            onSecondaryContainer = Color(0xFF3E001B),
            tertiary = Color(0xFF00E5FF), // Electric Cyan
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFFB3FBFF),
            onTertiaryContainer = Color(0xFF00262A),
            background = Color(0xFF121212), // Dark bg even in light mode for neon pop
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFFFFFFF)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFC6FF00), 
            onPrimary = Color(0xFF243300),
            primaryContainer = Color(0xFF425E00),
            onPrimaryContainer = Color(0xFFE4FF54),
            secondary = Color(0xFFFF3399),
            onSecondary = Color(0xFF5E002A),
            secondaryContainer = Color(0xFF8A0042),
            onSecondaryContainer = Color(0xFFFFD9E6),
            tertiary = Color(0xFF18FFFF),
            onTertiary = Color(0xFF00363D),
            tertiaryContainer = Color(0xFF005560),
            onTertiaryContainer = Color(0xFFB3FBFF),
            background = Color(0xFF000000), // Pitch Black
            onBackground = Color(0xFFE0E0E0),
            surface = Color(0xFF0A0A0A),
            onSurface = Color(0xFFE0E0E0)
        )
    ),

    // 100% UNIQUE: Deep Space Navy and Star Gold (Moved away from Violet/Purple)
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF1A237E), // Deep Space Navy
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDDE1FF),
            onPrimaryContainer = Color(0xFF000B45),
            secondary = Color(0xFFFBC02D), // Star Gold
            onSecondary = Color(0xFF3E2D00),
            secondaryContainer = Color(0xFFFFF0C4),
            onSecondaryContainer = Color(0xFF231900),
            tertiary = Color(0xFFEC407A), // Nebula Pink
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFD9E3),
            onTertiaryContainer = Color(0xFF3E001D),
            background = Color(0xFFF8F9FF),
            onBackground = Color(0xFF1A1B22),
            surface = Color(0xFFF8F9FF),
            onSurface = Color(0xFF1A1B22)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB8C4FF),
            onPrimary = Color(0xFF00176D),
            primaryContainer = Color(0xFF00259A),
            onPrimaryContainer = Color(0xFFDDE1FF),
            secondary = Color(0xFFFFEA60),
            onSecondary = Color(0xFF3C2C00),
            secondaryContainer = Color(0xFF564100),
            onSecondaryContainer = Color(0xFFFFE082),
            tertiary = Color(0xFFFFB1C8),
            onTertiary = Color(0xFF5E1133),
            tertiaryContainer = Color(0xFF7D294B),
            onTertiaryContainer = Color(0xFFFFD9E3),
            background = Color(0xFF080A12), // Deep Void Black
            onBackground = Color(0xFFE3E2E6),
            surface = Color(0xFF0E1019),
            onSurface = Color(0xFFE3E2E6)
        )
    ),

    // 100% UNIQUE: Frosty Pale Ice & Silver (No strong blue, purely frozen look)
    GLACIER(
        displayName = "Glacier",
        light = lightColorScheme(
            primary = Color(0xFF4DD0E1), // Light Frost Blue
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFFB2EBF2),
            onPrimaryContainer = Color(0xFF001F24),
            secondary = Color(0xFF90A4AE), // Silver Gray
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCFD8DC),
            onSecondaryContainer = Color(0xFF1C272D),
            tertiary = Color(0xFF0288D1), // Deep Ice Crevice
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFB3E5FC),
            onTertiaryContainer = Color(0xFF001D33),
            background = Color(0xFFFAFDFF),
            onBackground = Color(0xFF191C1D),
            surface = Color(0xFFFAFDFF),
            onSurface = Color(0xFF191C1D)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF81D4FA),
            onPrimary = Color(0xFF003541),
            primaryContainer = Color(0xFF004E5F),
            onPrimaryContainer = Color(0xFFB2EBF2),
            secondary = Color(0xFFB0BEC5),
            onSecondary = Color(0xFF1A2A31),
            secondaryContainer = Color(0xFF31434B),
            onSecondaryContainer = Color(0xFFCFD8DC),
            tertiary = Color(0xFF4FC3F7),
            onTertiary = Color(0xFF00344F),
            tertiaryContainer = Color(0xFF004B71),
            onTertiaryContainer = Color(0xFFB3E5FC),
            background = Color(0xFF0E1415),
            onBackground = Color(0xFFE1E3E4),
            surface = Color(0xFF131A1C),
            onSurface = Color(0xFFE1E3E4)
        )
    ),

    INVISIBLE(
        displayName = "Invisible",
        light = lightColorScheme(
            primary = Color(0x26000000),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0x12000000),
            onPrimaryContainer = Color(0xFF000000),
            background = Color(0xCCFFFFFF),
            onBackground = Color(0xFF000000),
            surface = Color(0xD9FFFFFF),
            onSurface = Color(0xFF000000)
        ),
        dark = darkColorScheme(
            primary = Color(0x33FFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0x1AFFFFFF),
            onPrimaryContainer = Color(0xFFFFFFFF),
            background = Color(0xCC000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0x991A1A1A),
            onSurface = Color(0xFFFFFFFF)
        )
    ),

    // 100% UNIQUE: Classic Google Standard (Pure Blue/Red/Green)
    INDUS(
        displayName = "Indus",
        light = lightColorScheme(
            primary = Color(0xFF1A73E8), // Classic Blue
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD3E3FD),
            onPrimaryContainer = Color(0xFF041E49),
            secondary = Color(0xFFEA4335), // Classic Red
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E4),
            onSecondaryContainer = Color(0xFF3E001D),
            tertiary = Color(0xFF34A853), // Classic Green
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC4EED0),
            onTertiaryContainer = Color(0xFF00210B),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1F1F1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1F1F1F)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA8C7FA),
            onPrimary = Color(0xFF062E6F),
            primaryContainer = Color(0xFF0842A0),
            onPrimaryContainer = Color(0xFFD3E3FD),
            secondary = Color(0xFFFFB2C6),
            onSecondary = Color(0xFF5D1137),
            secondaryContainer = Color(0xFF7D2A4F),
            onSecondaryContainer = Color(0xFFFFD9E4),
            tertiary = Color(0xFF6DD58C),
            onTertiary = Color(0xFF00391A),
            tertiaryContainer = Color(0xFF005227),
            onTertiaryContainer = Color(0xFFC4EED0),
            background = Color(0xFF121212),
            onBackground = Color(0xFFE3E3E3),
            surface = Color(0xFF1E1F22),
            onSurface = Color(0xFFE3E3E3)
        )
    ),

    // 100% UNIQUE: Tropical Water (Pure Teal/Seafoam & Warm Coral) - Moved away from Blue
    LIQUID(
        displayName = "Liquid",
        light = lightColorScheme(
            primary = Color(0xFF00897B), // Deep Teal
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF80CBC4),
            onPrimaryContainer = Color(0xFF00201A),
            secondary = Color(0xFFFF7043), // Vibrant Coral
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFCCBC),
            onSecondaryContainer = Color(0xFF3E1200),
            tertiary = Color(0xFF00BFA5), // Aqua
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFB2DFDB),
            onTertiaryContainer = Color(0xFF002821),
            background = Color(0xFFF2FBFA),
            onBackground = Color(0xFF191C1B),
            surface = Color(0xFFF2FBFA),
            onSurface = Color(0xFF191C1B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF4DB6AC),
            onPrimary = Color(0xFF003730),
            primaryContainer = Color(0xFF005047),
            onPrimaryContainer = Color(0xFF80CBC4),
            secondary = Color(0xFFFF8A65),
            onSecondary = Color(0xFF4E1F0C),
            secondaryContainer = Color(0xFF702D12),
            onSecondaryContainer = Color(0xFFFFCCBC),
            tertiary = Color(0xFF64FFDA),
            onTertiary = Color(0xFF00382E),
            tertiaryContainer = Color(0xFF005144),
            onTertiaryContainer = Color(0xFFB2DFDB),
            background = Color(0xFF0D1413),
            onBackground = Color(0xFFDFE4E2),
            surface = Color(0xFF151D1C),
            onSurface = Color(0xFFDFE4E2)
        )
    ),

    // 100% UNIQUE: Slate & Muted Blue-Gray
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF59677A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDCE5F1),
            onPrimaryContainer = Color(0xFF172333),
            secondary = Color(0xFF66717F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0E5EC),
            onSecondaryContainer = Color(0xFF1D2733),
            background = Color(0xFFF5F6F8),
            onBackground = Color(0xFF1A1E25),
            surface = Color(0xFFF5F6F8),
            onSurface = Color(0xFF1A1E25)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFBAC9DD),
            onPrimary = Color(0xFF253243),
            primaryContainer = Color(0xFF3D4B5E),
            onPrimaryContainer = Color(0xFFDCE5F1),
            secondary = Color(0xFFC4CDD8),
            onSecondary = Color(0xFF2B3541),
            secondaryContainer = Color(0xFF475260),
            onSecondaryContainer = Color(0xFFE0E5EC),
            background = Color(0xFF0D1118),
            onBackground = Color(0xFFE5EAF1),
            surface = Color(0xFF12171F),
            onSurface = Color(0xFFE5EAF1)
        )
    ),

    // 100% UNIQUE: Deep Forest Green and Earth Brown (Completely distinct from Pixel)
    NATURE(
        displayName = "Nature",
        light = lightColorScheme(
            primary = Color(0xFF33691E), // Forest Green
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDCEDC8),
            onPrimaryContainer = Color(0xFF112700),
            secondary = Color(0xFF6D4C41), // Earth Brown
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD7CCC8),
            onSecondaryContainer = Color(0xFF2E1911),
            background = Color(0xFFF9FAF6),
            onBackground = Color(0xFF1B1C18),
            surface = Color(0xFFF9FAF6),
            onSurface = Color(0xFF1B1C18)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFAED581),
            onPrimary = Color(0xFF204000),
            primaryContainer = Color(0xFF365E00),
            onPrimaryContainer = Color(0xFFDCEDC8),
            secondary = Color(0xFFA1887F),
            onSecondary = Color(0xFF3E2319),
            secondaryContainer = Color(0xFF56382D),
            onSecondaryContainer = Color(0xFFD7CCC8),
            background = Color(0xFF12140F),
            onBackground = Color(0xFFE4E3DB),
            surface = Color(0xFF181B15),
            onSurface = Color(0xFFE4E3DB)
        )
    ),

    // 100% UNIQUE: True Red and Pitch Black/White
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFFD7193F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDDE2),
            onPrimaryContainer = Color(0xFF41000D),
            secondary = Color(0xFF5E5E5E),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8E8E8),
            onSecondaryContainer = Color(0xFF1B1B1B),
            background = Color(0xFFF8F8F8),
            onBackground = Color(0xFF171717),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF171717)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFF6B81),
            onPrimary = Color(0xFF570014),
            primaryContainer = Color(0xFF920D2C),
            onPrimaryContainer = Color(0xFFFFDDE2),
            secondary = Color(0xFFC4C4C4),
            onSecondary = Color(0xFF292929),
            secondaryContainer = Color(0xFF454545),
            onSecondaryContainer = Color(0xFFE8E8E8),
            background = Color(0xFF000000), // Pitch Black
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF090909),
            onSurface = Color(0xFFFFFFFF)
        )
    ),

    // 100% UNIQUE: Vibrant Mint & Watermelon Pink
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFF00BFA5), // Pure Mint
            onPrimary = Color(0xFF00382E),
            primaryContainer = Color(0xFF80FDE1),
            onPrimaryContainer = Color(0xFF002019),
            secondary = Color(0xFFFF5252), // Watermelon
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9D4),
            onSecondaryContainer = Color(0xFF410002),
            background = Color(0xFFF4FBF9),
            onBackground = Color(0xFF161D1A),
            surface = Color(0xFFF4FBF9),
            onSurface = Color(0xFF161D1A)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF1DE9B6),
            onPrimary = Color(0xFF00382B),
            primaryContainer = Color(0xFF005141),
            onPrimaryContainer = Color(0xFF80FDE1),
            secondary = Color(0xFFFF8A80),
            onSecondary = Color(0xFF690005),
            secondaryContainer = Color(0xFF93000A),
            onSecondaryContainer = Color(0xFFFFD9D4),
            background = Color(0xFF0E1513),
            onBackground = Color(0xFFDFE4E1),
            surface = Color(0xFF141C1A),
            onSurface = Color(0xFFDFE4E1)
        )
    ),

    // 100% UNIQUE: True Multi-Color Rainbow (Yellow/Red/Green) - Moved away from Indigo
    RAINBOW(
        displayName = "Rainbow",
        light = lightColorScheme(
            primary = Color(0xFFFFC107), // Sunflower Yellow
            onPrimary = Color(0xFF402D00),
            primaryContainer = Color(0xFFFFE082),
            onPrimaryContainer = Color(0xFF261900),
            secondary = Color(0xFFE53935), // Ruby Red
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFCDD2),
            onSecondaryContainer = Color(0xFF4A0008),
            tertiary = Color(0xFF43A047), // Grass Green
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC8E6C9),
            onTertiaryContainer = Color(0xFF002204),
            background = Color(0xFFFFFDF7),
            onBackground = Color(0xFF1E1B16),
            surface = Color(0xFFFFFDF7),
            onSurface = Color(0xFF1E1B16)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFD54F),
            onPrimary = Color(0xFF3E2D00),
            primaryContainer = Color(0xFF5D4400),
            onPrimaryContainer = Color(0xFFFFE082),
            secondary = Color(0xFFEF5350),
            onSecondary = Color(0xFF5D000A),
            secondaryContainer = Color(0xFF850014),
            onSecondaryContainer = Color(0xFFFFCDD2),
            tertiary = Color(0xFF81C784),
            onTertiary = Color(0xFF00390A),
            tertiaryContainer = Color(0xFF005313),
            onTertiaryContainer = Color(0xFFC8E6C9),
            background = Color(0xFF16130E),
            onBackground = Color(0xFFEBE5DF),
            surface = Color(0xFF1D1914),
            onSurface = Color(0xFFEBE5DF)
        )
    ),

    // 100% UNIQUE: Pure Warm Orange & Peach
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFFF7A59),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDBD1),
            onPrimaryContainer = Color(0xFF3A0B00),
            secondary = Color(0xFFD94F70),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E2),
            onSecondaryContainer = Color(0xFF3E001D),
            background = Color(0xFFFFF8F5),
            onBackground = Color(0xFF2E1500),
            surface = Color(0xFFFFF8F5),
            onSurface = Color(0xFF2E1500)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFF9E80),
            onPrimary = Color(0xFF4A1806),
            primaryContainer = Color(0xFF752109),
            onPrimaryContainer = Color(0xFFFFDBD1),
            secondary = Color(0xFFF48FB1),
            onSecondary = Color(0xFF58112B),
            secondaryContainer = Color(0xFF83324C),
            onSecondaryContainer = Color(0xFFFFD9E2),
            background = Color(0xFF201311),
            onBackground = Color(0xFFFFEEE6),
            surface = Color(0xFF281816),
            onSurface = Color(0xFFFFEEE6)
        )
    ),

    // 100% UNIQUE: Monochromatic True Violet/Amethyst
    VIOLET(
        displayName = "Violet",
        light = lightColorScheme(
            primary = Color(0xFF8E24AA), // Pure Amethyst
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFF8D8FF),
            onPrimaryContainer = Color(0xFF320048),
            secondary = Color(0xFFAB47BC), // Lighter Violet
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF2DAF7),
            onSecondaryContainer = Color(0xFF25142B),
            background = Color(0xFFFFF7FD),
            onBackground = Color(0xFF1E1A20),
            surface = Color(0xFFFFF7FD),
            onSurface = Color(0xFF1E1A20)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFEAACEF),
            onPrimary = Color(0xFF54006E),
            primaryContainer = Color(0xFF710094),
            onPrimaryContainer = Color(0xFFF8D8FF),
            secondary = Color(0xFFCE93D8),
            onSecondary = Color(0xFF3B2A41),
            secondaryContainer = Color(0xFF524058),
            onSecondaryContainer = Color(0xFFF2DAF7),
            background = Color(0xFF161217),
            onBackground = Color(0xFFEADFE7),
            surface = Color(0xFF1D171E),
            onSurface = Color(0xFFEADFE7)
        )
    );
}
