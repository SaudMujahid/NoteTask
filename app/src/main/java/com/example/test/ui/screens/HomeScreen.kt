package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
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
    firstName: String = "",               // ← pass in from currentUser
    isDarkTheme: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
) {
    val tasks by taskViewModel.tasks.collectAsState()

    // Theme-aware colors
    val bgColor       = if (isDarkTheme) Color(0xFF0F0F1A) else BackgroundGray
    val textPrimary   = if (isDarkTheme) Color(0xFFE8E4FF) else Color(0xFF1A1A2E)
    val textSecondary = if (isDarkTheme) Color(0xFF9999BB) else Color.Gray
    val cardBg        = if (isDarkTheme) Color(0xFF1A1A2E) else Color(0xFFFFFBF2)
    val cardBorder    = if (isDarkTheme) Color(0xFF3A3A5C) else Color(0xFF1A1A2E)
    val iconBg        = if (isDarkTheme) Color(0xFF252538) else Color(0xFF1A1A2E)

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Top bar (header card + icons) ─────────────────────────────
            item {
                HomeTopBar(
                    firstName = firstName,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogout = onLogout,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    iconBg = iconBg,
                    textPrimary = textPrimary,
                    accentColor = PrimaryBlue
                )
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                QuickAccessRow(
                    onCalendarClick = onCalendarClick,
                    onNotesClick = onNotesClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Text(
                    text = "Tasks",
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                TaskListCard(
                    tasks = tasks,
                    isDarkTheme = isDarkTheme,
                    onCheckedChange = { task -> taskViewModel.toggleTask(task) },
                    modifier = Modifier.padding(horizontal = 16.dp)
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
    }
}

// ── Home Top Bar ──────────────────────────────────────────────────────────────
@Composable
fun HomeTopBar(
    firstName: String,
    isDarkTheme: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    iconBg: Color,
    textPrimary: Color,
    accentColor: Color
) {
    // Greeting based on time of day
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Good night"
        }
    }

    // Live date string e.g. "Mon, Apr 20"
    val dateString = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }

    val displayName = if (firstName.isBlank()) "there" else firstName

    Column {
        // ── Header card ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: greeting + name
                Column {
                    Text(
                        text = "$greeting,",
                        fontSize = 14.sp,
                        color = textPrimary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = displayName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 13.sp,
                        color = textPrimary.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Normal
                    )
                }

                // Right: dark mode toggle + profile/logout button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dark mode toggle
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconBg)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle dark mode",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Profile / logout button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Accent line matching bottom bar color ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
        )
    }
}

// ── Quick Access Row ──────────────────────────────────────────────────────────
@Composable
fun QuickAccessRow(
    onCalendarClick: () -> Unit,
    onNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tasks yet. Tap + to add one!",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
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