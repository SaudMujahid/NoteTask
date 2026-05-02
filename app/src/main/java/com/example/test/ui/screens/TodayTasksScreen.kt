package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val cs = MaterialTheme.colorScheme

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayLabel = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }
    val displayFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    val todayTasks = remember(tasks, today) { tasks.filter { it.date == today } }
    val overdueTasks = remember(tasks, today) {
        tasks.filter { it.date < today && it.date.isNotBlank() && !it.isChecked }
            .sortedBy { it.date }
    }

    val hasOverdue = overdueTasks.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tasks",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = cs.onBackground
                        )
                        Text(
                            "Today · $todayLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.primary,
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

        // Use Column + weight so today's card naturally fills remaining space,
        // and shrinks proportionally when the overdue section appears.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── TODAY card — weight(1f) so it always fills available height ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (hasOverdue) 1.4f else 1f),   // slightly larger slice when overdue exists
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Section header
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
                        // Task count badge
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
                                color = cs.primary
                            )
                        }
                    }

                    HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))

                    if (todayTasks.isEmpty()) {
                        // Empty state — centred inside the card
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                        // Scrollable task list inside the card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            todayTasks.forEachIndexed { index, task ->
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

            // ── OVERDUE card — only shown when there are overdue tasks ───────
            if (hasOverdue) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Section header — matches today's style, error-tinted
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
                                color = cs.error
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
                                    color = cs.error
                                )
                            }
                        }

                        HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            overdueTasks.forEachIndexed { index, task ->
                                val parsedDate = remember(task.date) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            .parse(task.date)
                                    } catch (_: Exception) { null }
                                }
                                val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

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
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overdue task row — matches TaskItem layout with date badge on the right
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OverdueTaskRow(
    task: Task,
    dateText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    checkedColor = cs.error,
                    uncheckedColor = cs.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (checked) cs.onSurface.copy(alpha = 0.4f) else cs.onSurface
                )
                Spacer(Modifier.height(4.dp))
                CategoryChip(category = task.category)
            }
        }

        Spacer(Modifier.width(8.dp))

        // Date badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(cs.error.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = cs.error
            )
        }
    }
}