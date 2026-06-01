package com.example.test.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    val today = remember { dateFormat.format(Date()) }
    val todayLabel = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }

    // 7 days from tomorrow
    val sevenDaysLater = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time
    }
    val sevenDaysLaterStr = remember { dateFormat.format(sevenDaysLater) }

    // Beyond 7 days (for "show more")
    val allUpcomingEnd = remember {
        Calendar.getInstance().apply { add(Calendar.YEAR, 10) }.time
    }
    val allUpcomingEndStr = remember { dateFormat.format(allUpcomingEnd) }

    val todayTasks = remember(tasks, today) {
        tasks.filter { it.date == today }
    }
    val overdueTasks = remember(tasks, today) {
        tasks.filter { it.date < today && it.date.isNotBlank() && !it.isChecked }
            .sortedBy { it.date }
    }
    val upcomingSevenTasks = remember(tasks, today, sevenDaysLaterStr) {
        tasks.filter { it.date > today && it.date <= sevenDaysLaterStr && it.date.isNotBlank() }
            .sortedBy { it.date }
    }
    val upcomingBeyondTasks = remember(tasks, sevenDaysLaterStr, allUpcomingEndStr) {
        tasks.filter { it.date > sevenDaysLaterStr && it.date <= allUpcomingEndStr && it.date.isNotBlank() }
            .sortedBy { it.date }
    }

    val hasOverdue = overdueTasks.isNotEmpty()
    val hasUpcoming = upcomingSevenTasks.isNotEmpty() || upcomingBeyondTasks.isNotEmpty()

    var showMoreUpcoming by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTask(null) },
                containerColor = cs.primary,
                contentColor = cs.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "All Tasks",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = cs.onBackground
                        )
                        Text(
                            "Today · $todayLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White else cs.primary,
                            letterSpacing = 0.3.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Close", tint = cs.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background,
                    titleContentColor = cs.onBackground
                )
            )
        },
        containerColor = cs.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── OVERDUE card ──
            if (hasOverdue) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 280.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Overdue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else cs.error
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(cs.error.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "${overdueTasks.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else cs.error
                                )
                            }
                        }

                        HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))

                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            overdueTasks.forEachIndexed { index, task ->
                                val parsedDate = remember(task.date) {
                                    try {
                                        dateFormat.parse(task.date)
                                    } catch (_: Exception) { null }
                                }
                                val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

                                SwipeOffTaskItem(
                                    checked = task.isChecked,
                                    onCheckedChange = { taskViewModel.toggleTask(task) }
                                ) { checked, onCheck ->
                                    TaskItem(
                                        task = task,
                                        checked = checked,
                                        onCheckedChange = onCheck,
                                        onEditTask = onAddTask,
                                        onDelete = { taskViewModel.deleteTask(task) },
                                        dateText = dateText,
                                        isOverdue = true
                                    )
                                }
                                if (index < overdueTasks.lastIndex) {
                                    HorizontalDivider(
                                        color = cs.outline.copy(alpha = 0.2f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ── TODAY card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Today",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(cs.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "${todayTasks.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else cs.primary
                            )
                        }
                    }

                    HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))

                    if (todayTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉", fontSize = 36.sp)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "All clear for today!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "No tasks scheduled.",
                                    fontSize = 13.sp,
                                    color = cs.onSurface.copy(alpha = 0.45f)
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            todayTasks.forEachIndexed { index, task ->
                                SwipeOffTaskItem(
                                    checked = task.isChecked,
                                    onCheckedChange = { taskViewModel.toggleTask(task) }
                                ) { checked, onCheck ->
                                    TaskItem(
                                        task = task,
                                        checked = checked,
                                        onCheckedChange = onCheck,
                                        onEditTask = onAddTask,
                                        onDelete = { taskViewModel.deleteTask(task) }
                                    )
                                }
                                if (index < todayTasks.lastIndex) {
                                    HorizontalDivider(
                                        color = cs.outline.copy(alpha = 0.2f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ── UPCOMING card ──
            if (hasUpcoming) {
                val totalUpcomingCount = upcomingSevenTasks.size + upcomingBeyondTasks.size
                val visibleCount = if (showMoreUpcoming) totalUpcomingCount else upcomingSevenTasks.size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = if (showMoreUpcoming) 480.dp else 320.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Upcoming",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = cs.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(cs.secondary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    if (showMoreUpcoming || upcomingBeyondTasks.isEmpty())
                                        "$visibleCount"
                                    else
                                        "${upcomingSevenTasks.size}+",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else cs.secondary
                                )
                            }
                        }

                        HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))

                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                            // Always show next 7 days
                            upcomingSevenTasks.forEachIndexed { index, task ->
                                val parsedDate = remember(task.date) {
                                    try { dateFormat.parse(task.date) } catch (_: Exception) { null }
                                }
                                val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

                                SwipeOffTaskItem(
                                    checked = task.isChecked,
                                    onCheckedChange = { taskViewModel.toggleTask(task) }
                                ) { checked, onCheck ->
                                    TaskItem(
                                        task = task,
                                        checked = checked,
                                        onCheckedChange = onCheck,
                                        onEditTask = onAddTask,
                                        onDelete = { taskViewModel.deleteTask(task) },
                                        dateText = dateText
                                    )
                                }

                                val isLastInSection = index == upcomingSevenTasks.lastIndex
                                val hasMore = upcomingBeyondTasks.isNotEmpty()

                                if (!isLastInSection || (isLastInSection && hasMore && showMoreUpcoming)) {
                                    HorizontalDivider(
                                        color = cs.outline.copy(alpha = 0.2f),
                                        thickness = 1.dp
                                    )
                                }
                            }

                            // "Show more" expanded tasks
                            AnimatedVisibility(
                                visible = showMoreUpcoming,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    upcomingBeyondTasks.forEachIndexed { index, task ->
                                        val parsedDate = remember(task.date) {
                                            try { dateFormat.parse(task.date) } catch (_: Exception) { null }
                                        }
                                        val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

                                        SwipeOffTaskItem(
                                            checked = task.isChecked,
                                            onCheckedChange = { taskViewModel.toggleTask(task) }
                                        ) { checked, onCheck ->
                                            TaskItem(
                                                task = task,
                                                checked = checked,
                                                onCheckedChange = onCheck,
                                                onEditTask = onAddTask,
                                                onDelete = { taskViewModel.deleteTask(task) },
                                                dateText = dateText
                                            )
                                        }
                                        if (index < upcomingBeyondTasks.lastIndex) {
                                            HorizontalDivider(
                                                color = cs.outline.copy(alpha = 0.2f),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }

                            // Show more / Show less button
                            if (upcomingBeyondTasks.isNotEmpty()) {
                                HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))
                                TextButton(
                                    onClick = { showMoreUpcoming = !showMoreUpcoming },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showMoreUpcoming)
                                            Icons.Default.KeyboardArrowUp
                                        else
                                            Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = cs.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (showMoreUpcoming)
                                            "Show less"
                                        else
                                            "Show ${upcomingBeyondTasks.size} more",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = cs.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Bottom breathing room
            Spacer(Modifier.height(8.dp))
        }
    }
}