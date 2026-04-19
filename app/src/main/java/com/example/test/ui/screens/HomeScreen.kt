package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.test.data.models.Task
import com.example.test.ui.theme.*
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    userId: Long?,
    firstName: String = "",
    isDarkTheme: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
) {
    val tasks by taskViewModel.tasks.collectAsState()

    // Theme colors
    val bgColor     = if (isDarkTheme) Color(0xFF0F0F1A) else BackgroundGray
    val textPrimary = if (isDarkTheme) Color(0xFFE8E4FF) else Color(0xFF1A1A2E)
    val cardBg      = if (isDarkTheme) Color(0xFF1A1A2E) else Color.White
    val cardBorder  = if (isDarkTheme) Color(0xFF3A3A5C) else Color(0xFF1A1A2E)

    // Hamburger menu open/closed
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        // ── Main scrollable content ───────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 20.dp, bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                HomeHeader(
                    firstName = firstName,
                    isDarkTheme = isDarkTheme,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    onMenuClick = { menuOpen = true }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                QuickAccessRow(
                    onCalendarClick = onCalendarClick,
                    onNotesClick = onNotesClick
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Text(
                    text = "Tasks",
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = textPrimary
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                TaskListCard(
                    tasks = tasks,
                    isDarkTheme = isDarkTheme,
                    onCheckedChange = { task -> taskViewModel.toggleTask(task) }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        // ── Bottom bar + FAB ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(top = 10.dp)
                    .background(
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-15).dp)
                    .size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "Add Task", modifier = Modifier.size(32.dp))
            }
        }

        // ── Scrim: fades in/out independently, no slide ──────────────────
        AnimatedVisibility(
            visible = menuOpen,
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
                    .clickable { menuOpen = false }
            )
        }

        // ── Drawer panel: slides in/out only, no fade ─────────────────────
        AnimatedVisibility(
            visible = menuOpen,
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
                            color = if (isDarkTheme) Color(0xFF1A1A2E) else Color.White,
                            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                        )
                        .clickable { /* consume clicks so scrim doesn't close */ }
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
                                onClick = { menuOpen = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE).copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Close, "Close menu", tint = textPrimary)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // User greeting in drawer
                        Text(
                            text = if (firstName.isBlank()) "Hello!" else "Hi, $firstName 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        )

                        Spacer(Modifier.height(8.dp))

                        // Accent divider
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(PrimaryBlue)
                        )

                        Spacer(Modifier.height(36.dp))

                        // Dark mode toggle row
                        DrawerMenuItem(
                            label = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            iconTint = Color(0xFFFFD700),
                            textColor = textPrimary,
                            onClick = {
                                onToggleDarkMode()
                                menuOpen = false
                            }
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = textPrimary.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))

                        // Logout row
                        DrawerMenuItem(
                            label = "Log Out",
                            icon = Icons.Default.Logout,
                            iconTint = Color(0xFFFF6B6B),
                            textColor = Color(0xFFFF6B6B),
                            onClick = {
                                menuOpen = false
                                onLogout()
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        // App version at bottom
                        Text(
                            "v1.0.0",
                            fontSize = 11.sp,
                            color = textPrimary.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// ── Drawer Menu Item ──────────────────────────────────────────────────────────
@Composable
fun DrawerMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ── Home Header (card style matching the design image) ────────────────────────
@Composable
fun HomeHeader(
    firstName: String,
    isDarkTheme: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    onMenuClick: () -> Unit
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Good night"
        }
    }
    val dateString = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }
    val displayName = if (firstName.isBlank()) "there" else firstName

    Column {
        // ── The header "text box" card ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, cardBorder, RoundedCornerShape(20.dp))
                .background(cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: greeting + name + date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, $displayName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = textPrimary.copy(alpha = 0.45f),
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Right side: hamburger menu button
                HamburgerButton(
                    color = textPrimary,
                    onClick = onMenuClick
                )
            }
        }

        // ── Accent line beneath the card matching bottom bar ──────────────
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(PrimaryBlue)
        )
    }
}

// ── Hamburger Button (3 animated bars) ───────────────────────────────────────
@Composable
fun HamburgerButton(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bar 1 — full width
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
            // Bar 2 — slightly shorter for style
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(50))
                    .align(Alignment.Start)
                    .offset(x = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryBlue)
            )
            // Bar 3 — full width
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

// ── Quick Access Row ──────────────────────────────────────────────────────────
@Composable
fun QuickAccessRow(
    onCalendarClick: () -> Unit,
    onNotesClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAccessCard(
            title = "Notes",
            subtitle = "Tap to open",
            icon = Icons.Default.Add,
            backgroundColor = Color(0xFFFFD54F),
            modifier = Modifier.weight(1f),
            onClick = onNotesClick
        )
        QuickAccessCard(
            title = "Calendar",
            subtitle = "View schedule",
            icon = Icons.Default.CalendarMonth,
            backgroundColor = Color(0xFF81C784),
            modifier = Modifier.weight(1f),
            onClick = onCalendarClick
        )
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

// ── Task List Card ────────────────────────────────────────────────────────────
@Composable
fun TaskListCard(
    tasks: List<Task>,
    isDarkTheme: Boolean = false,
    onCheckedChange: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) Color(0xFF1A1A2E) else Color.White

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks yet. Tap + to add one!", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskItem(
                        task = task,
                        isDarkTheme = isDarkTheme,
                        onCheckedChange = { onCheckedChange(task) }
                    )
                    if (index < tasks.lastIndex) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

// ── Task Item ─────────────────────────────────────────────────────────────────
@Composable
fun TaskItem(
    task: Task,
    isDarkTheme: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = if (isDarkTheme) Color(0xFFE8E4FF) else Color(0xFF1A1A2E)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    uncheckedColor = Color(0xFFCCCCCC),
                    checkedColor = PrimaryBlue
                ),
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (task.isChecked) Color.Gray else textColor
                )
                Spacer(Modifier.height(6.dp))
                CategoryChip(category = task.category)
            }
        }
    }
}

// ── Category Chip ─────────────────────────────────────────────────────────────
@Composable
fun CategoryChip(category: String) {
    val (bg, textColor) = when (category.uppercase()) {
        "HEALTH"        -> ChipHealthBg to ChipHealthText
        "WORK"          -> ChipWorkBg to ChipWorkText
        "MENTAL HEALTH" -> ChipMentalBg to ChipMentalText
        else            -> Color(0xFFF0F0F0) to Color.Gray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}