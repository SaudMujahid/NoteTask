package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.example.test.ui.components.SwipeOffTaskItem
import com.example.test.ui.components.TaskItem
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksScreen(
    taskViewModel: TaskViewModel,
    onAddTask: (Task?) -> Unit = {},
    onClose: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val cs     = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    val dateFormat       = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("MMM d",      Locale.getDefault()) }
    val today            = remember { dateFormat.format(Date()) }
    val todayLabel       = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }

    val sevenDaysLaterStr = remember {
        dateFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time)
    }
    val allUpcomingEndStr = remember {
        dateFormat.format(Calendar.getInstance().apply { add(Calendar.YEAR, 10) }.time)
    }

    val overdueTasks = remember(tasks, today) {
        tasks.filter { it.date < today && it.date.isNotBlank() && !it.isChecked }
            .sortedBy { it.date }
    }
    val todayTasks = remember(tasks, today) {
        tasks.filter { it.date == today }
    }
    val upcomingTasks = remember(tasks, today, allUpcomingEndStr) {
        tasks.filter { it.date > today && it.date <= allUpcomingEndStr && it.date.isNotBlank() }
            .sortedBy { it.date }
    }

    // ── Color palette for sections ────────────────────────────────────────────
    val overdueColor  = cs.error
    val todayColor    = cs.primary
    val upcomingColor = cs.secondary

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTask(null) },
                containerColor = cs.primary,
                contentColor   = cs.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Task")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "All Tasks",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = cs.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            todayLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.primary.copy(alpha = if (isDark) 0.8f else 1f),
                            letterSpacing = 0.2.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cs.onSurface.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close, "Close",
                                tint = cs.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background
                )
            )
        },
        containerColor = cs.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp
            )
        ) {

            // ══════════════════════════════════════════════════
            // OVERDUE
            // ══════════════════════════════════════════════════
            if (overdueTasks.isNotEmpty()) {
                item(key = "header_overdue") {
                    SectionHeader(
                        label    = "Overdue",
                        count    = overdueTasks.size,
                        color    = overdueColor,
                        topPad   = 8.dp,
                        isDark   = isDark
                    )
                }

                itemsIndexed(
                    items = overdueTasks,
                    key   = { _, task -> "overdue_${task.id}" }
                ) { index, task ->
                    val dateText = remember(task.date) {
                        try { displayFormatter.format(dateFormat.parse(task.date)!!) }
                        catch (_: Exception) { task.date }
                    }
                    TaskRow(
                        task            = task,
                        index           = index,
                        lastIndex       = overdueTasks.lastIndex,
                        accentColor     = overdueColor,
                        isFirst         = index == 0,
                        isLast          = index == overdueTasks.lastIndex,
                        dateText        = dateText,
                        isOverdue       = true,
                        cs              = cs,
                        isDark          = isDark,
                        onToggle        = { taskViewModel.toggleTask(task) },
                        onEdit          = { onAddTask(it) },
                        onDelete        = { taskViewModel.deleteTask(task) }
                    )
                }

                item(key = "spacer_overdue") { Spacer(Modifier.height(20.dp)) }
            }

            // ══════════════════════════════════════════════════
            // TODAY
            // ══════════════════════════════════════════════════
            item(key = "header_today") {
                SectionHeader(
                    label  = "Today",
                    count  = todayTasks.size,
                    color  = todayColor,
                    isDark = isDark
                )
            }

            if (todayTasks.isEmpty()) {
                item(key = "today_empty") {
                    EmptySection(
                        emoji   = "🎉",
                        title   = "All clear for today!",
                        subtitle = "No tasks scheduled.",
                        isFirst = true,
                        isLast  = true,
                        cs      = cs
                    )
                    Spacer(Modifier.height(20.dp))
                }
            } else {
                itemsIndexed(
                    items = todayTasks,
                    key   = { _, task -> "today_${task.id}" }
                ) { index, task ->
                    TaskRow(
                        task        = task,
                        index       = index,
                        lastIndex   = todayTasks.lastIndex,
                        accentColor = todayColor,
                        isFirst     = index == 0,
                        isLast      = index == todayTasks.lastIndex,
                        cs          = cs,
                        isDark      = isDark,
                        onToggle    = { taskViewModel.toggleTask(task) },
                        onEdit      = { onAddTask(it) },
                        onDelete    = { taskViewModel.deleteTask(task) }
                    )
                }
                item(key = "spacer_today") { Spacer(Modifier.height(20.dp)) }
            }

            // ══════════════════════════════════════════════════
            // UPCOMING
            // ══════════════════════════════════════════════════
            if (upcomingTasks.isNotEmpty()) {
                item(key = "header_upcoming") {
                    SectionHeader(
                        label  = "Upcoming",
                        count  = upcomingTasks.size,
                        color  = upcomingColor,
                        isDark = isDark
                    )
                }

                itemsIndexed(
                    items = upcomingTasks,
                    key   = { _, task -> "upcoming_${task.id}" }
                ) { index, task ->
                    val dateText = remember(task.date) {
                        try { displayFormatter.format(dateFormat.parse(task.date)!!) }
                        catch (_: Exception) { task.date }
                    }
                    TaskRow(
                        task        = task,
                        index       = index,
                        lastIndex   = upcomingTasks.lastIndex,
                        accentColor = upcomingColor,
                        isFirst     = index == 0,
                        isLast      = index == upcomingTasks.lastIndex,
                        dateText    = dateText,
                        cs          = cs,
                        isDark      = isDark,
                        onToggle    = { taskViewModel.toggleTask(task) },
                        onEdit      = { onAddTask(it) },
                        onDelete    = { taskViewModel.deleteTask(task) }
                    )
                }
            }

            // ── All done state ────────────────────────────────
            if (overdueTasks.isEmpty() && todayTasks.isEmpty() && upcomingTasks.isEmpty()) {
                item(key = "all_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Nothing here yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = cs.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap + to add your first task",
                                fontSize = 14.sp,
                                color = cs.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    label: String,
    count: Int,
    color: Color,
    isDark: Boolean,
    topPad: androidx.compose.ui.unit.Dp = 0.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPad, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Colored accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else color,
            letterSpacing = 0.2.sp
        )
        // Count badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = if (isDark) 0.9f else 1f)
            )
        }
    }
}

// ── Single task row with card-style top/bottom rounding ──────────────────────

@Composable
private fun TaskRow(
    task: Task,
    index: Int,
    lastIndex: Int,
    accentColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    cs: ColorScheme,
    isDark: Boolean,
    dateText: String? = null,
    isOverdue: Boolean = false,
    onToggle: () -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: () -> Unit
) {
    val topRadius    = if (isFirst) 16.dp else 0.dp
    val bottomRadius = if (isLast)  16.dp else 0.dp
    val shape = RoundedCornerShape(
        topStart    = topRadius,
        topEnd      = topRadius,
        bottomStart = bottomRadius,
        bottomEnd   = bottomRadius
    )

    // Left accent strip only on first item (represents the whole group)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cs.surface)
    ) {
        // Thin left-edge color strip
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    accentColor.copy(
                        alpha = if (isDark) 0.5f else 0.35f
                    )
                )
        )

        SwipeOffTaskItem(
            checked        = task.isChecked,
            onCheckedChange = { onToggle() }
        ) { checked, onCheck ->
            TaskItem(
                task           = task,
                checked        = checked,
                onCheckedChange = onCheck,
                onEditTask     = { onEdit(it) },
                onDelete       = onDelete,
                dateText       = dateText,
                isOverdue      = isOverdue
            )
        }
    }

    // Divider between items (not after last)
    if (!isLast) {
        HorizontalDivider(
            color     = cs.outline.copy(alpha = if (isDark) 0.14f else 0.1f),
            thickness = 1.dp
        )
    }
}

// ── Empty section placeholder ─────────────────────────────────────────────────

@Composable
private fun EmptySection(
    emoji: String,
    title: String,
    subtitle: String,
    isFirst: Boolean,
    isLast: Boolean,
    cs: ColorScheme
) {
    val topRadius    = if (isFirst) 16.dp else 0.dp
    val bottomRadius = if (isLast)  16.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart    = topRadius,    topEnd      = topRadius,
                    bottomStart = bottomRadius, bottomEnd   = bottomRadius
                )
            )
            .background(cs.surface)
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.4f))
        }
    }
}