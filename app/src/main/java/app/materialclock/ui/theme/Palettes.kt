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

    // 1. CREATIVE — Artistic Mauve, Apricot and Sage
    CREATIVE(
        displayName = "Creative",
        light = lightColorScheme(
            primary = Color(0xFF75465B),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFF5D9E4),
            onPrimaryContainer = Color(0xFF321522),

            secondary = Color(0xFF76513B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDDC5),
            onSecondaryContainer = Color(0xFF301B0C),

            tertiary = Color(0xFF52664A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFDCEBCF),
            onTertiaryContainer = Color(0xFF192515),

            background = Color(0xFFFFF8FA),
            onBackground = Color(0xFF241A20),
            surface = Color(0xFFFFF8FA),
            onSurface = Color(0xFF241A20),
            surfaceVariant = Color(0xFFECE0E5),
            onSurfaceVariant = Color(0xFF51434A),
            surfaceTint = Color(0xFF75465B),

            inverseSurface = Color(0xFF35282F),
            inverseOnSurface = Color(0xFFF8EAF0),
            inversePrimary = Color(0xFFE5B5CA),

            outline = Color(0xFF82717A),
            outlineVariant = Color(0xFFD4C3CB),

            error = Color(0xFFBA3D4B),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDADF),
            onErrorContainer = Color(0xFF41000C),

            surfaceBright = Color(0xFFFFF8FA),
            surfaceDim = Color(0xFFE5D9DF),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAEEF3),
            surfaceContainer = Color(0xFFF2E5EB),
            surfaceContainerHigh = Color(0xFFEADCE3),
            surfaceContainerHighest = Color(0xFFE1D3DA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE8B6CB),
            onPrimary = Color(0xFF452337),
            primaryContainer = Color(0xFF694052),
            onPrimaryContainer = Color(0xFFF5D9E4),

            secondary = Color(0xFFE7BEA2),
            onSecondary = Color(0xFF472B1A),
            secondaryContainer = Color(0xFF62432F),
            onSecondaryContainer = Color(0xFFFFDDC5),

            tertiary = Color(0xFFBED1B0),
            onTertiary = Color(0xFF263720),
            tertiaryContainer = Color(0xFF3B4E34),
            onTertiaryContainer = Color(0xFFDCEBCF),

            background = Color(0xFF21181E),
            onBackground = Color(0xFFEFE1E8),
            surface = Color(0xFF21181E),
            onSurface = Color(0xFFEFE1E8),
            surfaceVariant = Color(0xFF51414A),
            onSurfaceVariant = Color(0xFFD7C2CD),
            surfaceTint = Color(0xFFE8B6CB),

            inverseSurface = Color(0xFFEFE1E8),
            inverseOnSurface = Color(0xFF382A32),
            inversePrimary = Color(0xFF75465B),

            outline = Color(0xFF9F8995),
            outlineVariant = Color(0xFF51414A),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF40333B),
            surfaceDim = Color(0xFF171016),
            surfaceContainerLowest = Color(0xFF160F14),
            surfaceContainerLow = Color(0xFF291F26),
            surfaceContainer = Color(0xFF2D232A),
            surfaceContainerHigh = Color(0xFF382D35),
            surfaceContainerHighest = Color(0xFF443842)
        )
    ),

    // 2. EXPRESSIVE — Orchid, Lime and Airy Cyan
    EXPRESSIVE(
        displayName = "Expressive",
        light = lightColorScheme(
            primary = Color(0xFF68518A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEBDDFF),
            onPrimaryContainer = Color(0xFF251344),

            secondary = Color(0xFF58672D),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0EDAA),
            onSecondaryContainer = Color(0xFF1A2200),

            tertiary = Color(0xFF416878),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD1EDFC),
            onTertiaryContainer = Color(0xFF102936),

            background = Color(0xFFFAF8FF),
            onBackground = Color(0xFF1D1A24),
            surface = Color(0xFFFAF8FF),
            onSurface = Color(0xFF1D1A24),
            surfaceVariant = Color(0xFFE8E0F0),
            onSurfaceVariant = Color(0xFF494252),
            surfaceTint = Color(0xFF68518A),

            inverseSurface = Color(0xFF302B38),
            inverseOnSurface = Color(0xFFF2EBFA),
            inversePrimary = Color(0xFFD4BDF8),

            outline = Color(0xFF797184),
            outlineVariant = Color(0xFFD0C6DC),

            error = Color(0xFFB74752),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDADD),
            onErrorContainer = Color(0xFF410009),

            surfaceBright = Color(0xFFFAF8FF),
            surfaceDim = Color(0xFFE2D9EB),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF5EFFB),
            surfaceContainer = Color(0xFFEEE7F5),
            surfaceContainerHigh = Color(0xFFE5DDEC),
            surfaceContainerHighest = Color(0xFFDBD3E4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD5BDF7),
            onPrimary = Color(0xFF382653),
            primaryContainer = Color(0xFF51406F),
            onPrimaryContainer = Color(0xFFEBDDFF),

            secondary = Color(0xFFC4D18C),
            onSecondary = Color(0xFF303A0D),
            secondaryContainer = Color(0xFF414D20),
            onSecondaryContainer = Color(0xFFE0EDAA),

            tertiary = Color(0xFFB4D9EA),
            onTertiary = Color(0xFF213C49),
            tertiaryContainer = Color(0xFF314F5D),
            onTertiaryContainer = Color(0xFFD1EDFC),

            background = Color(0xFF1C1923),
            onBackground = Color(0xFFE9E1F0),
            surface = Color(0xFF1C1923),
            onSurface = Color(0xFFE9E1F0),
            surfaceVariant = Color(0xFF48404F),
            onSurfaceVariant = Color(0xFFD0C4DD),
            surfaceTint = Color(0xFFD5BDF7),

            inverseSurface = Color(0xFFE9E1F0),
            inverseOnSurface = Color(0xFF312A39),
            inversePrimary = Color(0xFF68518A),

            outline = Color(0xFF988BA7),
            outlineVariant = Color(0xFF48404F),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF3A3342),
            surfaceDim = Color(0xFF15121B),
            surfaceContainerLowest = Color(0xFF141119),
            surfaceContainerLow = Color(0xFF24202B),
            surfaceContainer = Color(0xFF292530),
            surfaceContainerHigh = Color(0xFF342F3C),
            surfaceContainerHighest = Color(0xFF403A49)
        )
    ),

    // 3. GALAXY — Cosmic Indigo, Rose Nebula and Champagne
    GALAXY(
        displayName = "Galaxy",
        light = lightColorScheme(
            primary = Color(0xFF465A94),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDCE3FF),
            onPrimaryContainer = Color(0xFF101F4B),

            secondary = Color(0xFF86506D),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E9),
            onSecondaryContainer = Color(0xFF351126),

            tertiary = Color(0xFF76602F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF9E2AC),
            onTertiaryContainer = Color(0xFF291D00),

            background = Color(0xFFF7F8FF),
            onBackground = Color(0xFF191C27),
            surface = Color(0xFFF7F8FF),
            onSurface = Color(0xFF191C27),
            surfaceVariant = Color(0xFFE1E5F3),
            onSurfaceVariant = Color(0xFF434858),
            surfaceTint = Color(0xFF465A94),

            inverseSurface = Color(0xFF2B3040),
            inverseOnSurface = Color(0xFFEFF0FA),
            inversePrimary = Color(0xFFBAC8F8),

            outline = Color(0xFF73798C),
            outlineVariant = Color(0xFFC7CDDF),

            error = Color(0xFFB54A59),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDADD),
            onErrorContainer = Color(0xFF41000B),

            surfaceBright = Color(0xFFF7F8FF),
            surfaceDim = Color(0xFFDFE2EF),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF1F3FC),
            surfaceContainer = Color(0xFFE9ECF7),
            surfaceContainerHigh = Color(0xFFE0E4F1),
            surfaceContainerHighest = Color(0xFFD7DBE9)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFBAC8F8),
            onPrimary = Color(0xFF253968),
            primaryContainer = Color(0xFF344778),
            onPrimaryContainer = Color(0xFFDCE3FF),

            secondary = Color(0xFFEAB9D0),
            onSecondary = Color(0xFF50263D),
            secondaryContainer = Color(0xFF693C54),
            onSecondaryContainer = Color(0xFFFFD9E9),

            tertiary = Color(0xFFE1C58A),
            onTertiary = Color(0xFF44330C),
            tertiaryContainer = Color(0xFF5D481E),
            onTertiaryContainer = Color(0xFFF9E2AC),

            background = Color(0xFF171A27),
            onBackground = Color(0xFFE2E4F0),
            surface = Color(0xFF171A27),
            onSurface = Color(0xFFE2E4F0),
            surfaceVariant = Color(0xFF414657),
            onSurfaceVariant = Color(0xFFC5CADE),
            surfaceTint = Color(0xFFBAC8F8),

            inverseSurface = Color(0xFFE2E4F0),
            inverseOnSurface = Color(0xFF2B2E3B),
            inversePrimary = Color(0xFF465A94),

            outline = Color(0xFF8C93A9),
            outlineVariant = Color(0xFF414657),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF35394A),
            surfaceDim = Color(0xFF111420),
            surfaceContainerLowest = Color(0xFF10121E),
            surfaceContainerLow = Color(0xFF1D202E),
            surfaceContainer = Color(0xFF222533),
            surfaceContainerHigh = Color(0xFF2D3040),
            surfaceContainerHighest = Color(0xFF393C4D)
        )
    ),

    // 4. GLACIER — Frosted Teal, Mist and Ice
    GLACIER(
        displayName = "Glacier",
        light = lightColorScheme(
            primary = Color(0xFF386B78),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD0F0F7),
            onPrimaryContainer = Color(0xFF0B3038),

            secondary = Color(0xFF626D7A),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0E7F0),
            onSecondaryContainer = Color(0xFF202A35),

            tertiary = Color(0xFF526A86),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD8E8FF),
            onTertiaryContainer = Color(0xFF182D45),

            background = Color(0xFFF5FAFC),
            onBackground = Color(0xFF182024),
            surface = Color(0xFFF5FAFC),
            onSurface = Color(0xFF182024),
            surfaceVariant = Color(0xFFE0E9ED),
            onSurfaceVariant = Color(0xFF414D52),
            surfaceTint = Color(0xFF386B78),

            inverseSurface = Color(0xFF293439),
            inverseOnSurface = Color(0xFFEBF3F6),
            inversePrimary = Color(0xFFA8DCE7),

            outline = Color(0xFF74838A),
            outlineVariant = Color(0xFFC5D3D9),

            error = Color(0xFFB7464A),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD9),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFF5FAFC),
            surfaceDim = Color(0xFFDCE7EB),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFEDF5F8),
            surfaceContainer = Color(0xFFE5EEF1),
            surfaceContainerHigh = Color(0xFFDCE7EB),
            surfaceContainerHighest = Color(0xFFD3DFE4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA8DCE7),
            onPrimary = Color(0xFF173B44),
            primaryContainer = Color(0xFF2B535D),
            onPrimaryContainer = Color(0xFFD0F0F7),

            secondary = Color(0xFFC4D0DC),
            onSecondary = Color(0xFF303A45),
            secondaryContainer = Color(0xFF434F5B),
            onSecondaryContainer = Color(0xFFE0E7F0),

            tertiary = Color(0xFFB9D1F1),
            onTertiary = Color(0xFF2A4058),
            tertiaryContainer = Color(0xFF3D5670),
            onTertiaryContainer = Color(0xFFD8E8FF),

            background = Color(0xFF151C20),
            onBackground = Color(0xFFDDE7EA),
            surface = Color(0xFF151C20),
            onSurface = Color(0xFFDDE7EA),
            surfaceVariant = Color(0xFF3D4A50),
            onSurfaceVariant = Color(0xFFC0D0D5),
            surfaceTint = Color(0xFFA8DCE7),

            inverseSurface = Color(0xFFDDE7EA),
            inverseOnSurface = Color(0xFF283135),
            inversePrimary = Color(0xFF386B78),

            outline = Color(0xFF87979D),
            outlineVariant = Color(0xFF3D4A50),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF323D42),
            surfaceDim = Color(0xFF101619),
            surfaceContainerLowest = Color(0xFF0F1518),
            surfaceContainerLow = Color(0xFF1C2428),
            surfaceContainer = Color(0xFF21292D),
            surfaceContainerHigh = Color(0xFF2C3439),
            surfaceContainerHighest = Color(0xFF384147)
        )
    ),

    // 5. INVISIBLE — Translucent Frosted Glass
    INVISIBLE(
        displayName = "Invisible",
        light = lightColorScheme(
            primary = Color(0xBFA7A7A7),
            onPrimary = Color(0xFF151515),
            primaryContainer = Color(0x52D5D5D5),
            onPrimaryContainer = Color(0xFF222222),

            secondary = Color(0x997C858C),
            onSecondary = Color(0xFF101820),
            secondaryContainer = Color(0x43B9C5CC),
            onSecondaryContainer = Color(0xFF202830),

            tertiary = Color(0xA6A4B6C4),
            onTertiary = Color(0xFF19212A),
            tertiaryContainer = Color(0x57D6DFE7),
            onTertiaryContainer = Color(0xFF202832),

            background = Color(0xBFFFFFFF),
            onBackground = Color(0xFF202020),
            surface = Color(0xD9F7F7F7),
            onSurface = Color(0xFF202020),
            surfaceVariant = Color(0x80D2D7DC),
            onSurfaceVariant = Color(0xFF303840),
            surfaceTint = Color(0xBFA7A7A7),

            inverseSurface = Color(0xD9323232),
            inverseOnSurface = Color(0xFFF8F8F8),
            inversePrimary = Color(0xD0D0D0D0),

            outline = Color(0x99707880),
            outlineVariant = Color(0x66A0A8B0),

            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),

            surfaceBright = Color(0xDDFDFDFD),
            surfaceDim = Color(0x99D1D1D1),
            surfaceContainerLowest = Color(0xE6FFFFFF),
            surfaceContainerLow = Color(0xCFEFEFEF),
            surfaceContainer = Color(0xC4E5E5E5),
            surfaceContainerHigh = Color(0xB8D9D9D9),
            surfaceContainerHighest = Color(0xACCDCDCD)
        ),
        dark = darkColorScheme(
            primary = Color(0xDDE4E4E4),
            onPrimary = Color(0xFF161616),
            primaryContainer = Color(0x52A0A0A0),
            onPrimaryContainer = Color(0xFFF4F4F4),

            secondary = Color(0xB7B8C0C8),
            onSecondary = Color(0xFF171B20),
            secondaryContainer = Color(0x4F7F8993),
            onSecondaryContainer = Color(0xFFE8EDF2),

            tertiary = Color(0xA6B4B6C7),
            onTertiary = Color(0xFF171B24),
            tertiaryContainer = Color(0x557A8196),
            onTertiaryContainer = Color(0xFFE7EAF3),

            background = Color(0xCC101010),
            onBackground = Color(0xFFF1F1F1),
            surface = Color(0xB31A1A1A),
            onSurface = Color(0xFFF1F1F1),
            surfaceVariant = Color(0x80515151),
            onSurfaceVariant = Color(0xFFE0E0E0),
            surfaceTint = Color(0xDDE4E4E4),

            inverseSurface = Color(0xDDEBEBEB),
            inverseOnSurface = Color(0xFF222222),
            inversePrimary = Color(0xFF626262),

            outline = Color(0xFF969696),
            outlineVariant = Color(0xFF505050),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xCC383838),
            surfaceDim = Color(0x990B0B0B),
            surfaceContainerLowest = Color(0x99080808),
            surfaceContainerLow = Color(0xAA202020),
            surfaceContainer = Color(0xB02A2A2A),
            surfaceContainerHigh = Color(0xBC343434),
            surfaceContainerHighest = Color(0xC8424242)
        )
    ),

    // 6. INDUS — Terracotta, Sandstone and Copper
    INDUS(
        displayName = "Indus",
        light = lightColorScheme(
            primary = Color(0xFF92523D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDFD2),
            onPrimaryContainer = Color(0xFF37170D),

            secondary = Color(0xFF796039),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF4E1BB),
            onSecondaryContainer = Color(0xFF2A200B),

            tertiary = Color(0xFF5F6742),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE5E8C5),
            onTertiaryContainer = Color(0xFF20250D),

            background = Color(0xFFFFF8F3),
            onBackground = Color(0xFF241B17),
            surface = Color(0xFFFFF8F3),
            onSurface = Color(0xFF241B17),
            surfaceVariant = Color(0xFFEFE1D9),
            onSurfaceVariant = Color(0xFF50423A),
            surfaceTint = Color(0xFF92523D),

            inverseSurface = Color(0xFF342923),
            inverseOnSurface = Color(0xFFF8EDE6),
            inversePrimary = Color(0xFFE9B7A4),

            outline = Color(0xFF837168),
            outlineVariant = Color(0xFFD7C5BA),

            error = Color(0xFFB84B45),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD5),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFFFF8F3),
            surfaceDim = Color(0xFFE7D9D0),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAEFE8),
            surfaceContainer = Color(0xFFF1E5DC),
            surfaceContainerHigh = Color(0xFFE8DAD1),
            surfaceContainerHighest = Color(0xFFDFD1C8)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE9B7A4),
            onPrimary = Color(0xFF4D281C),
            primaryContainer = Color(0xFF70402F),
            onPrimaryContainer = Color(0xFFFFDFD2),

            secondary = Color(0xFFD9C49B),
            onSecondary = Color(0xFF3D3017),
            secondaryContainer = Color(0xFF564429),
            onSecondaryContainer = Color(0xFFF4E1BB),

            tertiary = Color(0xFFC9CCA4),
            onTertiary = Color(0xFF30351B),
            tertiaryContainer = Color(0xFF484E30),
            onTertiaryContainer = Color(0xFFE5E8C5),

            background = Color(0xFF211814),
            onBackground = Color(0xFFF0E0D8),
            surface = Color(0xFF211814),
            onSurface = Color(0xFFF0E0D8),
            surfaceVariant = Color(0xFF514039),
            onSurfaceVariant = Color(0xFFD9C4B9),
            surfaceTint = Color(0xFFE9B7A4),

            inverseSurface = Color(0xFFF0E0D8),
            inverseOnSurface = Color(0xFF382A24),
            inversePrimary = Color(0xFF92523D),

            outline = Color(0xFF9C867B),
            outlineVariant = Color(0xFF514039),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF40332B),
            surfaceDim = Color(0xFF17100D),
            surfaceContainerLowest = Color(0xFF160F0C),
            surfaceContainerLow = Color(0xFF291F1A),
            surfaceContainer = Color(0xFF2F241F),
            surfaceContainerHigh = Color(0xFF3A2E28),
            surfaceContainerHighest = Color(0xFF463932)
        )
    ),

    // 7. LIQUID — Seafoam, Aquamarine and Coral
    LIQUID(
        displayName = "Liquid",
        light = lightColorScheme(
            primary = Color(0xFF28766D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFC0F2E9),
            onPrimaryContainer = Color(0xFF063630),

            secondary = Color(0xFF986044),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDCCB),
            onSecondaryContainer = Color(0xFF381C0D),

            tertiary = Color(0xFF47748A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD0EEFF),
            onTertiaryContainer = Color(0xFF102D3B),

            background = Color(0xFFF3FBF8),
            onBackground = Color(0xFF17201D),
            surface = Color(0xFFF3FBF8),
            onSurface = Color(0xFF17201D),
            surfaceVariant = Color(0xFFDDECE7),
            onSurfaceVariant = Color(0xFF3E514B),
            surfaceTint = Color(0xFF28766D),

            inverseSurface = Color(0xFF263632),
            inverseOnSurface = Color(0xFFE9F4F0),
            inversePrimary = Color(0xFF9DDED3),

            outline = Color(0xFF6D837C),
            outlineVariant = Color(0xFFC2D5CE),

            error = Color(0xFFB74749),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD8),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFF3FBF8),
            surfaceDim = Color(0xFFD8E8E2),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFECF7F2),
            surfaceContainer = Color(0xFFE4F0EB),
            surfaceContainerHigh = Color(0xFFDBE9E3),
            surfaceContainerHighest = Color(0xFFD2E0DA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9DDED3),
            onPrimary = Color(0xFF16473F),
            primaryContainer = Color(0xFF286057),
            onPrimaryContainer = Color(0xFFC0F2E9),

            secondary = Color(0xFFF0BDA4),
            onSecondary = Color(0xFF502C19),
            secondaryContainer = Color(0xFF70422D),
            onSecondaryContainer = Color(0xFFFFDCCB),

            tertiary = Color(0xFFB5DDF0),
            onTertiary = Color(0xFF254556),
            tertiaryContainer = Color(0xFF385D70),
            onTertiaryContainer = Color(0xFFD0EEFF),

            background = Color(0xFF14201D),
            onBackground = Color(0xFFDCEAE5),
            surface = Color(0xFF14201D),
            onSurface = Color(0xFFDCEAE5),
            surfaceVariant = Color(0xFF3B504A),
            onSurfaceVariant = Color(0xFFBED3CC),
            surfaceTint = Color(0xFF9DDED3),

            inverseSurface = Color(0xFFDCEAE5),
            inverseOnSurface = Color(0xFF263531),
            inversePrimary = Color(0xFF28766D),

            outline = Color(0xFF819A92),
            outlineVariant = Color(0xFF3B504A),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF31403B),
            surfaceDim = Color(0xFF0E1714),
            surfaceContainerLowest = Color(0xFF0D1613),
            surfaceContainerLow = Color(0xFF1A2823),
            surfaceContainer = Color(0xFF202E29),
            surfaceContainerHigh = Color(0xFF2B3934),
            surfaceContainerHighest = Color(0xFF374540)
        )
    ),

    // 8. MOON — Moonstone, Silver and Muted Blue
    MOON(
        displayName = "Moon",
        light = lightColorScheme(
            primary = Color(0xFF596B80),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDCE6F4),
            onPrimaryContainer = Color(0xFF192B3D),

            secondary = Color(0xFF706477),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFECE0F4),
            onSecondaryContainer = Color(0xFF292032),

            tertiary = Color(0xFF626C68),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFDDE7E1),
            onTertiaryContainer = Color(0xFF202A25),

            background = Color(0xFFF7F9FC),
            onBackground = Color(0xFF1A1D22),
            surface = Color(0xFFF7F9FC),
            onSurface = Color(0xFF1A1D22),
            surfaceVariant = Color(0xFFE1E6EC),
            onSurfaceVariant = Color(0xFF444B53),
            surfaceTint = Color(0xFF596B80),

            inverseSurface = Color(0xFF2C3037),
            inverseOnSurface = Color(0xFFEFF1F6),
            inversePrimary = Color(0xFFBCCCE0),

            outline = Color(0xFF747C86),
            outlineVariant = Color(0xFFC7CFD9),

            error = Color(0xFFB74B55),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDADD),
            onErrorContainer = Color(0xFF410009),

            surfaceBright = Color(0xFFF7F9FC),
            surfaceDim = Color(0xFFDFE4EB),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF0F3F8),
            surfaceContainer = Color(0xFFE9EDF3),
            surfaceContainerHigh = Color(0xFFDFE5EC),
            surfaceContainerHighest = Color(0xFFD6DDE5)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFBCCCE0),
            onPrimary = Color(0xFF293B50),
            primaryContainer = Color(0xFF40546A),
            onPrimaryContainer = Color(0xFFDCE6F4),

            secondary = Color(0xFFD0C2DA),
            onSecondary = Color(0xFF3B3044),
            secondaryContainer = Color(0xFF504358),
            onSecondaryContainer = Color(0xFFECE0F4),

            tertiary = Color(0xFFC1CEC7),
            onTertiary = Color(0xFF303B35),
            tertiaryContainer = Color(0xFF46514B),
            onTertiaryContainer = Color(0xFFDDE7E1),

            background = Color(0xFF171B21),
            onBackground = Color(0xFFE1E5EB),
            surface = Color(0xFF171B21),
            onSurface = Color(0xFFE1E5EB),
            surfaceVariant = Color(0xFF414852),
            onSurfaceVariant = Color(0xFFC5CCD6),
            surfaceTint = Color(0xFFBCCCE0),

            inverseSurface = Color(0xFFE1E5EB),
            inverseOnSurface = Color(0xFF2C3037),
            inversePrimary = Color(0xFF596B80),

            outline = Color(0xFF8A929D),
            outlineVariant = Color(0xFF414852),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF363B44),
            surfaceDim = Color(0xFF11151A),
            surfaceContainerLowest = Color(0xFF101419),
            surfaceContainerLow = Color(0xFF1F242B),
            surfaceContainer = Color(0xFF252A31),
            surfaceContainerHigh = Color(0xFF30353D),
            surfaceContainerHighest = Color(0xFF3C414A)
        )
    ),

    // 9. NATURE — Pine, Meadow and Sage
    NATURE(
        displayName = "Nature",
        light = lightColorScheme(
            primary = Color(0xFF456B45),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD5EBD0),
            onPrimaryContainer = Color(0xFF18351D),

            secondary = Color(0xFF6D6750),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFECE4C9),
            onSecondaryContainer = Color(0xFF292515),

            tertiary = Color(0xFF47716B),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFCDECE5),
            onTertiaryContainer = Color(0xFF102E29),

            background = Color(0xFFF7FAF3),
            onBackground = Color(0xFF1B2019),
            surface = Color(0xFFF7FAF3),
            onSurface = Color(0xFF1B2019),
            surfaceVariant = Color(0xFFE1E8DB),
            onSurfaceVariant = Color(0xFF434B40),
            surfaceTint = Color(0xFF456B45),

            inverseSurface = Color(0xFF2C332A),
            inverseOnSurface = Color(0xFFEEF2E8),
            inversePrimary = Color(0xFFB5D5AD),

            outline = Color(0xFF74816F),
            outlineVariant = Color(0xFFC6D2C0),

            error = Color(0xFFB74749),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD8),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFF7FAF3),
            surfaceDim = Color(0xFFDDE6D8),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFEEF5E9),
            surfaceContainer = Color(0xFFE7EFE1),
            surfaceContainerHigh = Color(0xFFDDE7D7),
            surfaceContainerHighest = Color(0xFFD4DECE)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB5D5AD),
            onPrimary = Color(0xFF28452A),
            primaryContainer = Color(0xFF3B583B),
            onPrimaryContainer = Color(0xFFD5EBD0),

            secondary = Color(0xFFD4CBA9),
            onSecondary = Color(0xFF3B3622),
            secondaryContainer = Color(0xFF534C35),
            onSecondaryContainer = Color(0xFFECE4C9),

            tertiary = Color(0xFFAED5CC),
            onTertiary = Color(0xFF244640),
            tertiaryContainer = Color(0xFF365A53),
            onTertiaryContainer = Color(0xFFCDECE5),

            background = Color(0xFF171D17),
            onBackground = Color(0xFFE0E7DC),
            surface = Color(0xFF171D17),
            onSurface = Color(0xFFE0E7DC),
            surfaceVariant = Color(0xFF414B40),
            onSurfaceVariant = Color(0xFFC4D1BF),
            surfaceTint = Color(0xFFB5D5AD),

            inverseSurface = Color(0xFFE0E7DC),
            inverseOnSurface = Color(0xFF2B332A),
            inversePrimary = Color(0xFF456B45),

            outline = Color(0xFF879783),
            outlineVariant = Color(0xFF414B40),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF343D33),
            surfaceDim = Color(0xFF101610),
            surfaceContainerLowest = Color(0xFF0F150F),
            surfaceContainerLow = Color(0xFF1F281F),
            surfaceContainer = Color(0xFF252E25),
            surfaceContainerHigh = Color(0xFF303930),
            surfaceContainerHighest = Color(0xFF3C453C)
        )
    ),

    // 10. NOTHING — Monochrome, White and Red
    NOTHING(
        displayName = "Nothing",
        light = lightColorScheme(
            primary = Color(0xFF292929),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE7E7E7),
            onPrimaryContainer = Color(0xFF171717),

            secondary = Color(0xFFB82F38),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDADD),
            onSecondaryContainer = Color(0xFF410009),

            tertiary = Color(0xFF555555),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFECECEC),
            onTertiaryContainer = Color(0xFF252525),

            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF202020),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF202020),
            surfaceVariant = Color(0xFFE3E3E3),
            onSurfaceVariant = Color(0xFF414141),
            surfaceTint = Color(0xFF292929),

            inverseSurface = Color(0xFF303030),
            inverseOnSurface = Color(0xFFF5F5F5),
            inversePrimary = Color(0xFFC9C9C9),

            outline = Color(0xFF747474),
            outlineVariant = Color(0xFFCCCCCC),

            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),

            surfaceBright = Color(0xFFFFFFFF),
            surfaceDim = Color(0xFFE0E0E0),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF5F5F5),
            surfaceContainer = Color(0xFFEEEEEE),
            surfaceContainerHigh = Color(0xFFE6E6E6),
            surfaceContainerHighest = Color(0xFFDDDDDD)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE7E7E7),
            onPrimary = Color(0xFF252525),
            primaryContainer = Color(0xFF454545),
            onPrimaryContainer = Color(0xFFF8F8F8),

            secondary = Color(0xFFFF8A8F),
            onSecondary = Color(0xFF56000A),
            secondaryContainer = Color(0xFF7C1721),
            onSecondaryContainer = Color(0xFFFFDADD),

            tertiary = Color(0xFFC7C7C7),
            onTertiary = Color(0xFF292929),
            tertiaryContainer = Color(0xFF414141),
            onTertiaryContainer = Color(0xFFECECEC),

            background = Color(0xFF101010),
            onBackground = Color(0xFFF3F3F3),
            surface = Color(0xFF191919),
            onSurface = Color(0xFFF3F3F3),
            surfaceVariant = Color(0xFF3A3A3A),
            onSurfaceVariant = Color(0xFFD1D1D1),
            surfaceTint = Color(0xFFE7E7E7),

            inverseSurface = Color(0xFFECECEC),
            inverseOnSurface = Color(0xFF252525),
            inversePrimary = Color(0xFF292929),

            outline = Color(0xFF8B8B8B),
            outlineVariant = Color(0xFF414141),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF353535),
            surfaceDim = Color(0xFF0B0B0B),
            surfaceContainerLowest = Color(0xFF080808),
            surfaceContainerLow = Color(0xFF202020),
            surfaceContainer = Color(0xFF272727),
            surfaceContainerHigh = Color(0xFF323232),
            surfaceContainerHighest = Color(0xFF3E3E3E)
        )
    ),

    // 11. PIXEL — Clean Blue, Peach and Green
    PIXEL(
        displayName = "Pixel",
        light = lightColorScheme(
            primary = Color(0xFF3867A8),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD8E6FF),
            onPrimaryContainer = Color(0xFF102D53),

            secondary = Color(0xFF9A5845),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDCCF),
            onSecondaryContainer = Color(0xFF3D1B0D),

            tertiary = Color(0xFF47764F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFD5F0D4),
            onTertiaryContainer = Color(0xFF15351C),

            background = Color(0xFFF7F9FF),
            onBackground = Color(0xFF191D24),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF191D24),
            surfaceVariant = Color(0xFFE0E7F1),
            onSurfaceVariant = Color(0xFF424B58),
            surfaceTint = Color(0xFF3867A8),

            inverseSurface = Color(0xFF2B3038),
            inverseOnSurface = Color(0xFFEEF1F8),
            inversePrimary = Color(0xFFB6CFF8),

            outline = Color(0xFF727D8C),
            outlineVariant = Color(0xFFC5CEDC),

            error = Color(0xFFB74749),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD8),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFFFFFFF),
            surfaceDim = Color(0xFFE0E5ED),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF0F4FA),
            surfaceContainer = Color(0xFFE9EEF6),
            surfaceContainerHigh = Color(0xFFE0E6EF),
            surfaceContainerHighest = Color(0xFFD7DEE8)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB6CFF8),
            onPrimary = Color(0xFF203D6B),
            primaryContainer = Color(0xFF304F7F),
            onPrimaryContainer = Color(0xFFD8E6FF),

            secondary = Color(0xFFF0BCA9),
            onSecondary = Color(0xFF502B1F),
            secondaryContainer = Color(0xFF704333),
            onSecondaryContainer = Color(0xFFFFDCCF),

            tertiary = Color(0xFFB9DAB9),
            onTertiary = Color(0xFF27452D),
            tertiaryContainer = Color(0xFF3A5D40),
            onTertiaryContainer = Color(0xFFD5F0D4),

            background = Color(0xFF171B21),
            onBackground = Color(0xFFE1E5ED),
            surface = Color(0xFF1C2026),
            onSurface = Color(0xFFE1E5ED),
            surfaceVariant = Color(0xFF414954),
            onSurfaceVariant = Color(0xFFC4CCD8),
            surfaceTint = Color(0xFFB6CFF8),

            inverseSurface = Color(0xFFE1E5ED),
            inverseOnSurface = Color(0xFF2A2E35),
            inversePrimary = Color(0xFF3867A8),

            outline = Color(0xFF8993A0),
            outlineVariant = Color(0xFF414954),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF373D45),
            surfaceDim = Color(0xFF111419),
            surfaceContainerLowest = Color(0xFF101318),
            surfaceContainerLow = Color(0xFF20252C),
            surfaceContainer = Color(0xFF262B32),
            surfaceContainerHigh = Color(0xFF31363E),
            surfaceContainerHighest = Color(0xFF3D424A)
        )
    ),

    // 12. RAINBOW — Golden Yellow, Coral and Sky
    RAINBOW(
        displayName = "Rainbow",
        light = lightColorScheme(
            primary = Color(0xFF805D24),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFE5B0),
            onPrimaryContainer = Color(0xFF302000),

            secondary = Color(0xFFB84F49),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFDAD5),
            onSecondaryContainer = Color(0xFF410A07),

            tertiary = Color(0xFF3E7480),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFC8F0FA),
            onTertiaryContainer = Color(0xFF092E36),

            background = Color(0xFFFFFAF4),
            onBackground = Color(0xFF211B15),
            surface = Color(0xFFFFFAF4),
            onSurface = Color(0xFF211B15),
            surfaceVariant = Color(0xFFF0E3D2),
            onSurfaceVariant = Color(0xFF514538),
            surfaceTint = Color(0xFF805D24),

            inverseSurface = Color(0xFF342C23),
            inverseOnSurface = Color(0xFFF7EDE0),
            inversePrimary = Color(0xFFEBCB8A),

            outline = Color(0xFF837567),
            outlineVariant = Color(0xFFD8C8B5),

            error = Color(0xFFB74645),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD8),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFFFFAF4),
            surfaceDim = Color(0xFFE8DCCB),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAF0E3),
            surfaceContainer = Color(0xFFF2E7D9),
            surfaceContainerHigh = Color(0xFFE9DDCE),
            surfaceContainerHighest = Color(0xFFDFD3C4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFEBCB8A),
            onPrimary = Color(0xFF493612),
            primaryContainer = Color(0xFF624A21),
            onPrimaryContainer = Color(0xFFFFE5B0),

            secondary = Color(0xFFF1B5AF),
            onSecondary = Color(0xFF5A211D),
            secondaryContainer = Color(0xFF793632),
            onSecondaryContainer = Color(0xFFFFDAD5),

            tertiary = Color(0xFFAEDDE8),
            onTertiary = Color(0xFF20434B),
            tertiaryContainer = Color(0xFF365C65),
            onTertiaryContainer = Color(0xFFC8F0FA),

            background = Color(0xFF1C1813),
            onBackground = Color(0xFFECE2D6),
            surface = Color(0xFF211C17),
            onSurface = Color(0xFFECE2D6),
            surfaceVariant = Color(0xFF4A4034),
            onSurfaceVariant = Color(0xFFD4C5B4),
            surfaceTint = Color(0xFFEBCB8A),

            inverseSurface = Color(0xFFECE2D6),
            inverseOnSurface = Color(0xFF302920),
            inversePrimary = Color(0xFF805D24),

            outline = Color(0xFF9A8B79),
            outlineVariant = Color(0xFF4A4034),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF40372C),
            surfaceDim = Color(0xFF15110D),
            surfaceContainerLowest = Color(0xFF14100C),
            surfaceContainerLow = Color(0xFF262019),
            surfaceContainer = Color(0xFF2C251E),
            surfaceContainerHigh = Color(0xFF373027),
            surfaceContainerHighest = Color(0xFF433B31)
        )
    ),

    // 13. SUNSET — Peach, Rose and Warm Amber
    SUNSET(
        displayName = "Sunset",
        light = lightColorScheme(
            primary = Color(0xFFAA553A),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDED2),
            onPrimaryContainer = Color(0xFF421B0E),

            secondary = Color(0xFF98516A),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E5),
            onSecondaryContainer = Color(0xFF3D1024),

            tertiary = Color(0xFF81602C),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFFFE2B2),
            onTertiaryContainer = Color(0xFF302000),

            background = Color(0xFFFFF8F5),
            onBackground = Color(0xFF261A17),
            surface = Color(0xFFFFF8F5),
            onSurface = Color(0xFF261A17),
            surfaceVariant = Color(0xFFF2E0D9),
            onSurfaceVariant = Color(0xFF55423B),
            surfaceTint = Color(0xFFAA553A),

            inverseSurface = Color(0xFF392923),
            inverseOnSurface = Color(0xFFFFEEE7),
            inversePrimary = Color(0xFFF0B9A5),

            outline = Color(0xFF8A7167),
            outlineVariant = Color(0xFFDCC6BC),

            error = Color(0xFFB74649),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD8),
            onErrorContainer = Color(0xFF410006),

            surfaceBright = Color(0xFFFFF8F5),
            surfaceDim = Color(0xFFEBD9D1),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFBEEE8),
            surfaceContainer = Color(0xFFF3E5DE),
            surfaceContainerHigh = Color(0xFFEADBD4),
            surfaceContainerHighest = Color(0xFFE1D2CA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF0B9A5),
            onPrimary = Color(0xFF572C1E),
            primaryContainer = Color(0xFF77432F),
            onPrimaryContainer = Color(0xFFFFDED2),

            secondary = Color(0xFFF0B5C9),
            onSecondary = Color(0xFF57263A),
            secondaryContainer = Color(0xFF733D52),
            onSecondaryContainer = Color(0xFFFFD9E5),

            tertiary = Color(0xFFE8C58B),
            onTertiary = Color(0xFF493516),
            tertiaryContainer = Color(0xFF634A25),
            onTertiaryContainer = Color(0xFFFFE2B2),

            background = Color(0xFF211713),
            onBackground = Color(0xFFF1E0D9),
            surface = Color(0xFF261B17),
            onSurface = Color(0xFFF1E0D9),
            surfaceVariant = Color(0xFF51403A),
            onSurfaceVariant = Color(0xFFDCC6BD),
            surfaceTint = Color(0xFFF0B9A5),

            inverseSurface = Color(0xFFF1E0D9),
            inverseOnSurface = Color(0xFF352721),
            inversePrimary = Color(0xFFAA553A),

            outline = Color(0xFF9C867C),
            outlineVariant = Color(0xFF51403A),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF42332B),
            surfaceDim = Color(0xFF17100D),
            surfaceContainerLowest = Color(0xFF160F0C),
            surfaceContainerLow = Color(0xFF2A1F19),
            surfaceContainer = Color(0xFF30241E),
            surfaceContainerHigh = Color(0xFF3B2F28),
            surfaceContainerHighest = Color(0xFF473A32)
        )
    ),

    // 14. VIOLET — Amethyst, Lavender and Orchid
    VIOLET(
        displayName = "Violet",
        light = lightColorScheme(
            primary = Color(0xFF7653A1),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEEDDFF),
            onPrimaryContainer = Color(0xFF2A154B),

            secondary = Color(0xFF80556E),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFD9E9),
            onSecondaryContainer = Color(0xFF361126),

            tertiary = Color(0xFF675F92),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE6DFFF),
            onTertiaryContainer = Color(0xFF211849),

            background = Color(0xFFFAF7FF),
            onBackground = Color(0xFF201A27),
            surface = Color(0xFFFAF7FF),
            onSurface = Color(0xFF201A27),
            surfaceVariant = Color(0xFFEAE0F1),
            onSurfaceVariant = Color(0xFF4D4255),
            surfaceTint = Color(0xFF7653A1),

            inverseSurface = Color(0xFF332B3A),
            inverseOnSurface = Color(0xFFF3EAF9),
            inversePrimary = Color(0xFFD4B8F4),

            outline = Color(0xFF7F7289),
            outlineVariant = Color(0xFFD2C5DE),

            error = Color(0xFFB74753),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDADF),
            onErrorContainer = Color(0xFF410009),

            surfaceBright = Color(0xFFFAF7FF),
            surfaceDim = Color(0xFFE4D9ED),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF5EDF9),
            surfaceContainer = Color(0xFFEFE6F5),
            surfaceContainerHigh = Color(0xFFE6DCEC),
            surfaceContainerHighest = Color(0xFFDDD3E5)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD4B8F4),
            onPrimary = Color(0xFF3D285B),
            primaryContainer = Color(0xFF574071),
            onPrimaryContainer = Color(0xFFEEDDFF),

            secondary = Color(0xFFE7B9D0),
            onSecondary = Color(0xFF51263E),
            secondaryContainer = Color(0xFF6B3D54),
            onSecondaryContainer = Color(0xFFFFD9E9),

            tertiary = Color(0xFFC9BDF0),
            onTertiary = Color(0xFF34295B),
            tertiaryContainer = Color(0xFF4C4074),
            onTertiaryContainer = Color(0xFFE6DFFF),

            background = Color(0xFF1D1724),
            onBackground = Color(0xFFEAE0F0),
            surface = Color(0xFF231B2B),
            onSurface = Color(0xFFEAE0F0),
            surfaceVariant = Color(0xFF4A3E53),
            onSurfaceVariant = Color(0xFFD0C1DD),
            surfaceTint = Color(0xFFD4B8F4),

            inverseSurface = Color(0xFFEAE0F0),
            inverseOnSurface = Color(0xFF302736),
            inversePrimary = Color(0xFF7653A1),

            outline = Color(0xFF9485A0),
            outlineVariant = Color(0xFF4A3E53),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            surfaceBright = Color(0xFF3D3346),
            surfaceDim = Color(0xFF16111C),
            surfaceContainerLowest = Color(0xFF15101B),
            surfaceContainerLow = Color(0xFF261E2E),
            surfaceContainer = Color(0xFF2C2334),
            surfaceContainerHigh = Color(0xFF372D40),
            surfaceContainerHighest = Color(0xFF43384C)
        )
    )
}