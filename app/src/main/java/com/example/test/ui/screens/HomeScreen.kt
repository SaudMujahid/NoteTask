package com.example.test.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.AppDatabase
import com.example.test.data.DataTransferManager
import com.example.test.data.models.Task
import com.example.test.data.repository.ProfileRepository
import com.example.test.ui.components.DrawerMenu
import com.example.test.ui.components.SwipeOffTaskItem
import com.example.test.ui.components.TaskCategories
import com.example.test.ui.components.TaskItem
import com.example.test.ui.components.TransferSheet
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
    isDarkTheme: Boolean = false,
    paletteIndex: Int = 0,
    onToggleDarkMode: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onPaletteChange: (Int) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val taskState by taskViewModel.tasks.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val profileRepository = remember { ProfileRepository.getInstance(context) }
    val profile by profileRepository.profileFlow.collectAsState()
    val firstName = profile.firstName

    val transferManager = remember {
        val db = AppDatabase.getDatabase(context)
        DataTransferManager(db.taskDao(), db.noteDao())
    }
    var showTransferSheet by remember { mutableStateOf(false) }

    if (showTransferSheet) {
        TransferSheet(
            transferManager = transferManager,
            userName = firstName,
            onDismiss = { showTransferSheet = false },
            onRestored = { restoredName ->
                profileRepository.saveProfile(profile.copy(firstName = restoredName))
                showTransferSheet = false
            }
        )
    }

    val todayTasks = remember(taskState, today) { taskState.filter { it.date == today } }
    val availableCategories = remember { TaskCategories.ALL_CATEGORIES }
    val pendingTasks = remember(todayTasks) { todayTasks.filter { !it.isChecked } }
    val completedTasks = remember(todayTasks) { todayTasks.filter { it.isChecked } }
    var homeTaskFilter by remember { mutableStateOf(HomeTaskFilter.PENDING) }
    var selectedCategory by remember { mutableStateOf(TaskCategories.ALL) }
    val statusFilteredTasks = when (homeTaskFilter) {
        HomeTaskFilter.PENDING -> pendingTasks
        HomeTaskFilter.COMPLETED -> completedTasks
    }
    val filteredTasks = if (selectedCategory == TaskCategories.ALL) {
        statusFilteredTasks
    } else {
        statusFilteredTasks.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

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
                    onMenuClick = { menuOpen = true },
                    isDarkTheme = isDarkTheme
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
                TypeFilterDropdownBadge(
                    selectedCategory = selectedCategory,
                    categories = availableCategories,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            item { Spacer(Modifier.height(10.dp)) }

            item {
                TaskListCard(
                    tasks = filteredTasks,
                    emptyMessage = if (homeTaskFilter == HomeTaskFilter.PENDING) {
                        if (selectedCategory == TaskCategories.ALL) {
                            "No pending tasks for today."
                        } else {
                            "No pending $selectedCategory tasks for today."
                        }
                    } else {
                        if (selectedCategory == TaskCategories.ALL) {
                            "No completed tasks for today."
                        } else {
                            "No completed $selectedCategory tasks for today."
                        }
                    },
                    onCheckedChange = { task -> taskViewModel.toggleTask(task) },
                    isDarkTheme = isDarkTheme
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
                        color = if (isDarkTheme) colorScheme.primaryContainer
                        else colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = if (isDarkTheme) colorScheme.primary else Color.White,
                contentColor   = if (isDarkTheme) colorScheme.onPrimary else colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-15).dp)
                    .size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "Add Task", modifier = Modifier.size(32.dp))
            }
        }

        // ── Drawer ──────────────────────
        DrawerMenu(
            isOpen = menuOpen,
            firstName = firstName,
            isDarkTheme = isDarkTheme,
            paletteIndex    = paletteIndex,
            onClose = { menuOpen = false },
            onToggleDarkMode = onToggleDarkMode,
            onStatsClick = onStatsClick,
            onPaletteChange = onPaletteChange,
            onTransferClick = { showTransferSheet = true },
            onProfileClick = onProfileClick
        )
    }
}

// ── TaskListCard ─────────────────────────────────────────────────────────────

@Composable
fun TaskListCard(
    tasks: List<Task>,
    onCheckedChange: (Task) -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    var expandedTaskId by remember { mutableStateOf<Long?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isDarkTheme) Modifier.shadow(3.dp, RoundedCornerShape(22.dp)) else Modifier),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(
            1.dp,
            // stronger border in dark compensates for the missing shadow
            colorScheme.primary.copy(alpha = if (isDarkTheme) 0.30f else 0.14f)
        )
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
                    val taskIsExpanded = expandedTaskId == task.id
                    val taskHasDescription = task.description.isNotBlank()

                    SwipeOffTaskItem(
                        checked = task.isChecked,
                        onCheckedChange = { onCheckedChange(task) }
                    ) { checked, onCheck ->
                        val clickModifier = if (taskHasDescription) {
                            Modifier.clickable {
                                expandedTaskId = if (taskIsExpanded) null else task.id
                            }
                        } else {
                            Modifier
                        }

                        TaskItem(
                            title = task.title,
                            category = task.category,
                            checked = checked,
                            onCheckedChange = onCheck,
                            isScheduled = task.isScheduled,
                            modifier = clickModifier,
                            description = task.description,
                            isExpanded = taskIsExpanded
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
    onMenuClick: () -> Unit,
    isDarkTheme: Boolean = false
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
        // ── Card with greeting text ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        lineHeight = 28.sp,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface.copy(alpha = 0.62f),
                        letterSpacing = 0.4.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                HamburgerButton(onClick = onMenuClick)
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Accent gradient bar ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = if (isDarkTheme) 0.6f else 1f),
                            colorScheme.primary.copy(alpha = if (isDarkTheme) 0.2f else 0.45f)
                        )
                    )
                )
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
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        QuickAccessCard(
            title = "Notes",
            subtitle = "Tap to open",
            icon = Icons.Default.Add,
            backgroundColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
            onClick = onNotesClick
        )
        QuickAccessCard(
            title = "Calendar",
            subtitle = "View schedule",
            icon = Icons.Default.CalendarMonth,
            backgroundColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
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
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = contentColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
