package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Create your\naccount",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.headerText,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Personal info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.headerText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = { Text("First name", color = colors.hint) },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = colors.primaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = authFieldColors(colors),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = { Text("Last name", color = colors.hint) },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = colors.primaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = authFieldColors(colors),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Account details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.headerText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email address", color = colors.hint) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = colors.primaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = authFieldColors(colors),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = colors.hint) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = colors.primaryAccent) },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    null,
                                    tint = colors.hint
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

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPw,
                        onValueChange = { confirmPw = it },
                        placeholder = { Text("Confirm password", color = colors.hint) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = colors.primaryAccent) },
                        trailingIcon = {
                            IconButton(
                                onClick = { confirmPwVisible = !confirmPwVisible },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (confirmPwVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    null,
                                    tint = colors.hint
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

                    Spacer(Modifier.height(16.dp))

                    if (authState is AuthState.Error) {
                        Text(
                            (authState as AuthState.Error).message,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (password != confirmPw) return@Button
                            authViewModel?.signUp(firstName, lastName, email, password) {
                                onSignUpClick()
                            } ?: onSignUpClick()
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground),
                        border = androidx.compose.foundation.BorderStroke(2.dp, colors.cardBorder)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = colors.primaryAccent,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign Up",
                                color = colors.primaryAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(colors.primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = onLoginClick) {
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.hint, fontSize = 15.sp)) {
                        append("Already have an account? ")
                    }
                    withStyle(
                        SpanStyle(
                            color = colors.primaryAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Log in")
                    }
                })
            }

            Spacer(Modifier.height(24.dp))
        }

        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.iconBackground)
        ) {
            Icon(
                if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme { SignUpScreen() }
}