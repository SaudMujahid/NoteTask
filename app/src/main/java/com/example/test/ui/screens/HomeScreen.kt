//package com.example.test.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.example.test.ui.theme.BackgroundGray
//import com.example.test.ui.theme.PrimaryBlue
//
//@Composable
//fun HomeScreen(onAddTask: () -> Unit) {
//    Scaffold(
//        containerColor = BackgroundGray,
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onAddTask,
//                containerColor = PrimaryBlue,
//                shape = CircleShape
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
//            }
//        },
//        floatingActionButtonPosition = FabPosition.End
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(padding)
//                .padding(horizontal = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            item { Spacer(Modifier.height(16.dp)) }
//            item {
//                Text(
//                    "Tasks",
//                    style = MaterialTheme.typography.headlineLarge,
//                    fontWeight = FontWeight.Black
//                )
//            }
//            // Add more items or task list here
//        }
//    }
//}

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
import com.example.test.ui.theme.*

// ── Data Models ──────────────────────────────────────────────────────────────
data class Task(
    val id: Int,
    val title: String,
    val category: String,
    val subtasks: List<String> = emptyList(),
    var isChecked: Boolean = false
)

fun sampleTasks() = listOf(
    Task(1, "Drink 8 glasses of water", "HEALTH"),
    Task(2, "Edit the PDF", "WORK"),
    Task(
        3, "Write in a gratitude journal", "MENTAL HEALTH",
        subtasks = listOf("Get a notebook", "Follow the youtube tutorial")
    ),
    Task(4, "Stretch everyday for 15 mins", "HEALTH"),
)

// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
) {
    var tasks by remember { mutableStateOf(sampleTasks()) }

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
                    onCheckedChange = { id, checked ->
                        tasks = tasks.map { if (it.id == id) it.copy(isChecked = checked) else it }
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
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(BottomBarBlue)
            )
            // FAB overlapping the bar
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        }
    }
}

// ── Welcome Header ────────────────────────────────────────────────────────────
@Composable
fun WelcomeHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(2.dp, BorderBlue),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome back, John",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = "Apr 19",
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFB0B0C0)
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
        // Calendar Card
        Card(
            onClick = onCalendarClick,
            modifier = Modifier
                .weight(1f)
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(2.dp, BorderBlue),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = Color(0xFF1A1A2E),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Calendar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A2E)
                )
            }
        }

        // Notes Card
        Card(
            onClick = onNotesClick,
            modifier = Modifier
                .weight(1f)
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(2.dp, BorderGreen),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Notes icon — green rounded rectangle with a dot
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BorderGreen),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Notes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A2E)
                )
            }
        }
    }
}

// ── Task List Card ────────────────────────────────────────────────────────────
@Composable
fun TaskListCard(
    tasks: List<Task>,
    onCheckedChange: (Int, Boolean) -> Unit
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
                    onCheckedChange = { checked -> onCheckedChange(task.id, checked) }
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

        // Subtasks
        task.subtasks.forEach { subtask ->
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.padding(start = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {},
                    colors = CheckboxDefaults.colors(uncheckedColor = Color(0xFFCCCCCC)),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subtask,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
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
        HomeScreen()
    }
}
