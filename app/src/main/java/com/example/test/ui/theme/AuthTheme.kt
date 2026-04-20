
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
        if (isDarkTheme) {
            AuthColors(
                background = Color(0xFF0F0F1A),
                card = Color(0xFF1A1A2E),
                inputBackground = Color(0xFF252538),
                inputBorder = Color(0xFF3A3A5C),
                headerText = Color(0xFFE8E4FF),
                primaryAccent = Color(0xFF9B7DFF),
                buttonBackground = Color(0xFF252538),
                hint = Color(0xFF8888AA),
                cardBorder = Color(0xFF3A3A5C),
                iconBackground = Color(0xFF252538),
                text = Color.White
            )
        } else {
            AuthColors(
                background = LoginBgGray,
                card = CardCream,
                inputBackground = Color.White,      // FIX: was InputDark
                inputBorder = InputBorder,
                headerText = DarkNavy,
                primaryAccent = Purple,
                buttonBackground = SignUpCardBg,
                hint = TextGray,
                cardBorder = DarkNavy,
                iconBackground = DarkNavy,
                text = DarkNavy                     // FIX: was Color.White
            )
        }
    }
}

@Composable
fun authFieldColors(colors: AuthColors) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = colors.inputBackground,
    unfocusedContainerColor = colors.inputBackground,
    focusedBorderColor = colors.primaryAccent,
    unfocusedBorderColor = colors.inputBorder,
    focusedTextColor = colors.text,
    unfocusedTextColor = colors.text,
    focusedPlaceholderColor = colors.hint,
    unfocusedPlaceholderColor = colors.hint,
    cursorColor = colors.primaryAccent
)