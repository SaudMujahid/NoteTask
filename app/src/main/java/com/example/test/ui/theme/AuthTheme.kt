
package com.example.test.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class AuthColors(
    val background: Color,
    val card: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val headerText: Color,
    val primaryAccent: Color,
    val buttonBackground: Color,
    val hint: Color,
    val cardBorder: Color,
    val iconBackground: Color,
    val text: Color
)

@Composable
fun rememberAuthColors(isDarkTheme: Boolean): AuthColors {
    return remember(isDarkTheme) {
        AuthColors(
            background      = AuthBgGreen,
            card            = AuthCardWhite,
            inputBackground = AuthInputBg,
            inputBorder     = AuthInputBorder,
            headerText      = AuthCardWhite,
            primaryAccent   = AuthHeaderGreen,
            buttonBackground = AuthHeaderGreen,
            hint            = AuthHintGreen,
            cardBorder      = AuthInputBorder,
            iconBackground  = AuthHeaderGreen,
            text            = Color(0xFF1a1a1a)
        )
    }
}

@Composable
fun authFieldColors(colors: AuthColors) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = colors.inputBackground,
    unfocusedContainerColor = colors.inputBackground,
    focusedBorderColor      = AuthMediumGreen,
    unfocusedBorderColor    = colors.inputBorder,
    focusedTextColor        = colors.text,
    unfocusedTextColor      = colors.text,
    focusedPlaceholderColor = colors.hint,
    unfocusedPlaceholderColor = colors.hint,
    cursorColor             = AuthMediumGreen
)
