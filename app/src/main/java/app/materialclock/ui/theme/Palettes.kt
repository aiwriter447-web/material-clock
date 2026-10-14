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
    /**
     * 1. CREATIVE
     * Artistic, high-contrast palette with rich Plum primary and warm Amber/Teal container accents for FABs.
     */
    CREATIVE(
        displayName = "Creative",
        light = lightColorScheme(
            primary = Color(0xFF8C1D40),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E1),
            onPrimaryContainer = Color(0xFF3B0014),
            secondary = Color(0xFF705B00),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDF7A),
            onSecondaryContainer = Color(0xFF231B00),
            tertiary = Color(0xFF00696D),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF9CF1F5),
            onTertiaryContainer = Color(0xFF002021),
            background = Color(0xFFFFF8F6),
            onBackground = Color(0xFF22191C),
            surface = Color(0xFFFFF8F6),
            onSurface = Color(0xFF22191C)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB1C4),
            onPrimary = Color(0xFF540023),
            primaryContainer = Color(0xFF700331),
            onPrimaryContainer = Color(0xFFFFD9E1),
            secondary = Color(0xFFE5C348),
            onSecondary = Color(0xFF3B2F00),
            secondaryContainer = Color(0xFF554400),
            onSecondaryContainer = Color(0xFFFFDF7A),
            tertiary = Color(0xFF80D5D9),
            onTertiary = Color(0xFF003638),
            tertiaryContainer = Color(0xFF004F52),
            onTertiaryContainer = Color(0xFF9CF1F5),
            background = Color(0xFF191114),
            onBackground = Color(0xFFF0DFE2),
            surface = Color(0xFF191114),
            onSurface = Color(0xFFF0DFE2)
        )
    ),

    /**
     * 2. EXPRESSIVE
     * Vibrant and dynamic without being eye-straining in dark mode. Warm Coral and Soft Cyan accents.
     */
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF9A2548),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E0),
            onPrimaryContainer = Color(0xFF3F0015),
            secondary = Color(0xFF006874),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF97F0FF),
            onSecondaryContainer = Color(0xFF001F24),
            tertiary = Color(0xFF795900),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFDF9E),
            onTertiaryContainer = Color(0xFF261A00),
            background = Color(0xFFFFF8F7),
            onBackground = Color(0xFF22191C),
            surface = Color(0xFFFFF8F7),
            onSurface = Color(0xFF22191C)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB1C7),
            onPrimary = Color(0xFF5E0027),
            primaryContainer = Color(0xFF7D0A32),
            onPrimaryContainer = Color(0xFFFFD9E0),
            secondary = Color(0xFF4FD8EB),
            onSecondary = Color(0xFF00363D),
            secondaryContainer = Color(0xFF004F58),
            onSecondaryContainer = Color(0xFF97F0FF),
            tertiary = Color(0xFFF0C048),
            onTertiary = Color(0xFF402D00),
            tertiaryContainer = Color(0xFF5C4300),
            onTertiaryContainer = Color(0xFFFFDF9E),
            background = Color(0xFF1A1114),
            onBackground = Color(0xFFF0DFE2),
            surface = Color(0xFF1A1114),
            onSurface = Color(0xFFF0DFE2)
        )
    ),

    /**
     * 3. GALAXY
     * Deep Space Navy, Cosmic Nebula Magenta, and Starlight Gold containers for FABs.
     */
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF283593),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E0FF),
            onPrimaryContainer = Color(0xFF000B47),
            secondary = Color(0xFFAD1457),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E2),
            onSecondaryContainer = Color(0xFF3E001D),
            tertiary = Color(0xFF795500),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFDF9E),
            onTertiaryContainer = Color(0xFF261A00),
            background = Color(0xFFFAF8FF),
            onBackground = Color(0xFF1A1B23),
            surface = Color(0xFFFAF8FF),
            onSurface = Color(0xFF1A1B23)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFBEC2FF),
            onPrimary = Color(0xFF00176C),
            primaryContainer = Color(0xFF0B1D7A),
            onPrimaryContainer = Color(0xFFE0E0FF),
            secondary = Color(0xFFFFB1C9),
            onSecondary = Color(0xFF650033),
            secondaryContainer = Color(0xFF8C0048),
            onSecondaryContainer = Color(0xFFFFD9E2),
            tertiary = Color(0xFFF2BF48),
            onTertiary = Color(0xFF402D00),
            tertiaryContainer = Color(0xFF5C4300),
            onTertiaryContainer = Color(0xFFFFDF9E),
            background = Color(0xFF0B0D19),
            onBackground = Color(0xFFE3E1EC),
            surface = Color(0xFF101221),
            onSurface = Color(0xFFE3E1EC)
        )
    ),

    /**
     * 4. GLACIER
     * Sub-zero Ice Blues, Slate Gray, and Cold Frost accents.
     */
    GLACIER(
        displayName = "Glacier",
        light = lightColorScheme(
            primary = Color(0xFF006678),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFCCE8FD),
            onPrimaryContainer = Color(0xFF001F26),
            secondary = Color(0xFF4A626C),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCDE7F3),
            onSecondaryContainer = Color(0xFF051F27),
            tertiary = Color(0xFF526070),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD6E4F7),
            onTertiaryContainer = Color(0xFF0E1D2A),
            background = Color(0xFFF5FAFD),
            onBackground = Color(0xFF171C1F),
            surface = Color(0xFFF5FAFD),
            onSurface = Color(0xFF171C1F)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF83D2E6),
            onPrimary = Color(0xFF003641),
            primaryContainer = Color(0xFF004D5B),
            onPrimaryContainer = Color(0xFFCCE8FD),
            secondary = Color(0xFFB1CBD6),
            onSecondary = Color(0xFF1C343D),
            secondaryContainer = Color(0xFF334A54),
            onSecondaryContainer = Color(0xFFCDE7F3),
            tertiary = Color(0xFFBAA7DA),
            onTertiary = Color(0xFF1D2B37),
            tertiaryContainer = Color(0xFF3B4857),
            onTertiaryContainer = Color(0xFFD6E4F7),
            background = Color(0xFF0E1417),
            onBackground = Color(0xFFDEE3E6),
            surface = Color(0xFF11171A),
            onSurface = Color(0xFFDEE3E6)
        )
    ),

    /**
     * 5. INVISIBLE
     * Semi-transparent frosted elements designed for translucent overlay UI without losing accessibility.
     */
    INVISIBLE(
        displayName = "Invisible",
        light = lightColorScheme(
            primary = Color(0xBF1F1F1F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0x33000000),
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0x80333333),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0x1F000000),
            onSecondaryContainer = Color(0xFF1F1F1F),
            background = Color(0xCCFFFFFF),
            onBackground = Color(0xFF1F1F1F),
            surface = Color(0xE6FFFFFF),
            onSurface = Color(0xFF1F1F1F)
        ),
        dark = darkColorScheme(
            primary = Color(0xDDF5F5F5),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0x40FFFFFF),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0x99CCCCCC),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0x26FFFFFF),
            onSecondaryContainer = Color(0xFFE3E3E3),
            background = Color(0xCC121212),
            onBackground = Color(0xFFE3E3E3),
            surface = Color(0xB31E1E1E),
            onSurface = Color(0xFFE3E3E3)
        )
    ),

    /**
     * 6. INDUS
     * Indus Valley & Indian Cultural Heritage Palette: Terracotta/Clay, Warm Ochre, and Copper.
     */
    INDUS(
        displayName = "Indus",
        light = lightColorScheme(
            primary = Color(0xFFA04026),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDAD1),
            onPrimaryContainer = Color(0xFF3D0700),
            secondary = Color(0xFF825500),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDDA6),
            onSecondaryContainer = Color(0xFF2A1800),
            tertiary = Color(0xFF4E6528),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFCFECA0),
            onTertiaryContainer = Color(0xFF102000),
            background = Color(0xFFFFF8F6),
            onBackground = Color(0xFF231A18),
            surface = Color(0xFFFFF8F6),
            onSurface = Color(0xFF231A18)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB4A2),
            onPrimary = Color(0xFF5F1502),
            primaryContainer = Color(0xFF802A12),
            onPrimaryContainer = Color(0xFFFFDAD1),
            secondary = Color(0xFFF5BE4F),
            onSecondary = Color(0xFF452B00),
            secondaryContainer = Color(0xFF633F00),
            onSecondaryContainer = Color(0xFFFFDDA6),
            tertiary = Color(0xFFB3CF86),
            onTertiary = Color(0xFF213600),
            tertiaryContainer = Color(0xFF374D13),
            onTertiaryContainer = Color(0xFFCFECA0),
            background = Color(0xFF1A1210),
            onBackground = Color(0xFFF1DFDC),
            surface = Color(0xFF1A1210),
            onSurface = Color(0xFFF1DFDC)
        )
    ),

    /**
     * 7. LIQUID
     * Frosted Glass Teal & Soft Coral Translucency aesthetics.
     */
    LIQUID(
        displayName = "Liquid",
        light = lightColorScheme(
            primary = Color(0xFF006A60),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF74F8E5),
            onPrimaryContainer = Color(0xFF00201C),
            secondary = Color(0xFF9E4311),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDBCF),
            onSecondaryContainer = Color(0xFF380B00),
            tertiary = Color(0xFF00668A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC4E7FF),
            onTertiaryContainer = Color(0xFF001E2D),
            background = Color(0xFFF4FAF8),
            onBackground = Color(0xFF161D1B),
            surface = Color(0xFFF4FAF8),
            onSurface = Color(0xFF161D1B)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF53DBC9),
            onPrimary = Color(0xFF003731),
            primaryContainer = Color(0xFF005048),
            onPrimaryContainer = Color(0xFF74F8E5),
            secondary = Color(0xFFFFB59B),
            onSecondary = Color(0xFF5A1B00),
            secondaryContainer = Color(0xFF7D2D00),
            onSecondaryContainer = Color(0xFFFFDBCF),
            tertiary = Color(0xFF7CD0FF),
            onTertiary = Color(0xFF00344A),
            tertiaryContainer = Color(0xFF004C69),
            onTertiaryContainer = Color(0xFFC4E7FF),
            background = Color(0xFF0E1513),
            onBackground = Color(0xFFDEE4E1),
            surface = Color(0xFF101715),
            onSurface = Color(0xFFDEE4E1)
        )
    ),

    /**
     * 8. MOON
     * Midnight Moonlight: Slate Gray, Soft Silver, Frost Lavender.
     */
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF4A5F78),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD3E3FD),
            onPrimaryContainer = Color(0xFF041C32),
            secondary = Color(0xFF585F70),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFDCE3F9),
            onSecondaryContainer = Color(0xFF151C2B),
            tertiary = Color(0xFF6B5778),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF3DAFF),
            onTertiaryContainer = Color(0xFF251431),
            background = Color(0xFFF8F9FF),
            onBackground = Color(0xFF191C22),
            surface = Color(0xFFF8F9FF),
            onSurface = Color(0xFF191C22)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB1C8E5),
            onPrimary = Color(0xFF1B3148),
            primaryContainer = Color(0xFF33485F),
            onPrimaryContainer = Color(0xFFD3E3FD),
            secondary = Color(0xFFC0C7DC),
            onSecondary = Color(0xFF2A3142),
            secondaryContainer = Color(0xFF404758),
            onSecondaryContainer = Color(0xFFDCE3F9),
            tertiary = Color(0xFFD6BEE4),
            onTertiary = Color(0xFF3B2947),
            tertiaryContainer = Color(0xFF533F5F),
            onTertiaryContainer = Color(0xFFF3DAFF),
            background = Color(0xFF0F141B),
            onBackground = Color(0xFFE1E2E8),
            surface = Color(0xFF121720),
            onSurface = Color(0xFFE1E2E8)
        )
    ),

    /**
     * 9. NATURE
     * Fresh Pine, Deep Earth Moss, and Soft Meadow Greens.
     */
    NATURE(
        displayName = "Nature",
        light = lightColorScheme(
            primary = Color(0xFF2E6A38),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB0F3B3),
            onPrimaryContainer = Color(0xFF002107),
            secondary = Color(0xFF52634F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD5E8CE),
            onSecondaryContainer = Color(0xFF101F10),
            tertiary = Color(0xFF386567),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFBCEBEB),
            onTertiaryContainer = Color(0xFF002021),
            background = Color(0xFFF6FAF3),
            onBackground = Color(0xFF181D18),
            surface = Color(0xFFF6FAF3),
            onSurface = Color(0xFF181D18)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF95D699),
            onPrimary = Color(0xFF003913),
            primaryContainer = Color(0xFF125223),
            onPrimaryContainer = Color(0xFFB0F3B3),
            secondary = Color(0xFFB9CCB3),
            onSecondary = Color(0xFF253423),
            secondaryContainer = Color(0xFF3B4B38),
            onSecondaryContainer = Color(0xFFD5E8CE),
            tertiary = Color(0xFFA0CFD0),
            onTertiary = Color(0xFF003738),
            tertiaryContainer = Color(0xFF1E4D4F),
            onTertiaryContainer = Color(0xFFBCEBEB),
            background = Color(0xFF101511),
            onBackground = Color(0xFFE0E4DE),
            surface = Color(0xFF121813),
            onSurface = Color(0xFFE0E4DE)
        )
    ),

    /**
     * 10. NOTHING
     * Authentic Nothing OS Monochromatic Dot-Matrix Style: Clean Black & White with subtle Red Accent.
     */
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFF171717),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE5E5E5),
            onPrimaryContainer = Color(0xFF000000),
            secondary = Color(0xFFD71921),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDAD8),
            onSecondaryContainer = Color(0xFF410003),
            background = Color(0xFFF8F8F8),
            onBackground = Color(0xFF171717),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF171717)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFEEEEEE),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF303030),
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFFFF5449),
            onSecondary = Color(0xFF690007),
            secondaryContainer = Color(0xFF930010),
            onSecondaryContainer = Color(0xFFFFDAD8),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF121212),
            onSurface = Color(0xFFFFFFFF)
        )
    ),

    /**
     * 11. PIXEL
     * Authentic Google Material You Tonal Style: Clean Blue, Soft Coral, and Pastel Tones.
     */
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFF1A73E8),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD3E3FD),
            onPrimaryContainer = Color(0xFF041E49),
            secondary = Color(0xFFE06D53),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDBCF),
            onSecondaryContainer = Color(0xFF3A0B00),
            tertiary = Color(0xFF34A853),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC4EED0),
            onTertiaryContainer = Color(0xFF00210B),
            background = Color(0xFFF8F9FF),
            onBackground = Color(0xFF1A1C1E),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1C1E)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA8C7FA),
            onPrimary = Color(0xFF062E6F),
            primaryContainer = Color(0xFF0842A0),
            onPrimaryContainer = Color(0xFFD3E3FD),
            secondary = Color(0xFFFFB59D),
            onSecondary = Color(0xFF5B1B08),
            secondaryContainer = Color(0xFF7B311C),
            onSecondaryContainer = Color(0xFFFFDBCF),
            tertiary = Color(0xFF6DD58C),
            onTertiary = Color(0xFF00391A),
            tertiaryContainer = Color(0xFF005227),
            onTertiaryContainer = Color(0xFFC4EED0),
            background = Color(0xFF111315),
            onBackground = Color(0xFFE2E2E6),
            surface = Color(0xFF1A1C1E),
            onSurface = Color(0xFFE2E2E6)
        )
    ),

    /**
     * 12. RAINBOW
     * Balanced Spectrum: Warm Golden Yellow, Soft Coral Red, Meadow Green, and Sapphire Blue.
     */
    RAINBOW(
        displayName = "Rainbow",
        light = lightColorScheme(
            primary = Color(0xFF7A5900),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDF9E),
            onPrimaryContainer = Color(0xFF261A00),
            secondary = Color(0xFFB3261E),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF9DEDC),
            onSecondaryContainer = Color(0xFF410E0B),
            tertiary = Color(0xFF006874),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF97F0FF),
            onTertiaryContainer = Color(0xFF001F24),
            background = Color(0xFFFFFBF7),
            onBackground = Color(0xFF1E1B16),
            surface = Color(0xFFFFFBF7),
            onSurface = Color(0xFF1E1B16)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF0C048),
            onPrimary = Color(0xFF402D00),
            primaryContainer = Color(0xFF5C4300),
            onPrimaryContainer = Color(0xFFFFDF9E),
            secondary = Color(0xFFF2B8B5),
            onSecondary = Color(0xFF601410),
            secondaryContainer = Color(0xFF8C1D18),
            onSecondaryContainer = Color(0xFFF9DEDC),
            tertiary = Color(0xFF4FD8EB),
            onTertiary = Color(0xFF00363D),
            tertiaryContainer = Color(0xFF004F58),
            onTertiaryContainer = Color(0xFF97F0FF),
            background = Color(0xFF16130E),
            onBackground = Color(0xFFEBE1D7),
            surface = Color(0xFF191611),
            onSurface = Color(0xFFEBE1D7)
        )
    ),

    /**
     * 13. SUNSET
     * Golden Hour Sunset Warmth: Warm Orange/Coral, Magenta, and Amber Glow.
     */
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFA83810),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDBCF),
            onPrimaryContainer = Color(0xFF3B0900),
            secondary = Color(0xFF98004B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E2),
            onSecondaryContainer = Color(0xFF3E001D),
            tertiary = Color(0xFF775A00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFDF9E),
            onTertiaryContainer = Color(0xFF251A00),
            background = Color(0xFFFFF8F6),
            onBackground = Color(0xFF231915),
            surface = Color(0xFFFFF8F6),
            onSurface = Color(0xFF231915)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB59B),
            onPrimary = Color(0xFF5F1500),
            primaryContainer = Color(0xFF862200),
            onPrimaryContainer = Color(0xFFFFDBCF),
            secondary = Color(0xFFFFB1C9),
            onSecondary = Color(0xFF5E002B),
            secondaryContainer = Color(0xFF83003E),
            onSecondaryContainer = Color(0xFFFFD9E2),
            tertiary = Color(0xFFEEC148),
            onTertiary = Color(0xFF3F2E00),
            tertiaryContainer = Color(0xFF5A4400),
            onTertiaryContainer = Color(0xFFFFDF9E),
            background = Color(0xFF1A110E),
            onBackground = Color(0xFFF1DFDA),
            surface = Color(0xFF1E1310),
            onSurface = Color(0xFFF1DFDA)
        )
    ),

    /**
     * 14. VIOLET
     * Deep Violet & Lavender Elegance with distinct secondary accents.
     */
    VIOLET(
        displayName = "Violet",
        light = lightColorScheme(
            primary = Color(0xFF7B2CBF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFF1DAFF),
            onPrimaryContainer = Color(0xFF2D0057),
            secondary = Color(0xFF006874),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFF97F0FF),
            onSecondaryContainer = Color(0xFF001F24),
            tertiary = Color(0xFF9A2562),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFD9E5),
            onTertiaryContainer = Color(0xFF3E0021),
            background = Color(0xFFFAF7FF),
            onBackground = Color(0xFF1D1A22),
            surface = Color(0xFFFAF7FF),
            onSurface = Color(0xFF1D1A22)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFDFB7FF),
            onPrimary = Color(0xFF480082),
            primaryContainer = Color(0xFF6200B3),
            onPrimaryContainer = Color(0xFFF1DAFF),
            secondary = Color(0xFF4FD8EB),
            onSecondary = Color(0xFF00363D),
            secondaryContainer = Color(0xFF004F58),
            onSecondaryContainer = Color(0xFF97F0FF),
            tertiary = Color(0xFFFFAFD0),
            onTertiary = Color(0xFF5E0038),
            tertiaryContainer = Color(0xFF7D0A4A),
            onTertiaryContainer = Color(0xFFFFD9E5),
            background = Color(0xFF15121A),
            onBackground = Color(0xFFE7E0EB),
            surface = Color(0xFF191520),
            onSurface = Color(0xFFE7E0EB)
        )
    );
}
