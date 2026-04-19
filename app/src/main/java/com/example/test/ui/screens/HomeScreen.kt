
package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.theme.*
import com.example.test.ui.viewmodels.TaskViewModel

// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    userId: Long?,
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
) {
    val tasks by taskViewModel.tasks.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // leave room for bottom bar
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item { WelcomeHeader() }
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
                    color = Color(0xFF1A1A2E)
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                TaskListCard(
                    tasks = tasks,
                    onCheckedChange = { task ->
                        taskViewModel.toggleTask(task)
                    }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        // Bottom bar + FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Blue bottom bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(top = 10.dp) // creates a gap so it looks floating or defined
                    .background(
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )

            // FAB
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ── Welcome Header ────────────────────────────────────────────────────────────
@Composable
fun WelcomeHeader() {
    Column {
        Text(
            text = "Morning, \nLiar Punk",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 38.sp,
            color = Color(0xFF1A1A2E)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You have 3 tasks for today",
            fontSize = 16.sp,
            color = Color.Gray
        )
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
            subtitle = "24 notes",
            icon = Icons.Default.Add, // Using Add as placeholder for Note icon
            backgroundColor = Color(0xFFFFD54F),
            modifier = Modifier.weight(1f),
            onClick = onNotesClick
        )
        QuickAccessCard(
            title = "Calendar",
            subtitle = "Mar 2025",
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
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
    onCheckedChange: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            tasks.forEachIndexed { index, task ->
                TaskItem(
                    task = task,
                    onCheckedChange = { onCheckedChange(task) }
                )
                if (index < tasks.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFEEEEEE),
                        thickness = 1.dp
                    )
                }
            }
            // Empty trailing space at bottom of card (matches design)
            Spacer(Modifier.height(60.dp))
        }
    }
}

// ── Task Item ─────────────────────────────────────────────────────────────────
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit
) {
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
                    color = if (task.isChecked) Color.Gray else Color(0xFF1A1A2E)
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

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        // Preview can't easily show the ViewModel logic
        Text("Home Screen Preview")
    }
}
