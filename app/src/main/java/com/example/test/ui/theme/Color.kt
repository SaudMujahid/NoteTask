package com.example.test.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val BackgroundGray  = Color(0xFFE8EAF0)
val CardWhite       = Color(0xFFFFFBF2)
val PrimaryBlue     = Color(0xFF5C6BC0)
val BorderBlue      = Color(0xFF3F51B5)
val BorderGreen     = Color(0xFF4CAF50)
val BottomBarBlue   = Color(0xFF5C6BC0)

val ChipHealthBg    = Color(0xFFE8EAF6)
val ChipHealthText  = Color(0xFF7986CB)
val ChipWorkBg      = Color(0xFFE8F5E9)
val ChipWorkText    = Color(0xFF4CAF50)
val ChipMentalBg    = Color(0xFFFCE4EC)
val ChipMentalText  = Color(0xFFEC407A)

val PendingBadgeBg      = Color(0xFFFFF4CC)
val PendingBadgeBorder  = Color(0xFFC99700)
val CompletedBadgeBg    = Color(0xFFE3F2FD)
val CompletedBadgeBorder= Color(0xFF1565C0)

// Filter Badge Colors
val FilterBadgeBg = Color(0xFFF6F7F9)
val FilterBadgeBorder = Color(0xFFD6DBE3)

// Auth Screen Colors — Green & Yellow theme
val AuthBgGreen      = Color(0xFFf0f7eb)
val AuthHeaderGreen  = Color(0xFF2d7a22)
val AuthMediumGreen  = Color(0xFF4aad3a)
val AuthInputBg      = Color(0xFFf6fbf3)
val AuthInputBorder  = Color(0xFFc8e6b4)
val AuthHintGreen    = Color(0xFFb8d4a8)
val AuthMutedGreen   = Color(0xFF9bbf8e)
val AuthCardWhite    = Color(0xFFFFFFFF)
val AuthYellowBg     = Color(0xFFfffbea)
val AuthYellowBorder = Color(0xFFFFE566)
val AuthYellowText   = Color(0xFF9a7c00)
val AuthDivider      = Color(0xFFEAF4E4)

val LoginBgGray  = Color(0xFFE2E4EC)
val DarkNavy     = Color(0xFF1A1040)
val CardCream    = Color(0xFFFAF6EF)
val InputDark    = Color(0xFF1E1E2E)
val InputBorder  = Color(0xFF3A2F6E)
val Purple       = Color(0xFF6B2EFF)
val PurpleLight  = Color(0xFFD4C5FF)
val PurpleBubble = Color(0xFFC9BFFF)
val TextGray     = Color(0xFF888888)
val SignUpCardBg = Color(0xFFEAE8F0)

val SubtaskIndent = 32.dp

// ─────────────────────────────────────────────────────────────────────────────
// App Palettes  — 5 hand-crafted light + dark colour schemes
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One entry per selectable palette. [swatch] is shown as the circle in the picker.
 */
data class AppPalette(
    val name: String,
    val swatch: Color,
    val light: androidx.compose.material3.ColorScheme,
    val dark:  androidx.compose.material3.ColorScheme
)

val AppPalettes: List<AppPalette> = listOf(

    // 0 — Indigo (default, close to the original purple)
    AppPalette(
        name   = "Indigo",
        swatch = Color(0xFF5C6BC0),
        light  = lightColorScheme(
            primary            = Color(0xFF5C6BC0),
            onPrimary          = Color(0xFFFFFFFF),
            primaryContainer   = Color(0xFFDDE1FF),
            onPrimaryContainer = Color(0xFF00105C),
            secondary          = Color(0xFF5D5B72),
            onSecondary        = Color(0xFFFFFFFF),
            background         = Color(0xFFF4F4FB),
            onBackground       = Color(0xFF1A1B27),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF1A1B27),
            surfaceVariant     = Color(0xFFE4E1F0),
            onSurfaceVariant   = Color(0xFF47475E),
            outline            = Color(0xFF78788E),
            error              = Color(0xFFBA1A1A),
            onError            = Color(0xFFFFFFFF),
        ),
        dark   = darkColorScheme(
            primary            = Color(0xFFBEC2FF),
            onPrimary          = Color(0xFF1A2678),
            primaryContainer   = Color(0xFF3F4FAF),
            onPrimaryContainer = Color(0xFFDDE1FF),
            secondary          = Color(0xFFC6C3DE),
            onSecondary        = Color(0xFF2E2D42),
            background         = Color(0xFF121218),
            onBackground       = Color(0xFFE5E5F0),
            surface            = Color(0xFF1E1E2A),
            onSurface          = Color(0xFFE5E5F0),
            surfaceVariant     = Color(0xFF2A2A38),
            onSurfaceVariant   = Color(0xFFC8C5DC),
            outline            = Color(0xFF928FA6),
            error              = Color(0xFFFFB4AB),
            onError            = Color(0xFF690005),
        )
    ),

    // 1 — Ocean
    AppPalette(
        name   = "Ocean",
        swatch = Color(0xFF0077B6),
        light  = lightColorScheme(
            primary            = Color(0xFF0077B6),
            onPrimary          = Color(0xFFFFFFFF),
            primaryContainer   = Color(0xFFCCEAFF),
            onPrimaryContainer = Color(0xFF001F3A),
            secondary          = Color(0xFF4D7A94),
            onSecondary        = Color(0xFFFFFFFF),
            background         = Color(0xFFF0F7FC),
            onBackground       = Color(0xFF0D1C26),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF0D1C26),
            surfaceVariant     = Color(0xFFD6E8F4),
            onSurfaceVariant   = Color(0xFF3A5566),
            outline            = Color(0xFF5C7D90),
            error              = Color(0xFFBA1A1A),
            onError            = Color(0xFFFFFFFF),
        ),
        dark   = darkColorScheme(
            primary            = Color(0xFF6DD3FF),
            onPrimary          = Color(0xFF003550),
            primaryContainer   = Color(0xFF005478),
            onPrimaryContainer = Color(0xFFCCEAFF),
            secondary          = Color(0xFFAAD0E8),
            onSecondary        = Color(0xFF1A3A4D),
            background         = Color(0xFF0D1519),
            onBackground       = Color(0xFFD8EEF9),
            surface            = Color(0xFF161E24),
            onSurface          = Color(0xFFD8EEF9),
            surfaceVariant     = Color(0xFF1E2D36),
            onSurfaceVariant   = Color(0xFFB0CDD9),
            outline            = Color(0xFF7A9EAF),
            error              = Color(0xFFFFB4AB),
            onError            = Color(0xFF690005),
        )
    ),

    // 2 — Forest
    AppPalette(
        name   = "Forest",
        swatch = Color(0xFF2D6A4F),
        light  = lightColorScheme(
            primary            = Color(0xFF2D6A4F),
            onPrimary          = Color(0xFFFFFFFF),
            primaryContainer   = Color(0xFFB7E4CA),
            onPrimaryContainer = Color(0xFF002114),
            secondary          = Color(0xFF52796F),
            onSecondary        = Color(0xFFFFFFFF),
            background         = Color(0xFFF0F7F2),
            onBackground       = Color(0xFF0D1F15),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF0D1F15),
            surfaceVariant     = Color(0xFFD4EAD8),
            onSurfaceVariant   = Color(0xFF3A5546),
            outline            = Color(0xFF5C7E68),
            error              = Color(0xFFBA1A1A),
            onError            = Color(0xFFFFFFFF),
        ),
        dark   = darkColorScheme(
            primary            = Color(0xFF6DCEA2),
            onPrimary          = Color(0xFF003824),
            primaryContainer   = Color(0xFF1A5239),
            onPrimaryContainer = Color(0xFFB7E4CA),
            secondary          = Color(0xFFA0C4B4),
            onSecondary        = Color(0xFF1A332A),
            background         = Color(0xFF0D1510),
            onBackground       = Color(0xFFD4ECD9),
            surface            = Color(0xFF141E17),
            onSurface          = Color(0xFFD4ECD9),
            surfaceVariant     = Color(0xFF1C2E22),
            onSurfaceVariant   = Color(0xFFB2CDBA),
            outline            = Color(0xFF78A086),
            error              = Color(0xFFFFB4AB),
            onError            = Color(0xFF690005),
        )
    ),

    // 3 — Sunset
    AppPalette(
        name   = "Sunset",
        swatch = Color(0xFFE05C2A),
        light  = lightColorScheme(
            primary            = Color(0xFFE05C2A),
            onPrimary          = Color(0xFFFFFFFF),
            primaryContainer   = Color(0xFFFFDBC9),
            onPrimaryContainer = Color(0xFF3B0D00),
            secondary          = Color(0xFF8C5544),
            onSecondary        = Color(0xFFFFFFFF),
            background         = Color(0xFFFFF8F5),
            onBackground       = Color(0xFF231008),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF231008),
            surfaceVariant     = Color(0xFFFFDFCF),
            onSurfaceVariant   = Color(0xFF573327),
            outline            = Color(0xFF8D5A4D),
            error              = Color(0xFFBA1A1A),
            onError            = Color(0xFFFFFFFF),
        ),
        dark   = darkColorScheme(
            primary            = Color(0xFFFFB59A),
            onPrimary          = Color(0xFF5A1A00),
            primaryContainer   = Color(0xFFB54110),
            onPrimaryContainer = Color(0xFFFFDBC9),
            secondary          = Color(0xFFE8BAA9),
            onSecondary        = Color(0xFF3B1A10),
            background         = Color(0xFF1A1009),
            onBackground       = Color(0xFFFFDBC9),
            surface            = Color(0xFF251510),
            onSurface          = Color(0xFFFFDBC9),
            surfaceVariant     = Color(0xFF332018),
            onSurfaceVariant   = Color(0xFFD9BFB1),
            outline            = Color(0xFFA67A6A),
            error              = Color(0xFFFFB4AB),
            onError            = Color(0xFF690005),
        )
    ),

    // 4 — Rose
    AppPalette(
        name   = "Rose",
        swatch = Color(0xFFC2185B),
        light  = lightColorScheme(
            primary            = Color(0xFFC2185B),
            onPrimary          = Color(0xFFFFFFFF),
            primaryContainer   = Color(0xFFFFD9E6),
            onPrimaryContainer = Color(0xFF3E001A),
            secondary          = Color(0xFF7A4F60),
            onSecondary        = Color(0xFFFFFFFF),
            background         = Color(0xFFFFF8F9),
            onBackground       = Color(0xFF22101A),
            surface            = Color(0xFFFFFFFF),
            onSurface          = Color(0xFF22101A),
            surfaceVariant     = Color(0xFFFFD9E6),
            onSurfaceVariant   = Color(0xFF57343F),
            outline            = Color(0xFF8D5767),
            error              = Color(0xFFBA1A1A),
            onError            = Color(0xFFFFFFFF),
        ),
        dark   = darkColorScheme(
            primary            = Color(0xFFFFB1CB),
            onPrimary          = Color(0xFF67002D),
            primaryContainer   = Color(0xFF960042),
            onPrimaryContainer = Color(0xFFFFD9E6),
            secondary          = Color(0xFFE9B9C8),
            onSecondary        = Color(0xFF3E1A28),
            background         = Color(0xFF1A0E14),
            onBackground       = Color(0xFFFFD9E6),
            surface            = Color(0xFF24141C),
            onSurface          = Color(0xFFFFD9E6),
            surfaceVariant     = Color(0xFF321B26),
            onSurfaceVariant   = Color(0xFFD9BECA),
            outline            = Color(0xFFA67A88),
            error              = Color(0xFFFFB4AB),
            onError            = Color(0xFF690005),
        )
    ),
)