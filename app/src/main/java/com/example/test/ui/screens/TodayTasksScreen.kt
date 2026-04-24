package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.components.CategoryChip
import com.example.test.ui.components.SwipeOffTaskItem
import com.example.test.ui.components.TaskItem
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksScreen(
    taskViewModel: TaskViewModel,
    onClose: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayLabel = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }
    val displayFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    val todayTasks = remember(tasks) { tasks.filter { it.date == today } }
    val overdueTasks = remember(tasks) {
        tasks.filter { it.date < today && it.date.isNotBlank() && !it.isChecked }
            .sortedBy { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tasks",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onBackground
                        )
                        Text(
                            "Today · $todayLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                            letterSpacing = 0.3.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── TODAY ─────────────────────────────────────────────────────
            if (todayTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks for today.",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(todayTasks.size) { index ->
                    val task = todayTasks[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        // Wrap with SwipeOffTaskItem for animation + sound
                        SwipeOffTaskItem(
                            checked = task.isChecked,
                            onCheckedChange = { taskViewModel.toggleTask(task) }
                        ) { checked, onCheck ->
                            TaskItem(
                                title = task.title,
                                category = task.category,
                                checked = checked,
                                onCheckedChange = onCheck
                            )
                        }
                    }
                }
            }

            // ── OVERDUE ───────────────────────────────────────────────────
            if (overdueTasks.isNotEmpty()) {
                item { Spacer(Modifier.height(24.dp)) }

                item {
                    Text(
                        text = "Overdue",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            overdueTasks.forEachIndexed { index, task ->
                                val parsedDate = remember(task.date) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            .parse(task.date)
                                    } catch (_: Exception) { null }
                                }
                                val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

                                // Wrap with SwipeOffTaskItem for animation + sound
                                SwipeOffTaskItem(
                                    checked = task.isChecked,
                                    onCheckedChange = { taskViewModel.toggleTask(task) }
                                ) { checked, onCheck ->
                                    OverdueTaskRow(
                                        task = task,
                                        dateText = dateText,
                                        checked = checked,
                                        onCheckedChange = onCheck
                                    )
                                }

                                if (index < overdueTasks.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdueTaskRow(
    task: Task,
    dateText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = colorScheme.error,
                    uncheckedColor = colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (checked) colorScheme.onSurface.copy(alpha = 0.4f)
                    else colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                CategoryChip(category = task.category)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.error.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.error
            )
        }
    }
}