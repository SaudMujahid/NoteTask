package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.theme.*
import com.example.test.ui.utils.playCheckSound
import com.example.test.ui.viewmodels.CalendarDay
import com.example.test.ui.viewmodels.CalendarViewModel
import com.example.test.ui.viewmodels.CalendarViewMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateHome: () -> Unit = {},
    onAddTaskClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val viewMode by viewModel.viewMode.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setViewMode(CalendarViewMode.DAY)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContentColor = BorderBlue
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundGray.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calendar Views",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BorderBlue
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close menu",
                            tint = BorderBlue
                        )
                    }
                }

                NavigationDrawerItem(
                    label = { Text("Year View") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    selected = viewMode == CalendarViewMode.YEAR,
                    onClick = {
                        viewModel.setViewMode(CalendarViewMode.YEAR)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.18f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = BorderBlue,
                        unselectedTextColor = BorderBlue.copy(alpha = 0.8f),
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = BorderBlue.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Month View") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    },
                    selected = viewMode == CalendarViewMode.MONTH,
                    onClick = {
                        viewModel.setViewMode(CalendarViewMode.MONTH)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.18f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = BorderBlue,
                        unselectedTextColor = BorderBlue.copy(alpha = 0.8f),
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = BorderBlue.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Daily View") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null
                        )
                    },
                    selected = viewMode == CalendarViewMode.DAY,
                    onClick = {
                        viewModel.setViewMode(CalendarViewMode.DAY)
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.18f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = BorderBlue,
                        unselectedTextColor = BorderBlue.copy(alpha = 0.8f),
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = BorderBlue.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CalendarTopBar(
                    viewMode = viewMode,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onHomeClick = onNavigateHome,
                    viewModel = viewModel
                )
            },
            floatingActionButton = {
                if (viewMode != CalendarViewMode.YEAR) {
                    FloatingActionButton(
                        onClick = onAddTaskClick,
                        containerColor = BorderBlue, 
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task"
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            containerColor = colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colorScheme.background)
            ) {
                when (viewMode) {
                    CalendarViewMode.YEAR -> YearView(viewModel = viewModel)
                    CalendarViewMode.MONTH -> MonthView(viewModel = viewModel)
                    CalendarViewMode.DAY -> DayView(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    viewMode: CalendarViewMode,
    onMenuClick: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: CalendarViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val monthYearFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()) }

    val title = when (viewMode) {
        CalendarViewMode.YEAR -> currentYear.toString()
        CalendarViewMode.MONTH -> monthYearFormatter.format(currentMonth.time)
        CalendarViewMode.DAY -> dayFormatter.format(selectedDate)
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                letterSpacing = 0.25.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open view menu",
                    tint = colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Back to Home",
                    tint = colorScheme.onBackground
                )
            }

            IconButton(onClick = { viewModel.goToToday() }) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Today",
                    tint = colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.background.copy(alpha = 0.95f)
        )
    )
}

// year view with month cards and year navigation
@Composable
fun YearView(viewModel: CalendarViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val currentYear by viewModel.currentYear.collectAsState()
    
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Year navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousYear() }) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous Year",
                    tint = colorScheme.primary
                )
            }
            
            Text(
                text = currentYear.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            
            IconButton(onClick = { viewModel.nextYear() }) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next Year",
                    tint = colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { index ->
                MonthCard(
                    monthName = months[index],
                    monthIndex = index,
                    isCurrentMonth = isCurrentYearMonth(currentYear, index),
                    onClick = { viewModel.navigateToMonth(index) }
                )
            }
        }
    }
}

@Composable
fun MonthCard(
    monthName: String,
    monthIndex: Int,
    isCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .shadow(3.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = if (isCurrentMonth) colorScheme.primaryContainer else colorScheme.surface,
        border = if (isCurrentMonth) {
            BorderStroke(2.dp, colorScheme.primary)
        } else {
            BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.14f))
        },
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrentMonth) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun isCurrentYearMonth(year: Int, month: Int): Boolean {
    val today = Calendar.getInstance()
    return today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
}

// month view with calendar grid and month navigation
@Composable
fun MonthView(viewModel: CalendarViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val calendarDays by viewModel.calendarDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val selectedDateFormatter = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousMonth() },
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.surface, CircleShape)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = colorScheme.primary
                )
            }

            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.surface, CircleShape)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DaysOfWeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorScheme.surface.copy(alpha = 0.72f)),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays) { day ->
                CalendarDayCell(
                    day = day,
                    isSelected = isSameDay(day.date, selectedDate),
                    onClick = { viewModel.selectDateAndNavigateToDay(day.date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tasks for ${selectedDateFormatter.format(selectedDate)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (tasksForSelectedDate.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Text(
                    text = "No tasks for selected date",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasksForSelectedDate.size, key = { tasksForSelectedDate[it].id }) { index ->
                    val task = tasksForSelectedDate[index]
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                    if (index < tasksForSelectedDate.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = BorderBlue.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Separate scheduled and non-scheduled tasks
    val scheduledTasks = day.tasks.filter { it.isScheduled }
    val nonScheduledTasks = day.tasks.filter { !it.isScheduled }
    
    // Count scheduled tasks by completion status
    val incompleteScheduledCount = scheduledTasks.count { !it.isChecked }
    val completeScheduledCount = scheduledTasks.count { it.isChecked }

    // Determine background color based on scheduled task status
    val backgroundColor = when {
        isSelected -> colorScheme.primary
        scheduledTasks.isNotEmpty() && incompleteScheduledCount > 0 -> 
            colorScheme.error.copy(alpha = 0.2f)  // Light red for incomplete scheduled tasks
        scheduledTasks.isNotEmpty() && completeScheduledCount > 0 && incompleteScheduledCount == 0 -> 
            colorScheme.tertiary.copy(alpha = 0.2f)  // Light green for completed scheduled tasks
        day.isToday -> colorScheme.primary.copy(alpha = 0.2f)
        else -> colorScheme.surface
    }

    val textColor = when {
        isSelected -> colorScheme.onPrimary
        !day.isCurrentMonth -> colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        scheduledTasks.isNotEmpty() && incompleteScheduledCount > 0 -> colorScheme.error
        scheduledTasks.isNotEmpty() && completeScheduledCount > 0 && incompleteScheduledCount == 0 -> colorScheme.tertiary
        day.isToday -> colorScheme.primary
        else -> colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (day.isToday && !isSelected) 2.dp else 0.dp,
                color = if (day.isToday && !isSelected) colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            // Show scheduled task indicators (color bars)
            if (scheduledTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Incomplete scheduled tasks indicator
                    if (incompleteScheduledCount > 0) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (isSelected) colorScheme.onPrimary 
                                    else colorScheme.error
                                )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    
                    // Complete scheduled tasks indicator
                    if (completeScheduledCount > 0) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (isSelected) colorScheme.onPrimary 
                                    else colorScheme.tertiary
                                )
                        )
                    }
                }
            }

            // Show non-scheduled task indicators (dots)
            if (nonScheduledTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    nonScheduledTasks.take(3).forEach { task ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(horizontal = 1.dp)
                                .clip(CircleShape)
                                .background(
                                    when (task.category.uppercase()) {
                                        "HEALTH" -> ChipHealthText
                                        "WORK" -> ChipWorkText
                                        "MENTAL HEALTH" -> ChipMentalText
                                        else -> colorScheme.onSurfaceVariant
                                    }
                                )
                        )
                    }
                    if (nonScheduledTasks.size > 3) {
                        Text(
                            text = "+",
                            fontSize = 8.sp,
                            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// day view with date strip and today's tasks, schedule cards
@Composable
fun DayView(viewModel: CalendarViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val weekDates by viewModel.weekDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val isTodaysTasksExpanded by viewModel.isTodaysTasksExpanded.collectAsState()
    val isScheduleExpanded by viewModel.isScheduleExpanded.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Date strip
        DateStripRow(
            dates = weekDates,
            selectedDate = selectedDate,
            onDateClick = { viewModel.selectDate(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's Tasks Card
            item {
                CollapsibleTaskCard(
                    title = "Today's Tasks",
                    isExpanded = isTodaysTasksExpanded,
                    onToggleExpand = { viewModel.toggleTodaysTasksExpanded() },
                    tasks = tasksForSelectedDate,
                    onTaskToggle = { viewModel.toggleTask(it) },
                    onTaskDelete = { viewModel.deleteTask(it) }
                )
            }

            // Schedule Card
            item {
                CollapsibleScheduleCard(
                    isExpanded = isScheduleExpanded,
                    onToggleExpand = { viewModel.toggleScheduleExpanded() },
                    tasks = tasksForSelectedDate
                )
            }
        }
    }
}

@Composable
fun DateStripRow(
    dates: List<Date>,
    selectedDate: Date,
    onDateClick: (Date) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorScheme.surface,
                        colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                )
            )
            .border(1.dp, colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            DateChipItem(
                date = date,
                isSelected = isSameDay(date, selectedDate),
                onClick = { onDateClick(date) }
            )
        }
    }
}

@Composable
fun DateChipItem(
    date: Date,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dayOfWeekFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayOfMonthFormatter = remember { SimpleDateFormat("d", Locale.getDefault()) }
    
    val dayOfWeek = dayOfWeekFormatter.format(date)
    val dayOfMonth = dayOfMonthFormatter.format(date)

    Column(
        modifier = Modifier
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayOfMonth,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
        )
    }
}

@Composable
fun CollapsibleTaskCard(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    tasks: List<Task>,
    onTaskToggle: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )
    var selectedFilter by remember(tasks) { mutableStateOf(TaskFilter.ALL) }
    val availableCategories = remember {
        TaskCategoryFilter.entries
            .filter { it != TaskCategoryFilter.ALL }
            .sortedBy { it.label }
    }
    var selectedCategory by remember { mutableStateOf(TaskCategoryFilter.ALL) }

    val statusFilteredTasks = when (selectedFilter) {
        TaskFilter.ALL -> tasks
        TaskFilter.PENDING -> tasks.filter { !it.isChecked }
        TaskFilter.COMPLETED -> tasks.filter { it.isChecked }
    }

    val filteredTasks = if (selectedCategory == TaskCategoryFilter.ALL) {
        statusFilteredTasks
    } else {
        statusFilteredTasks.filter { TaskCategoryFilter.fromTaskCategory(it.category) == selectedCategory }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val pendingTasks = tasks.count { !it.isChecked }
            val completedTasks = tasks.count { it.isChecked }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(
                                text = "pending",
                                backgroundColor = PendingBadgeBg,
                                borderColor = PendingBadgeBorder,
                                contentColor = PendingBadgeBorder,
                                count = pendingTasks,
                                selected = selectedFilter == TaskFilter.PENDING,
                                onClick = {
                                    selectedFilter = if (selectedFilter == TaskFilter.PENDING) {
                                        TaskFilter.ALL
                                    } else {
                                        TaskFilter.PENDING
                                    }
                                }
                            )
                            StatusBadge(
                                text = "completed",
                                backgroundColor = CompletedBadgeBg,
                                borderColor = CompletedBadgeBorder,
                                contentColor = CompletedBadgeBorder,
                                count = completedTasks,
                                selected = selectedFilter == TaskFilter.COMPLETED,
                                onClick = {
                                    selectedFilter = if (selectedFilter == TaskFilter.COMPLETED) {
                                        TaskFilter.ALL
                                    } else {
                                        TaskFilter.COMPLETED
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TypeFilterDropdownBadge(
                            selectedCategory = selectedCategory,
                            categories = availableCategories,
                            onCategorySelected = { selectedCategory = it },
                            badgeBackgroundColor = FilterBadgeBg,
                            badgeBorderColor = FilterBadgeBorder,
                            badgeContentColor = BorderBlue
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selectedFilter) {
                                    TaskFilter.PENDING -> if (selectedCategory == TaskCategoryFilter.ALL) {
                                        "No pending tasks for this day"
                                    } else {
                                        "No pending ${selectedCategory.label} tasks for this day"
                                    }
                                    TaskFilter.COMPLETED -> if (selectedCategory == TaskCategoryFilter.ALL) {
                                        "No completed tasks for this day"
                                    } else {
                                        "No completed ${selectedCategory.label} tasks for this day"
                                    }
                                    TaskFilter.ALL -> if (selectedCategory == TaskCategoryFilter.ALL) {
                                        "No tasks for this day"
                                    } else {
                                        "No ${selectedCategory.label} tasks for this day"
                                    }
                                },
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        filteredTasks.forEachIndexed { index, task ->
                            TaskItemRow(
                                task = task,
                                onToggle = { onTaskToggle(task) },
                                onDelete = { onTaskDelete(task) }
                            )
                            if (index < filteredTasks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = BorderBlue.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    borderColor: Color,
    contentColor: Color,
    count: Int,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val badgeModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = badgeModifier,
        color = backgroundColor,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor.copy(alpha = if (selected) 1f else 0.55f))
    ) {
        Text(
            text = "$count $text",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor.copy(alpha = if (selected) 1f else 0.7f)
        )
    }
}

@Composable
fun TypeFilterDropdownBadge(
    selectedCategory: TaskCategoryFilter,
    categories: List<TaskCategoryFilter>,
    onCategorySelected: (TaskCategoryFilter) -> Unit,
    badgeBackgroundColor: Color = FilterBadgeBg,
    badgeBorderColor: Color = FilterBadgeBorder,
    badgeContentColor: Color = BorderBlue
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            color = badgeBackgroundColor,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, badgeBorderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Type: ${selectedCategory.label}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeContentColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Open type filter",
                    tint = badgeContentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(TaskCategoryFilter.ALL.label) },
                onClick = {
                    onCategorySelected(TaskCategoryFilter.ALL)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CollapsibleScheduleCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    tasks: List<Task>
) {
    val colorScheme = MaterialTheme.colorScheme
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Expandable content - Timeline
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    TimelineView(tasks = tasks)
                }
            }
        }
    }
}

@Composable
fun TimelineView(tasks: List<Task>) {
    val colorScheme = MaterialTheme.colorScheme
    val visibleStartMinutes = 0 * 60
    val visibleEndMinutes = 24 * 60
    val hourSlotHeight = 72.dp
    val scheduledTasks = remember(tasks) {
        tasks.filter {
            it.isScheduled && it.scheduleStartMinutes != null && it.scheduleEndMinutes != null
        }.sortedBy { it.scheduleStartMinutes ?: Int.MAX_VALUE }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Time blocks",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(hourSlotHeight * 24)
                .clip(RoundedCornerShape(18.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .border(1.dp, colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.width(56.dp)
            ) {
                repeat(24) { hourIndex ->
                    val hour = hourIndex
                    Box(
                        modifier = Modifier
                            .height(hourSlotHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = formatTimelineHour(hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Draw hour divider lines
                repeat(25) { hourIndex ->
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = hourSlotHeight * hourIndex),
                        color = colorScheme.outlineVariant.copy(alpha = 0.8f)
                    )
                }

                // Draw scheduled task blocks
                scheduledTasks.forEachIndexed { index, task ->
                    val rawStart = task.scheduleStartMinutes ?: return@forEachIndexed
                    val rawEnd = task.scheduleEndMinutes ?: return@forEachIndexed
                    val startMinutes = rawStart.coerceIn(visibleStartMinutes, visibleEndMinutes)
                    val endMinutes = rawEnd.coerceIn(visibleStartMinutes, visibleEndMinutes)
                    if (endMinutes <= startMinutes) return@forEachIndexed

                    val top = hourSlotHeight * ((startMinutes - visibleStartMinutes) / 60f)
                    val height = hourSlotHeight * ((endMinutes - startMinutes) / 60f)
                    val (blockBg, blockText) = categoryEventColors(task.category, colorScheme)

                    Box(
                        modifier = Modifier
                            .offset(x = 8.dp, y = top + 4.dp)
                            .fillMaxWidth()
                            .height(height - 8.dp)
                            .border(1.dp, blockBg.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(blockBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = blockText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatMinutesOfDay(startMinutes)} - ${formatMinutesOfDay(endMinutes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = blockText.copy(alpha = 0.82f)
                            )
                        }
                    }
                }
            }
        }

        // Show empty state message when no scheduled tasks
        if (scheduledTasks.isEmpty()) {
            Text(
                text = "No scheduled tasks for this day. Click a time slot to add one.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTimelineHour(hour: Int): String {
    return when (hour) {
        0 -> "12 AM"
        in 1..11 -> "$hour AM"
        12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

private fun formatMinutesOfDay(minutes: Int): String {
    val clampedMinutes = minutes.coerceIn(0, 23 * 60 + 59)
    val hour = clampedMinutes / 60
    val minute = clampedMinutes % 60
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, suffix)
}

private fun categoryEventColors(category: String, colorScheme: androidx.compose.material3.ColorScheme): Pair<Color, Color> {
    return when (category.trim().lowercase()) {
        "personal", "health" -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        "work" -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
        "university", "mental health" -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
        else -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val (chipBg, chipText) = when (task.category.uppercase()) {
        "HEALTH" -> ChipHealthBg to ChipHealthText
        "WORK" -> ChipWorkBg to ChipWorkText
        "MENTAL HEALTH" -> ChipMentalBg to ChipMentalText
        else -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = { isChecked ->
                if (isChecked && !task.isChecked) {
                    playCheckSound(context)
                }
                onToggle()
            },
            colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.onSurface
                )
                
                if (task.isScheduled) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Scheduled task",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.category,
                fontSize = 10.sp,
                color = chipText,
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}