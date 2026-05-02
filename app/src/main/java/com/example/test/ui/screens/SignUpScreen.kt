package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
fun SignUpScreen(
    isDarkTheme: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var firstName        by remember { mutableStateOf("") }
    var lastName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPw        by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmPwVisible by remember { mutableStateOf(false) }

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
                .padding(top = 64.dp, bottom = 52.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Sign up to get started",
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

                // Full Name
                FieldLabel("FULL NAME")
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("Your full name", color = AuthHintGreen) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = AuthMediumGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = authFieldColors(colors),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // Last Name
                FieldLabel("LAST NAME")
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Your last name", color = AuthHintGreen) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = AuthMediumGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = authFieldColors(colors),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

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

                Spacer(Modifier.height(16.dp))

                // Confirm Password
                FieldLabel("CONFIRM PASSWORD")
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = confirmPw,
                    onValueChange = { confirmPw = it },
                    placeholder = { Text("••••••••", color = AuthHintGreen) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = AuthMediumGreen) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPwVisible = !confirmPwVisible }) {
                            Icon(
                                if (confirmPwVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                null,
                                tint = AuthHintGreen
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = authFieldColors(colors),
                    visualTransformation = if (confirmPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                if (confirmPw.isNotEmpty() && password != confirmPw) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Passwords do not match",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(28.dp))

                if (authState is AuthState.Error) {
                    Text(
                        (authState as AuthState.Error).message,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }

                // Sign Up button
                Button(
                    onClick = {
                        if (password != confirmPw) return@Button
                        authViewModel?.signUp(firstName, lastName, email, password) { onSignUpClick() }
                            ?: onSignUpClick()
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
                            "SIGN UP",
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

                // Login link
                TextButton(onClick = onLoginClick) {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(color = AuthMutedGreen, fontSize = 13.sp)) {
                            append("Already have an account? ")
                        }
                        withStyle(SpanStyle(color = AuthHeaderGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
                            append("Log In")
                        }
                    })
                }
            }
        }
    }
}

// Shared label composable
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme { SignUpScreen() }
}
