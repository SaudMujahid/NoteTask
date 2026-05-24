package com.example.test.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.Typography
@Composable
fun TestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paletteIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val palette = AppPalettes.getOrElse(paletteIndex) { AppPalettes[0] }
    val colorScheme = if (darkTheme) palette.dark else palette.light

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // background instead of primary — looks correct in both light and dark
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}