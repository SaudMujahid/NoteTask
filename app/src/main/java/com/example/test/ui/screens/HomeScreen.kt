package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.example.test.data.models.Task
import com.example.test.ui.components.DrawerMenu
import com.example.test.ui.components.SwipeOffTaskItem
import com.example.test.ui.components.TaskItem
import com.example.test.ui.theme.*
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private enum class HomeTaskFilter {
    PENDING,
    COMPLETED
}

@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    userId: Long?,
    firstName: String = "",
    isDarkTheme: Boolean = false,
    isLoggedIn: Boolean = false,           // ← new: drives Login / Logout label
    onToggleDarkMode: () -> Unit = {},
    onAuthAction: () -> Unit = {},         // ← replaces onLogout; caller decides login or logout
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onTasksClick: () -> Unit = {}
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val todayTasks = remember(tasks, today) { tasks.filter { it.date == today } }
    val pendingTasks = remember(todayTasks) { todayTasks.filter { !it.isChecked } }
    val completedTasks = remember(todayTasks) { todayTasks.filter { it.isChecked } }
    var homeTaskFilter by remember { mutableStateOf(HomeTaskFilter.PENDING) }
    val filteredTasks = when (homeTaskFilter) {
        HomeTaskFilter.PENDING -> pendingTasks
        HomeTaskFilter.COMPLETED -> completedTasks
    }

    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {

        // ── Main content ──────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                HomeHeader(
                    firstName = firstName,
                    onMenuClick = { menuOpen = true }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                QuickAccessRow(
                    onCalendarClick = onCalendarClick,
                    onNotesClick = onNotesClick
                )
                Spacer(Modifier.height(24.dp))
            }

            // Tasks section header — tapping opens fullscreen
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTasksClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tasks",
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View all tasks",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(
                        text = "pending",
                        backgroundColor = PendingBadgeBg,
                        borderColor = PendingBadgeBorder,
                        contentColor = PendingBadgeBorder,
                        count = pendingTasks.size,
                        selected = homeTaskFilter == HomeTaskFilter.PENDING,
                        onClick = { homeTaskFilter = HomeTaskFilter.PENDING }
                    )
                    StatusBadge(
                        text = "completed",
                        backgroundColor = CompletedBadgeBg,
                        borderColor = CompletedBadgeBorder,
                        contentColor = CompletedBadgeBorder,
                        count = completedTasks.size,
                        selected = homeTaskFilter == HomeTaskFilter.COMPLETED,
                        onClick = { homeTaskFilter = HomeTaskFilter.COMPLETED }
                    )
                }
            }

            item { Spacer(Modifier.height(10.dp)) }

            item {
                // TaskListCard now uses SwipeOffTaskItem internally
                TaskListCard(
                    tasks = filteredTasks,
                    emptyMessage = if (homeTaskFilter == HomeTaskFilter.PENDING) {
                        "No pending tasks for today."
                    } else {
                        "No completed tasks for today."
                    },
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
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-15).dp)
                    .size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "Add Task", modifier = Modifier.size(32.dp))
            }
        }

        // ── Drawer (extracted to its own composable) ──────────────────────
        DrawerMenu(
            isOpen = menuOpen,
            firstName = firstName,
            isDarkTheme = isDarkTheme,
            isLoggedIn = isLoggedIn,
            onClose = { menuOpen = false },
            onToggleDarkMode = onToggleDarkMode,
            onAuthAction = onAuthAction
        )
    }
}

// ── TaskListCard ─────────────────────────────────────────────────────────────
// Uses SwipeOffTaskItem so every row gets the shared animation + sound.

@Composable
fun TaskListCard(
    tasks: List<Task>,
    onCheckedChange: (Task) -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
                        emptyMessage,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                tasks.forEachIndexed { index, task ->
                    // ↓ SwipeOffTaskItem wraps each row with animation + sound
                    SwipeOffTaskItem(
                        checked = task.isChecked,
                        onCheckedChange = { onCheckedChange(task) }
                    ) { checked, onCheck ->
                        TaskItem(
                            title = task.title,
                            category = task.category,
                            checked = checked,
                            onCheckedChange = onCheck
                        )
                    }
                    if (index < tasks.lastIndex) {
                        HorizontalDivider(
                            color = colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

// ── HomeHeader ───────────────────────────────────────────────────────────────

@Composable
fun HomeHeader(
    firstName: String,
    onMenuClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                .background(colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, $displayName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = colorScheme.onSurface.copy(alpha = 0.45f),
                        letterSpacing = 0.3.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                HamburgerButton(onClick = onMenuClick)
            }
        }

        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.primary)
        )
    }
}

// ── HamburgerButton ──────────────────────────────────────────────────────────

@Composable
fun HamburgerButton(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onBackground
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
            Box(Modifier.width(22.dp).height(2.5.dp).clip(RoundedCornerShape(50)).background(color))
            Box(
                Modifier
                    .width(16.dp).height(2.5.dp).clip(RoundedCornerShape(50))
                    .align(Alignment.Start).offset(x = 3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(Modifier.width(22.dp).height(2.5.dp).clip(RoundedCornerShape(50)).background(color))
        }
    }
}

// ── QuickAccessRow ───────────────────────────────────────────────────────────

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
    icon: ImageVector,
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