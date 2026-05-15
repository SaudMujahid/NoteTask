package com.example.test.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.test.ui.theme.AppPalettes

@Composable
fun DrawerMenu(
    isOpen: Boolean,
    firstName: String,
    isDarkTheme: Boolean,
    isLoggedIn: Boolean = true,
    paletteIndex: Int = 0,
    onClose: () -> Unit,
    onToggleDarkMode: () -> Unit,
onTransferClick: () -> Unit = {},
    onAuthAction: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onPaletteChange: (Int) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    var showPalettePicker by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = isOpen, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize().zIndex(10f)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { showPalettePicker = false; onClose() })
    }

    AnimatedVisibility(visible = isOpen, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it }), modifier = Modifier.fillMaxSize().zIndex(11f)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.72f).align(Alignment.CenterEnd).background(color = colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)).clickable { }) {
                Column(modifier = Modifier.fillMaxSize().padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { showPalettePicker = false; onClose() }, modifier = Modifier.size(36.dp).clip(CircleShape).background(colorScheme.onSurface.copy(alpha = 0.1f))) {
                            Icon(Icons.Default.Close, contentDescription = "Close menu", tint = colorScheme.onSurface)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(text = if (firstName.isBlank()) "Hello!" else "Hi, $firstName 👋", fontSize = 22.sp, fontWeight = FontWeight.Black, color = colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(40.dp).height(3.dp).clip(RoundedCornerShape(50)).background(colorScheme.primary))
                    Spacer(Modifier.height(36.dp))

                    DrawerMenuItem(label = if (isDarkTheme) "Light Mode" else "Dark Mode", icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, iconTint = Color(0xFFFFD700), textColor = colorScheme.onSurface, onClick = { onToggleDarkMode(); onClose() })
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

                    DrawerMenuItem(label = "Colour Scheme", icon = Icons.Default.Palette, iconTint = colorScheme.primary, textColor = colorScheme.onSurface, onClick = { showPalettePicker = !showPalettePicker })
                    AnimatedVisibility(visible = showPalettePicker, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Column {
                            Spacer(Modifier.height(14.dp))
                            Text("Pick a palette", fontSize = 11.sp, color = colorScheme.onSurface.copy(alpha = 0.45f), modifier = Modifier.padding(start = 4.dp))
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 4.dp)) {
                                AppPalettes.forEachIndexed { index, palette ->
                                    val selected = index == paletteIndex
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(30.dp).clip(CircleShape).background(palette.swatch).then(if (selected) Modifier.border(2.5.dp, colorScheme.onSurface, CircleShape) else Modifier).clickable { onPaletteChange(index) }) {
                                        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

             DrawerMenuItem(icon = Icons.Default.BarChart, label = "Progress", iconTint = colorScheme.primary, textColor = colorScheme.onSurface, onClick = { onClose(); onStatsClick() })
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    DrawerMenuItem(icon = Icons.Default.SwapHoriz, label = "Transfer", iconTint = colorScheme.primary, textColor = colorScheme.onSurface, onClick = { onClose(); onTransferClick() })
                    Spacer(Modifier.weight(1f))
                    Text("v1.0.0", fontSize = 11.sp, color = colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(label: String, icon: ImageVector, iconTint: Color, textColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
