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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.*

// ── SignUpScreen ──────────────────────────────────────────────────────────────
@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBgGray)
    ) {
        // ··· menu button top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkNavy),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header Card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, DarkNavy, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardCream),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Create your\naccount",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy,
                        lineHeight = 32.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(PurpleBubble),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = DarkNavy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Personal Info Section ─────────────────────────────────────────
            SectionLabel("Personal info")
            Spacer(Modifier.height(10.dp))

            // First + Last name in one dark card, split by divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(InputDark)
                    .border(1.5.dp, InputBorder, RoundedCornerShape(16.dp))
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First name
                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicInputField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "First name"
                    )
                }
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(InputBorder)
                )
                // Last name
                Box(
                    modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicInputField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Last name"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Account Details Section ───────────────────────────────────────
            SectionLabel("Account details")
            Spacer(Modifier.height(10.dp))

            DarkInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email address",
                icon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(10.dp))
            DarkInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                icon = Icons.Outlined.Lock,
                isPassword = true
            )
            Spacer(Modifier.height(10.dp))
            DarkInputField(
                value = confirmPw,
                onValueChange = { confirmPw = it },
                placeholder = "Confirm password",
                icon = Icons.Outlined.Lock,
                isPassword = true
            )

            Spacer(Modifier.height(20.dp))

            // ── Sign Up Button ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SignUpCardBg)
                    .border(2.dp, DarkNavy, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onSignUpClick) {
                        Text(
                            text = "Sign Up",
                            color = Purple,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Purple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Sign Up",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Login Link ────────────────────────────────────────────────────
            TextButton(onClick = onLoginClick) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.DarkGray, fontSize = 15.sp)) {
                            append("Already have an account? ")
                        }
                        withStyle(SpanStyle(color = Purple, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)) {
                            append("Log in")
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Down arrow button ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6B6B7E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll down",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ── Reusable: Section Label ───────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkNavy
        )
    }
}

// ── Reusable: Dark Input Field ────────────────────────────────────────────────
@Composable
private fun DarkInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(InputDark)
            .border(1.5.dp, InputBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            keyboardType = keyboardType,
            isPassword = isPassword,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Reusable: Inline BasicTextField wrapper ───────────────────────────────────
@Composable
private fun BasicInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardType = keyboardType,
        isPassword = isPassword,
        modifier = modifier
    )
}

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 15.sp
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextGray,
                    fontSize = 15.sp
                )
            }
            innerTextField()
        }
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen()
    }
}