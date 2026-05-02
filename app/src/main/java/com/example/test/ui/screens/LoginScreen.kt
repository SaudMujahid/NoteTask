package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.*
import com.example.test.ui.viewmodels.AuthState
import com.example.test.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    isDarkTheme: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by (authViewModel?.authState?.collectAsState()
        ?: remember { mutableStateOf(AuthState.Idle) })

    val colors = rememberAuthColors(isDarkTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBgGreen)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Green header ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuthHeaderGreen)
                .padding(top = 72.dp, bottom = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Log in to your account",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // ── White card slides over header ─────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-22).dp),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Email
                FieldLabel("EMAIL ADDRESS")
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("you@email.com", color = AuthHintGreen) },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = AuthMediumGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = authFieldColors(colors),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // Password
                FieldLabel("PASSWORD")
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = AuthHintGreen) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = AuthMediumGreen) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                null,
                                tint = AuthHintGreen
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = authFieldColors(colors),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                // Forgot password
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = {}, contentPadding = PaddingValues(vertical = 4.dp)) {
                        Text(
                            "Forgot Password?",
                            color = AuthHeaderGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Error state
                if (authState is AuthState.Error) {
                    Text(
                        (authState as AuthState.Error).message,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }

                // Login button
                Button(
                    onClick = {
                        if (authViewModel != null) authViewModel.login(email, password) { onLoginClick() }
                        else onLoginClick()
                    },
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuthHeaderGreen)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            "LOG IN",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Divider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Divider(Modifier.weight(1f), color = AuthDivider)
                    Text("  or  ", color = AuthHintGreen, fontSize = 11.sp)
                    Divider(Modifier.weight(1f), color = AuthDivider)
                }

                Spacer(Modifier.height(14.dp))

                // Google button
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AuthYellowBg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AuthYellowBorder)
                ) {
                    Text(
                        "Continue with Google",
                        color = AuthYellowText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Sign up link
                TextButton(onClick = onSignUpClick) {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(color = AuthMutedGreen, fontSize = 13.sp)) {
                            append("Don't have an account? ")
                        }
                        withStyle(SpanStyle(color = AuthHeaderGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                            append("Sign Up")
                        }
                    })
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme { LoginScreen() }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = AuthHeaderGreen,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.fillMaxWidth()
    )
}
