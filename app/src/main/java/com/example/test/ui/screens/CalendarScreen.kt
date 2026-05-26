package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.components.TaskCategories
import com.example.test.ui.components.TaskItem
import com.example.test.ui.viewmodels.CalendarDay
import com.example.test.ui.viewmodels.CalendarViewModel
import com.example.test.ui.viewmodels.CalendarViewMode
import java.text.SimpleDateFormat
import java.util.*

// ─── Filter enum (used in CollapsibleTaskCard) ───────────
private enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

// Helper to determine depth levels for transitions
private fun CalendarViewMode.getDepth(): Int = when (this) {
    CalendarViewMode.YEAR -> 0
    CalendarViewMode.MONTH -> 1
    CalendarViewMode.DAY -> 2
}

// ─── Custom swipe detector modifier ───────────────────────
@Composable
fun Modifier.calendarSwipeDetector(
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
): Modifier {
    return this.pointerInput(Unit) {
        var totalX = 0f
        var totalY = 0f
        detectDragGestures(
            onDragStart = {
                totalX = 0f
                totalY = 0f
            },
            onDragEnd = {
                val threshold = 120f // Pixels needed to trigger action
                if (Math.abs(totalX) > Math.abs(totalY)) {
                    if (totalX > threshold) onSwipeRight?.invoke()
                    else if (totalX < -threshold) onSwipeLeft?.invoke()
                } else {
                    if (totalY > threshold) onSwipeDown?.invoke()
                    else if (totalY < -threshold) onSwipeUp?.invoke()
                }
            },
            onDragCancel = {
                totalX = 0f
                totalY = 0f
            },
            onDrag = { change, dragAmount ->
                change.consume()
                totalX += dragAmount.x
                totalY += dragAmount.y
            }
        )
    }
}

// ─── Custom nested scroll connection to detect overscroll ─
@Composable
fun rememberOverscrollConnection(
    lazyListState: androidx.compose.foundation.lazy.LazyListState? = null,
    onPullDown: (() -> Unit)?,
    onPullUp: (() -> Unit)?,
    threshold: Float = 140f
): NestedScrollConnection {
    var accumulatedOffset by remember { mutableFloatStateOf(0f) }

    return remember(onPullDown, onPullUp, threshold) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    val unconsumedY = available.y

                    // If pulling down at the top of the list
                    if (unconsumedY > 0 && onPullDown != null) {
                        accumulatedOffset += unconsumedY
                        if (accumulatedOffset >= threshold) {
                            onPullDown()
                            accumulatedOffset = 0f
                        }
                    }
                    // If pulling up at the bottom of the list
                    else if (unconsumedY < 0 && onPullUp != null) {
                        accumulatedOffset += unconsumedY
                        if (accumulatedOffset <= -threshold) {
                            onPullUp()
                            accumulatedOffset = 0f
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                accumulatedOffset = 0f // Reset offset accumulation when gesture finishes
                return super.onPostFling(consumed, available)
            }
        }
    }
}

// ─── Main CalendarScreen ──────────────────────────────────
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateHome: () -> Unit = {},
    onAddTask: (Task?) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val viewMode by viewModel.viewMode.collectAsState()
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

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.background.copy(alpha = 0.95f))
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = colorScheme.primary
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 2.dp,
                    color = colorScheme.primary
                )
            }
        },
        floatingActionButton = {
            if (viewMode != CalendarViewMode.YEAR) {
                FloatingActionButton(
                    onClick = { onAddTask(null) },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = colorScheme.background
    ) { padding ->
        // Fine-tuned spring dynamics to mirror physics-based zooming transitions
        val springFloat = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val springOffset = spring<IntOffset>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                val isZoomingIn = targetState.getDepth() > initialState.getDepth()
                if (isZoomingIn) {
                    // Moving deeper (e.g. YEAR -> MONTH or MONTH -> DAY)
                    (slideInVertically(animationSpec = springOffset) { it / 4 } +
                            fadeIn(animationSpec = springFloat) +
                            scaleIn(initialScale = 0.9f, animationSpec = springFloat)) togetherWith
                            (slideOutVertically(animationSpec = springOffset) { -it / 4 } +
                                    fadeOut(animationSpec = springFloat) +
                                    scaleOut(targetScale = 1.1f, animationSpec = springFloat))
                } else {
                    // Zooming out (e.g. DAY -> MONTH or MONTH -> YEAR)
                    (slideInVertically(animationSpec = springOffset) { -it / 4 } +
                            fadeIn(animationSpec = springFloat) +
                            scaleIn(initialScale = 1.1f, animationSpec = springFloat)) togetherWith
                            (slideOutVertically(animationSpec = springOffset) { it / 4 } +
                                    fadeOut(animationSpec = springFloat) +
                                    scaleOut(targetScale = 0.9f, animationSpec = springFloat))
                }
            },
            label = "viewTransition"
        ) { mode ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colorScheme.background)
            ) {
                when (mode) {
                    CalendarViewMode.YEAR -> YearView(
                        viewModel = viewModel,
                        onSwipeUpToMonth = { viewModel.setViewMode(CalendarViewMode.MONTH) }
                    )
                    CalendarViewMode.MONTH -> MonthView(
                        viewModel = viewModel,
                        onAddTask = onAddTask,
                        onNavigateToDay = { viewModel.setViewMode(CalendarViewMode.DAY) }
                    )
                    CalendarViewMode.DAY -> DayView(
                        viewModel = viewModel,
                        onAddTask = onAddTask,
                        onPullDownToMonth = { viewModel.setViewMode(CalendarViewMode.MONTH) }
                    )
                }
            }
        }
    }
}

// ─── Year View ────────────────────────────────────────────
@Composable
fun YearView(
    viewModel: CalendarViewModel,
    onSwipeUpToMonth: () -> Unit
) {
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
            .calendarSwipeDetector(
                onSwipeLeft = { viewModel.nextYear() },
                onSwipeRight = { viewModel.previousYear() },
                onSwipeUp = onSwipeUpToMonth
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousYear() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year", tint = colorScheme.primary)
            }
            Text(
                text = currentYear.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.nextYear() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Year", tint = colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

// ─── Month Card (used in YearView) ───────────────────────
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

// ─── Month View ───────────────────────────────────────────
@Composable
fun MonthView(
    viewModel: CalendarViewModel,
    onAddTask: (Task?) -> Unit = {},
    onNavigateToDay: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val calendarDays by viewModel.calendarDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val selectedDateFormatter = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
    val tasksListState = rememberLazyListState()

    val nestedScrollConnection = rememberOverscrollConnection(
        lazyListState = tasksListState,
        onPullDown = { viewModel.setViewMode(CalendarViewMode.YEAR) },
        onPullUp = { onNavigateToDay() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .calendarSwipeDetector(
                onSwipeLeft = { viewModel.nextMonth() },
                onSwipeRight = { viewModel.previousMonth() },
                onSwipeUp = onNavigateToDay,
                onSwipeDown = { viewModel.setViewMode(CalendarViewMode.YEAR) }
            )
    ) {
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
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = colorScheme.primary)
            }
            IconButton(
                onClick = { viewModel.nextMonth() },
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = colorScheme.primary)
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
                    onClick = { viewModel.selectDate(day.date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Outer box is no longer globally clickable, allowing normal scrolling mechanics
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Text(
                    text = "Tasks for ${selectedDateFormatter.format(selectedDate)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (tasksForSelectedDate.isEmpty()) {
                    // Clickability is now scoped exclusively to this empty state card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDay() },
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
                        state = tasksListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .nestedScroll(nestedScrollConnection),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasksForSelectedDate.size, key = { tasksForSelectedDate[it].id }) { index ->
                            val task = tasksForSelectedDate[index]
                            TaskItem(
                                task = task,
                                checked = task.isChecked,
                                onCheckedChange = { viewModel.toggleTask(task) },
                                onEditTask = onAddTask,
                                onDelete = { viewModel.deleteTask(task) }
                            )
                            if (index < tasksForSelectedDate.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = colorScheme.outline.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Day View ─────────────────────────────────────────────
@Composable
fun DayView(
    viewModel: CalendarViewModel,
    onAddTask: (Task?) -> Unit = {},
    onPullDownToMonth: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val weekDates by viewModel.weekDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsState()
    val isTodaysTasksExpanded by viewModel.isTodaysTasksExpanded.collectAsState()
    val isScheduleExpanded by viewModel.isScheduleExpanded.collectAsState()
    val dayListState = rememberLazyListState()

    val nestedScrollConnection = rememberOverscrollConnection(
        lazyListState = dayListState,
        onPullDown = onPullDownToMonth,
        onPullUp = null
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .calendarSwipeDetector(
                onSwipeDown = onPullDownToMonth
            )
    ) {
        DateStripRow(
            dates = weekDates,
            selectedDate = selectedDate,
            onDateClick = { viewModel.selectDate(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            state = dayListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CollapsibleTaskCard(
                    title = "Today's Tasks",
                    isExpanded = isTodaysTasksExpanded,
                    onToggleExpand = { viewModel.toggleTodaysTasksExpanded() },
                    tasks = tasksForSelectedDate,
                    onTaskToggle = { viewModel.toggleTask(it) },
                    onTaskDelete = { viewModel.deleteTask(it) },
                    onTaskEdit = onAddTask
                )
            }

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

// ─── Helper composables ───────────────────────────────────

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
    val scheduledTasks = day.tasks.filter { it.isScheduled }
    val nonScheduledTasks = day.tasks.filter { !it.isScheduled }
    val incompleteScheduledCount = scheduledTasks.count { !it.isChecked }
    val completeScheduledCount = scheduledTasks.count { it.isChecked }

    val backgroundColor = when {
        isSelected -> colorScheme.primary
        scheduledTasks.isNotEmpty() && incompleteScheduledCount > 0 ->
            colorScheme.error.copy(alpha = 0.2f)
        scheduledTasks.isNotEmpty() && completeScheduledCount > 0 && incompleteScheduledCount == 0 ->
            colorScheme.tertiary.copy(alpha = 0.2f)
        day.isToday -> colorScheme.primary.copy(alpha = 0.2f)
        else -> colorScheme.surface
    }

    val isDark = isSystemInDarkTheme()
    val textColor = when {
        isSelected -> colorScheme.onPrimary
        !day.isCurrentMonth -> (if (isDark) Color.White else colorScheme.onSurfaceVariant).copy(alpha = 0.4f)
        else -> if (isDark) Color.White else colorScheme.onSurface
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
            if (scheduledTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (incompleteScheduledCount > 0) {
                        Box(
                            modifier = Modifier.height(2.dp).width(4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isSelected) colorScheme.onPrimary else if (isDark) Color.White else colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    if (completeScheduledCount > 0) {
                        Box(
                            modifier = Modifier.height(2.dp).width(4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isSelected) colorScheme.onPrimary else if (isDark) Color.White else colorScheme.tertiary)
                        )
                    }
                }
            }
            if (nonScheduledTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    nonScheduledTasks.take(3).forEach { task ->
                        Box(
                            modifier = Modifier.size(4.dp).padding(horizontal = 1.dp)
                                .clip(CircleShape)
                                .background(
                                    when (task.category.uppercase()) {
                                        "HEALTH" -> if (isDark) Color.White else colorScheme.primary
                                        "WORK" -> if (isDark) Color.White else colorScheme.secondary
                                        "MENTAL HEALTH" -> if (isDark) Color.White else colorScheme.tertiary
                                        else -> if (isDark) Color.White else colorScheme.onSurfaceVariant
                                    }
                                )
                        )
                    }
                    if (nonScheduledTasks.size > 3) {
                        Text(
                            text = "+",
                            fontSize = 8.sp,
                            color = if (isSelected) colorScheme.onPrimary else if (isDark) Color.White else colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
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
                    colors = listOf(colorScheme.surface, colorScheme.surfaceVariant.copy(alpha = 0.55f))
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
fun DateChipItem(date: Date, isSelected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val dayOfWeekFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayOfMonthFormatter = remember { SimpleDateFormat("d", Locale.getDefault()) }
    val dayOfWeek = dayOfWeekFormatter.format(date)
    val dayOfMonth = dayOfMonthFormatter.format(date)

    Column(
        modifier = Modifier
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) colorScheme.primary else colorScheme.surfaceVariant)
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
    onTaskDelete: (Task) -> Unit,
    onTaskEdit: (Task?) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val rotationAngle by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")
    var selectedFilter by remember(tasks) { mutableStateOf(TaskFilter.ALL) }
    val availableCategories = remember { TaskCategories.ALL_CATEGORIES }
    var selectedCategory by remember { mutableStateOf(TaskCategories.ALL) }

    val statusFilteredTasks = when (selectedFilter) {
        TaskFilter.ALL -> tasks
        TaskFilter.PENDING -> tasks.filter { !it.isChecked }
        TaskFilter.COMPLETED -> tasks.filter { it.isChecked }
    }
    val filteredTasks = if (selectedCategory == TaskCategories.ALL) {
        statusFilteredTasks
    } else {
        statusFilteredTasks.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            val pendingTasks = tasks.count { !it.isChecked }
            val completedTasks = tasks.count { it.isChecked }
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    }
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge(
                                    text = "pending", backgroundColor = colorScheme.errorContainer.copy(alpha = 0.4f),
                                    borderColor = colorScheme.error, contentColor = if (isDark) Color.White else colorScheme.error,
                                    count = pendingTasks,
                                    selected = selectedFilter == TaskFilter.PENDING,
                                    onClick = { selectedFilter = if (selectedFilter == TaskFilter.PENDING) TaskFilter.ALL else TaskFilter.PENDING }
                                )
                                StatusBadge(
                                    text = "completed", backgroundColor = colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                    borderColor = colorScheme.tertiary, contentColor = if (isDark) Color.White else colorScheme.tertiary,
                                    count = completedTasks,
                                    selected = selectedFilter == TaskFilter.COMPLETED,
                                    onClick = { selectedFilter = if (selectedFilter == TaskFilter.COMPLETED) TaskFilter.ALL else TaskFilter.COMPLETED }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TypeFilterDropdownBadge(
                                selectedCategory = selectedCategory,
                                categories = availableCategories,
                                onCategorySelected = { selectedCategory = it }
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isDark) Color.White else colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (filteredTasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = when (selectedFilter) {
                                    TaskFilter.PENDING -> if (selectedCategory == TaskCategories.ALL) "No pending tasks for this day" else "No pending $selectedCategory tasks for this day"
                                    TaskFilter.COMPLETED -> if (selectedCategory == TaskCategories.ALL) "No completed tasks for this day" else "No completed $selectedCategory tasks for this day"
                                    TaskFilter.ALL -> if (selectedCategory == TaskCategories.ALL) "No tasks for this day" else "No $selectedCategory tasks for this day"
                                },
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        filteredTasks.forEachIndexed { index, task ->
                            TaskItem(
                                task = task, checked = task.isChecked,
                                onCheckedChange = { onTaskToggle(task) },
                                onEditTask = onTaskEdit,
                                onDelete = { onTaskDelete(task) }
                            )
                            if (index < filteredTasks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 1.dp,
                                    color = colorScheme.outline.copy(alpha = 0.3f)
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
    val badgeModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
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
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            color = colorScheme.surfaceVariant,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Type: $selectedCategory",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Open type filter",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(TaskCategories.ALL) },
                onClick = { onCategorySelected(TaskCategories.ALL); expanded = false }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = { onCategorySelected(category); expanded = false }
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
    val rotationAngle by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")
    Card(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                }
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    if (isExpanded) "Collapse" else "Expand",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
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
    val hourSlotHeight = 72.dp
    val scheduledTasks = remember(tasks) {
        tasks.filter { it.isScheduled && it.scheduleStartMinutes != null && it.scheduleEndMinutes != null }
            .sortedBy { it.scheduleStartMinutes ?: Int.MAX_VALUE }
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
            Column(modifier = Modifier.width(56.dp)) {
                repeat(24) { hourIndex ->
                    Box(
                        modifier = Modifier.height(hourSlotHeight).fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = formatTimelineHour(hourIndex),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                repeat(25) { hourIndex ->
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().offset(y = hourSlotHeight * hourIndex),
                        color = colorScheme.outlineVariant.copy(alpha = 0.8f)
                    )
                }

                scheduledTasks.forEach { task ->
                    val start = task.scheduleStartMinutes ?: 0
                    val end = task.scheduleEndMinutes ?: 0
                    val top = hourSlotHeight * (start / 60f)
                    val height = hourSlotHeight * ((end - start) / 60f)
                    val (bg, text) = categoryEventColors(task.category, colorScheme)

                    Box(
                        modifier = Modifier
                            .offset(x = 8.dp, y = top + 4.dp)
                            .fillMaxWidth()
                            .height(height - 8.dp)
                            .border(1.dp, bg.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(bg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatMinutesOfDay(start)} - ${formatMinutesOfDay(end)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = text.copy(alpha = 0.82f)
                            )
                        }
                    }
                }
            }
        }

        if (scheduledTasks.isEmpty()) {
            Text(
                text = "No scheduled tasks for this day.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Utility functions ────────────────────────────────────
private fun formatTimelineHour(hour: Int): String = when (hour) {
    0 -> "12 AM"
    in 1..11 -> "$hour AM"
    12 -> "12 PM"
    else -> "${hour - 12} PM"
}

private fun formatMinutesOfDay(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    val suffix = if (h < 12) "AM" else "PM"
    val displayHour = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, m, suffix)
}

private fun categoryEventColors(category: String, colorScheme: androidx.compose.material3.ColorScheme): Pair<Color, Color> {
    val isDark = colorScheme.surface.luminance() < 0.5f
    return when (category.trim().lowercase()) {
        "personal", "health" -> colorScheme.primaryContainer to (if (isDark) Color.White else colorScheme.onPrimaryContainer)
        "work" -> colorScheme.secondaryContainer to (if (isDark) Color.White else colorScheme.onSecondaryContainer)
        "university", "mental health" -> colorScheme.tertiaryContainer to (if (isDark) Color.White else colorScheme.onTertiaryContainer)
        else -> colorScheme.surfaceVariant to (if (isDark) Color.White else colorScheme.onSurfaceVariant)
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}