package com.example.test.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Full-screen drawer overlay (scrim + slide-in panel).
 *
 * @param isOpen        Whether the drawer is currently visible.
 * @param firstName     Displayed in the greeting. Empty string → "Hello!"
 * @param isDarkTheme   Controls the light/dark mode toggle label and icon.
 * @param isLoggedIn    When true shows "Log Out"; when false shows "Log In".
 * @param onClose       Called when the scrim or close button is tapped.
 * @param onToggleDarkMode Called when the theme toggle item is tapped.
 * @param onAuthAction  Called for both login and logout — the caller decides
 *                      what to do based on [isLoggedIn].
 */
@Composable
fun DrawerMenu(
    isOpen: Boolean,
    firstName: String,
    isDarkTheme: Boolean,
    isLoggedIn: Boolean,
    onClose: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onAuthAction: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Scrim ──────────────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onClose() }
        )
    }

    // ── Panel ──────────────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(11f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.72f)
                    .align(Alignment.CenterEnd)
                    .background(
                        color = colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .clickable { /* consume touches so scrim doesn't fire */ }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                ) {
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close menu",
                                tint = colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Greeting
                    Text(
                        text = if (firstName.isBlank()) "Hello!" else "Hi, $firstName 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    // Accent divider
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorScheme.primary)
                    )

                    Spacer(Modifier.height(36.dp))

                    // Dark / Light mode toggle
                    DrawerMenuItem(
                        label = if (isDarkTheme) "Light Mode" else "Dark Mode",
                        icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        iconTint = Color(0xFFFFD700),
                        textColor = colorScheme.onSurface,
                        onClick = {
                            onToggleDarkMode()
                            onClose()
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

                    // Login / Logout — label, icon and tint switch based on auth state
                    DrawerMenuItem(
                        label = if (isLoggedIn) "Log Out" else "Log In",
                        icon = if (isLoggedIn) Icons.Default.Logout else Icons.Default.Login,
                        iconTint = if (isLoggedIn) colorScheme.error else colorScheme.primary,
                        textColor = if (isLoggedIn) colorScheme.error else colorScheme.primary,
                        onClick = {
                            onClose()
                            onAuthAction()
                        }
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        "v1.0.0",
                        fontSize = 11.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ── Reusable menu row ────────────────────────────────────────────────────────

@Composable
fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}