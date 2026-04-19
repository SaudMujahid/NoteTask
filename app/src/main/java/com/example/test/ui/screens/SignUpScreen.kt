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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.example.test.ui.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    isDarkTheme: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPwVisible by remember { mutableStateOf(false) }

    // ── Dark mode palette ──
    val bgColor = if (isDarkTheme) Color(0xFF0F0F1A) else LoginBgGray
    val cardColor = if (isDarkTheme) Color(0xFF1A1A2E) else CardCream
    val inputBg = if (isDarkTheme) Color(0xFF252538) else InputDark
    val inputBorderColor = if (isDarkTheme) Color(0xFF3A3A5C) else InputBorder
    val headerTextColor = if (isDarkTheme) Color(0xFFE8E4FF) else DarkNavy
    val primaryAccent = if (isDarkTheme) Color(0xFF9B7DFF) else Purple
    val buttonBackground = if (isDarkTheme) Color(0xFF252538) else SignUpCardBg
    val hintColor = if (isDarkTheme) Color(0xFF8888AA) else TextGray
    val cardBorder = if (isDarkTheme) Color(0xFF3A3A5C) else DarkNavy

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ──
            Text(
                text = "Create your\naccount",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = headerTextColor,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Form Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, cardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Personal Info
                    Text(
                        text = "Personal info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = headerTextColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = { Text("First name", color = hintColor) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = primaryAccent
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                focusedBorderColor = primaryAccent,
                                unfocusedBorderColor = inputBorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = hintColor,
                                unfocusedPlaceholderColor = hintColor
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = { Text("Last name", color = hintColor) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                focusedBorderColor = primaryAccent,
                                unfocusedBorderColor = inputBorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = hintColor,
                                unfocusedPlaceholderColor = hintColor
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Account Details
                    Text(
                        text = "Account details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = headerTextColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email address", color = hintColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = primaryAccent
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = primaryAccent,
                            unfocusedBorderColor = inputBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = hintColor,
                            unfocusedPlaceholderColor = hintColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = hintColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = primaryAccent
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = hintColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = primaryAccent,
                            unfocusedBorderColor = inputBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = hintColor,
                            unfocusedPlaceholderColor = hintColor
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPw,
                        onValueChange = { confirmPw = it },
                        placeholder = { Text("Confirm password", color = hintColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = primaryAccent
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { confirmPwVisible = !confirmPwVisible },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (confirmPwVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (confirmPwVisible) "Hide password" else "Show password",
                                    tint = hintColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = primaryAccent,
                            unfocusedBorderColor = inputBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = hintColor,
                            unfocusedPlaceholderColor = hintColor
                        ),
                        visualTransformation = if (confirmPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign Up Button (fixed height, never shrinks)
                    Button(
                        onClick = {
                            if (authViewModel != null) {
                                authViewModel.signUp(firstName, lastName, email, password) {
                                    onSignUpClick()
                                }
                            } else {
                                onSignUpClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBackground),
                        border = androidx.compose.foundation.BorderStroke(2.dp, cardBorder)
                    ) {
                        Text(
                            text = "Sign Up",
                            color = primaryAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(primaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Login Link ──
            TextButton(onClick = onLoginClick) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = if (isDarkTheme) Color(0xFFAAAAAA) else Color.DarkGray, fontSize = 15.sp)) {
                            append("Already have an account? ")
                        }
                        withStyle(
                            SpanStyle(
                                color = primaryAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Log in")
                        }
                    }
                )
            }

            // Down arrow
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF3A3A5C) else Color(0xFF6B6B7E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll down",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Dark Mode Toggle (Top Right) ──
        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDarkTheme) Color(0xFF252538) else DarkNavy)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to dark mode",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen()
    }
}