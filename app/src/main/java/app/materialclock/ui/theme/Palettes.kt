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
    ARTIST(
        displayName = "Artist",
        light = lightColorScheme(
            primary = Color(0xFF7C83FD),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0E3FF),
            onPrimaryContainer = Color(0xFF101452),
            secondary = Color(0xFFFF9800), // Distinct warm orange
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD54F), // Bright distinct yellow
            onSecondaryContainer = Color(0xFF3E2723),
            tertiary = Color(0xFF2C2D43),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF2C2D43),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF1A1A24),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A24),
            surfaceVariant = Color(0xFFECEFF8),
            onSurfaceVariant = Color(0xFF464653),
            outline = Color(0xFF767684),
            surfaceContainer = Color(0xFFE8EAF6), // Lighter & identical
            surfaceContainerHighest = Color(0xFFE8EAF6)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA5ACFF),
            onPrimary = Color(0xFF101452),
            primaryContainer = Color(0xFF2F3485),
            onPrimaryContainer = Color(0xFFE0E3FF),
            secondary = Color(0xFFFFB74D), // Warm contrast
            onSecondary = Color(0xFF4A3200),
            secondaryContainer = Color(0xFFF57F17), // Deep vibrant yellow/orange
            onSecondaryContainer = Color(0xFFFFF8E1),
            tertiary = Color(0xFFE0E1F5),
            onTertiary = Color(0xFF2C2D43),
            tertiaryContainer = Color(0xFF424465),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF14141B),
            onBackground = Color(0xFFE4E1E6),
            surface = Color(0xFF14141B),
            onSurface = Color(0xFFE4E1E6),
            surfaceVariant = Color(0xFF1D212A),
            onSurfaceVariant = Color(0xFFC7C5D0),
            outline = Color(0xFF90909A),
            surfaceContainer = Color(0xFF383D52), // Lighter & identical
            surfaceContainerHighest = Color(0xFF383D52)
        )
    ),

    EARTH(
        displayName = "Earth",
        light = lightColorScheme(
            primary = Color(0xFF386A34),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD8F2D1),
            onPrimaryContainer = Color(0xFF032104),
            secondary = Color(0xFF827717), // Earthy distinct olive
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE6EE9C), // Bright lime/sage
            onSecondaryContainer = Color(0xFF1B5E20),
            tertiary = Color(0xFF386A34),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF386A34),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF181D17),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF181D17),
            surfaceVariant = Color(0xFFEBF5E7),
            onSurfaceVariant = Color(0xFF43483F),
            outline = Color(0xFF73796E),
            surfaceContainer = Color(0xFFE4EDE4), // Lighter & identical
            surfaceContainerHighest = Color(0xFFE4EDE4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9CD695),
            onPrimary = Color(0xFF06390A),
            primaryContainer = Color(0xFF20511E),
            onPrimaryContainer = Color(0xFFD8F2D1),
            secondary = Color(0xFFD4E157), // Distinct neon earth green
            onSecondary = Color(0xFF33691E),
            secondaryContainer = Color(0xFF827717), // Earthy distinct
            onSecondaryContainer = Color(0xFFF0F4C3),
            tertiary = Color(0xFF9CD695),
            onTertiary = Color(0xFF06390A),
            tertiaryContainer = Color(0xFF386A34),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF101410),
            onBackground = Color(0xFFE0E4DB),
            surface = Color(0xFF101410),
            onSurface = Color(0xFFE0E4DB),
            surfaceVariant = Color(0xFF1A2419),
            onSurfaceVariant = Color(0xFFC3C8BC),
            outline = Color(0xFF8D9387),
            surfaceContainer = Color(0xFF273628), // Lighter & identical
            surfaceContainerHighest = Color(0xFF273628)
        )
    ),

    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFFA43B4B),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFD7A8A),
            onPrimaryContainer = Color(0xFF3B0011),
            secondary = Color(0xFFE65100), // Distinct warm peach/orange
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFCC80), // Peach distinct
            onSecondaryContainer = Color(0xFF4E342E),
            tertiary = Color(0xFFA43B4B),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFD7A8A),
            onTertiaryContainer = Color(0xFF000000),
            background = Color(0xFFFBF0F8),
            onBackground = Color(0xFF221720),
            surface = Color(0xFFFBF0F8),
            onSurface = Color(0xFF221720),
            surfaceVariant = Color(0xFFFFEEFE),
            onSurfaceVariant = Color(0xFF221720),
            outline = Color(0xFF8A7280),
            surfaceContainer = Color(0xFFF2E6EA), // Lighter & identical
            surfaceContainerHighest = Color(0xFFF2E6EA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB3B8),
            onPrimary = Color(0xFF650B20),
            primaryContainer = Color(0xFF842335),
            onPrimaryContainer = Color(0xFFFFD9DD),
            secondary = Color(0xFFFFB74D), // Distinct warm contrast
            onSecondary = Color(0xFF4E342E),
            secondaryContainer = Color(0xFFEF6C00), // Bright contrast
            onSecondaryContainer = Color(0xFFFFE0B2),
            tertiary = Color(0xFFEAB78D),
            onTertiary = Color(0xFF462604),
            tertiaryContainer = Color(0xFF623C18),
            onTertiaryContainer = Color(0xFFFFDCC1),
            background = Color(0xFF1A1114),
            onBackground = Color(0xFFF1DFE1),
            surface = Color(0xFF1A1114),
            onSurface = Color(0xFFF1DFE1),
            surfaceVariant = Color(0xFF534345),
            onSurfaceVariant = Color(0xFFD8C2C4),
            outline = Color(0xFFA08C8E),
            surfaceContainer = Color(0xFF382A2D), // Lighter & identical
            surfaceContainerHighest = Color(0xFF382A2D)
        )
    ),

    GALAXIA(
        displayName = "Galaxia",
        light = lightColorScheme(
            primary = Color(0xFF673AB7),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            secondary = Color(0xFFC2185B), // Distinct magenta
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF8BBD0), // Bright pink
            onSecondaryContainer = Color(0xFF4A148C),
            tertiary = Color(0xFF7D5260),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF673AB7),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF1C1B1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFE3E3FD),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E),
            surfaceContainer = Color(0xFFF3EDF9), // Lighter & identical
            surfaceContainerHighest = Color(0xFFF3EDF9)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF23144A),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFFF4081), // Neon distinct pink
            onSecondary = Color(0xFF311B92),
            secondaryContainer = Color(0xFFC2185B), // Deep vibrant magenta
            onSecondaryContainer = Color(0xFFFCE4EC),
            tertiary = Color(0xFFEFB8C8),
            onTertiary = Color(0xFF492532),
            tertiaryContainer = Color(0xFF8A59FF),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF151318),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF151318),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF1A1B2D),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99),
            surfaceContainer = Color(0xFF3D384D), // Lighter & identical
            surfaceContainerHighest = Color(0xFF3D384D)
        )
    ),

    INDIGO(
        displayName = "Indigo",
        light = lightColorScheme(
            primary = Color(0xFF2563EB),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDBEAFE),
            onPrimaryContainer = Color(0xFF1E3A8A),
            secondary = Color(0xFF00ACC1), // Distinct Cyan
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFB2EBF2), // Light aqua distinct
            onSecondaryContainer = Color(0xFF006064),
            tertiary = Color(0xFF2563EB),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1D4ED8),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE6ECF9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFF94A3B8),
            surfaceContainer = Color(0xFFE5EDFA), // Lighter & identical
            surfaceContainerHighest = Color(0xFFE5EDFA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color(0xFF002855),
            primaryContainer = Color(0xFF1E3A8A),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF18FFFF), // Vibrant neon cyan
            onSecondary = Color(0xFF004D40),
            secondaryContainer = Color(0xFF00838F), // Distinct dark cyan
            onSecondaryContainer = Color(0xFFE0F7FA),
            tertiary = Color(0xFF60A5FA),
            onTertiary = Color(0xFF002855),
            tertiaryContainer = Color(0xFF3B82F6),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF0F131A),
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF0F131A),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF1B2232),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF5A7291),
            surfaceContainer = Color(0xFF283A5C), // Lighter & identical
            surfaceContainerHighest = Color(0xFF283A5C)
        )
    ),

    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF4A5568), // Cool slate gray
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E8F0), // Bright moon silver
            onPrimaryContainer = Color(0xFF1A202C), // Deep night black
            secondary = Color(0xFF718096),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCBD5E0), // Soft lunar gray
            onSecondaryContainer = Color(0xFF2D3748),
            tertiary = Color(0xFF2D3748),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF4A5568),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF121212),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF121212),
            surfaceVariant = Color(0xFFEDF2F7),
            onSurfaceVariant = Color(0xFF4A5568),
            outline = Color(0xFFA0AEC0),
            surfaceContainer = Color(0xFFF0F2F5), // Lighter & identical
            surfaceContainerHighest = Color(0xFFF0F2F5)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF6F8FA), // Glowing moon white
            onPrimary = Color(0xFF121212),
            primaryContainer = Color(0xFF3A3F47), // Deep lunar crater gray
            onPrimaryContainer = Color(0xFFF6F8FA),
            secondary = Color(0xFFE2E8F0), // Moonlit silver
            onSecondary = Color(0xFF1A202C),
            secondaryContainer = Color(0xFF4A5568),
            onSecondaryContainer = Color(0xFFF6F8FA),
            tertiary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFF121212),
            tertiaryContainer = Color(0xFF718096),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF121212), // Deep night sky
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF121212),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF2D3748),
            onSurfaceVariant = Color(0xFFCBD5E0),
            outline = Color(0xFF718096),
            surfaceContainer = Color(0xFF282C34), // Lighter & identical
            surfaceContainerHighest = Color(0xFF282C34)
        )
    ),

    NUCLEUS(
        displayName = "Nucleus",
        light = lightColorScheme(
            primary = Color(0xFF212121),
            onPrimary = Color(0xFFF4F2F4), // Adjusted for readability against #212121
            primaryContainer = Color(0xFFD61818),
            onPrimaryContainer = Color(0xFFF4F2F4), // Clean contrast on red
            secondary = Color(0xFFD61818),
            onSecondary = Color(0xFFF4F2F4),
            secondaryContainer = Color(0xFFBFC0C1),
            onSecondaryContainer = Color(0xFF1B1C1E),
            tertiary = Color(0xFFD61818),
            onTertiary = Color(0xFFF4F2F4),
            tertiaryContainer = Color(0xFFF4F2F4), // From your mapping
            onTertiaryContainer = Color(0xFF212121), // From your mapping
            background = Color(0xFFF4F2F4), // From your mapping
            onBackground = Color(0xFF1B1C1E),
            surface = Color(0xFFF4F2F4),
            onSurface = Color(0xFF1B1C1E),
            surfaceVariant = Color(0xFFE5E4E7),
            onSurfaceVariant = Color(0xFF1B1C1E),
            outline = Color(0xFF747478),
            surfaceContainer = Color(0xFFE4E2E5), // From your mapping & identical
            surfaceContainerHighest = Color(0xFFE4E2E5) // From your mapping & identical
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF4F2F4), // From your mapping
            onPrimary = Color(0xFF212121), // From your mapping
            primaryContainer = Color(0xFFD61818), // From your mapping
            onPrimaryContainer = Color(0xFFF4F2F4), // From your mapping
            secondary = Color(0xFFF4F2F4),
            onSecondary = Color(0xFF212121),
            secondaryContainer = Color(0xFF929395),
            onSecondaryContainer = Color(0xFFF4F2F4),
            tertiary = Color(0xFFD61818),
            onTertiary = Color(0xFFF4F2F4),
            tertiaryContainer = Color(0xFF1C1C1D),
            onTertiaryContainer = Color(0xFFF4F2F4),
            background = Color(0xFF000000), // From your mapping
            onBackground = Color(0xFFF4F2F4),
            surface = Color(0xFF000000), // From your mapping
            onSurface = Color(0xFFF4F2F4),
            surfaceVariant = Color(0xFF1C1C1D),
            onSurfaceVariant = Color(0xFFF4F2F4),
            outline = Color(0xFF8E8E93),
            surfaceContainer = Color(0xFF1C1C1D), // From your mapping & identical
            surfaceContainerHighest = Color(0xFF1C1C1D) // From your mapping & identical
        )
    ),

    OCEAN(
        displayName = "Ocean",
        light = lightColorScheme(
            primary = Color(0xFF00838F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB2EBF2),
            onPrimaryContainer = Color(0xFF00363A),
            secondary = Color(0xFF0277BD), // Distinct ocean blue
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFB3E5FC), // Distinct water blue
            onSecondaryContainer = Color(0xFF01579B),
            tertiary = Color(0xFF00838F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD71917),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF041014),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF041014),
            surfaceVariant = Color(0xFFE0F4F8),
            onSurfaceVariant = Color(0xFF3F484A),
            outline = Color(0xFF6F797A),
            surfaceContainer = Color(0xFFE0F1F2), // Lighter & identical
            surfaceContainerHighest = Color(0xFFE0F1F2)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF4DD0E1),
            onPrimary = Color(0xFF00363A),
            primaryContainer = Color(0xFF004D40),
            onPrimaryContainer = Color(0xFFE0F7FA),
            secondary = Color(0xFF40C4FF), // Distinct bright blue
            onSecondary = Color(0xFF01579B),
            secondaryContainer = Color(0xFF0277BD), // Distinct depth blue
            onSecondaryContainer = Color(0xFFE1F5FE),
            tertiary = Color(0xFF80DEEA),
            onTertiary = Color(0xFF00363A),
            tertiaryContainer = Color(0xFF004D40),
            onTertiaryContainer = Color(0xFFE0F7FA),
            background = Color(0xFF0B1618),
            onBackground = Color(0xFFE0F7FA),
            surface = Color(0xFF0B1618),
            onSurface = Color(0xFFE0F7FA),
            surfaceVariant = Color(0xFF3F484A),
            onSurfaceVariant = Color(0xFFBFC8CA),
            outline = Color(0xFF899294),
            surfaceContainer = Color(0xFF213638), // Lighter & identical
            surfaceContainerHighest = Color(0xFF213638)
        )
    ),

    PRIME(
        displayName = "Prime",
        light = lightColorScheme(
            primary = Color(0xFFF06292),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFCE4EC),
            onPrimaryContainer = Color(0xFF4B001F),
            secondary = Color(0xFFAB47BC), // Distinct rich purple
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE1BEE7), // Distinct soft purple
            onSecondaryContainer = Color(0xFF4A148C),
            tertiary = Color(0xFFF06292),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFEC407A),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF201A1C),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF201A1C),
            surfaceVariant = Color(0xFFDDD8F3),
            onSurfaceVariant = Color(0xFF514347),
            outline = Color(0xFF837377),
            surfaceContainer = Color(0xFFF5E6ED), // Lighter & identical
            surfaceContainerHighest = Color(0xFFF5E6ED)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF48FB1),
            onPrimary = Color(0xFF5C0028),
            primaryContainer = Color(0xFF880E4F),
            onPrimaryContainer = Color(0xFFFCE4EC),
            secondary = Color(0xFFCE93D8), // Distinct violet contrast
            onSecondary = Color(0xFF4A148C),
            secondaryContainer = Color(0xFF7B1FA2), // Vibrant distinct
            onSecondaryContainer = Color(0xFFF3E5F5),
            tertiary = Color(0xFFFF80AB),
            onTertiary = Color(0xFF4A001F),
            tertiaryContainer = Color(0xFFF06292),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF1A1114),
            onBackground = Color(0xFFEBE0E2),
            surface = Color(0xFF1A1114),
            onSurface = Color(0xFFEBE0E2),
            surfaceVariant = Color(0xFF1E1B2E),
            onSurfaceVariant = Color(0xFFD6C2C6),
            outline = Color(0xFF9E8C90),
            surfaceContainer = Color(0xFF422938), // Lighter & identical
            surfaceContainerHighest = Color(0xFF422938)
        )
    ),

    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFD84315),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFCCBC),
            onPrimaryContainer = Color(0xFF4E1400),
            secondary = Color(0xFFC62828), // Distinct deep red
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFCDD2), // Soft distinct red
            onSecondaryContainer = Color(0xFFB71C1C),
            tertiary = Color(0xFFEF6C00),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFEF6C00),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF201A18),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF201A18),
            surfaceVariant = Color(0xFFEBEAE6),
            onSurfaceVariant = Color(0xFF51443E),
            outline = Color(0xFF84746C),
            surfaceContainer = Color(0xFFF7EBE5), // Lighter & identical
            surfaceContainerHighest = Color(0xFFF7EBE5)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFAB91),
            onPrimary = Color(0xFF6B1800),
            primaryContainer = Color(0xFF9E2A00),
            onPrimaryContainer = Color(0xFFFFCCBC),
            secondary = Color(0xFFEF9A9A), // Distinct light red
            onSecondary = Color(0xFFB71C1C),
            secondaryContainer = Color(0xFFC62828), // Deep distinct red
            onSecondaryContainer = Color(0xFFFFEBEE),
            tertiary = Color(0xFFFF9800),
            onTertiary = Color(0xFF4B2000),
            tertiaryContainer = Color(0xFFE64A19),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFF1A120F),
            onBackground = Color(0xFFECE0DA),
            surface = Color(0xFF1A120F),
            onSurface = Color(0xFFECE0DA),
            surfaceVariant = Color(0xFF23211E),
            onSurfaceVariant = Color(0xFFD6C3B9),
            outline = Color(0xFF9E8D85),
            surfaceContainer = Color(0xFF4A3226), // Lighter & identical
            surfaceContainerHighest = Color(0xFF4A3226)
        )
    ),

    WOODLAND(
        displayName = "Woodland",
        light = lightColorScheme(
            primary = Color(0xFF1C1C1C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEADAD7),
            onPrimaryContainer = Color(0xFF1C1C1C),
            secondary = Color(0xFF5D4037), // Distinct earthy brown
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD7CCC8), // Light wood
            onSecondaryContainer = Color(0xFF3E2723),
            tertiary = Color(0xFF1C1C1C),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFF1C1C1C),
            onTertiaryContainer = Color(0xFFFFFFFF),
            background = Color(0xFFF8F9FD),
            onBackground = Color(0xFF1C1C1C),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1C1C),
            surfaceVariant = Color(0xFFE8DFDD),
            onSurfaceVariant = Color(0xFF4A4544),
            outline = Color(0xFF7A7574),
            surfaceContainer = Color(0xFFE8ECEF), // Lighter & identical
            surfaceContainerHighest = Color(0xFFE8ECEF)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF0EBE9),
            onPrimary = Color(0xFF1C1C1C),
            primaryContainer = Color(0xFF3D3331),
            onPrimaryContainer = Color(0xFFF0EBE9),
            secondary = Color(0xFFBCAAA4), // Distinct wood grey
            onSecondary = Color(0xFF3E2723),
            secondaryContainer = Color(0xFF5D4037), // Deep wood brown
            onSecondaryContainer = Color(0xFFEFEBE9),
            tertiary = Color(0xFFF0EBE9),
            onTertiary = Color(0xFF1C1C1C),
            tertiaryContainer = Color(0xFFF0EBE9),
            onTertiaryContainer = Color(0xFF1C1C1C),
            background = Color(0xFF171515),
            onBackground = Color(0xFFE8E1DF),
            surface = Color(0xFF171515),
            onSurface = Color(0xFFE8E1DF),
            surfaceVariant = Color(0xFF2E2726),
            onSurfaceVariant = Color(0xFFC4BDBB),
            outline = Color(0xFF918A89),
            surfaceContainer = Color(0xFF33302F), // Lighter & identical
            surfaceContainerHighest = Color(0xFF33302F)
        )
    )
}
