package com.example.test.ui.components

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val QuickAddCategories = listOf("Personal", "Work", "University", "Other")

// Enum to manage views inside the single sheet window to prevent laggy nested overlays
private enum class QuickAddTaskSheetView {
    MAIN,
    CATEGORY_PICKER,
    SCHEDULE_PICKER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTaskSheet(
    taskViewModel: TaskViewModel,
    onDismiss: () -> Unit,
    onOpenFullEditor: () -> Unit,
    initialDateMillis: Long? = null
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── State ────────────────────────────────────────────────────────────────
    var currentView by remember { mutableStateOf(QuickAddTaskSheetView.MAIN) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDescription by remember { mutableStateOf(false) }

    val todayMillis = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis ?: todayMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    var notificationMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    var isScheduled by remember { mutableStateOf(false) }
    var startMinutes by remember { mutableStateOf(9 * 60) }
    var endMinutes by remember { mutableStateOf(10 * 60) }

    val focusRequester = remember { FocusRequester() }

    // ── Formatters ───────────────────────────────────────────────────────────
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayShortFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val todayStr = remember { dateFormatter.format(Date()) }
    val isToday = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis)) == todayStr
    }
    val dateLabel = remember(selectedDateMillis, isToday) {
        if (isToday) "Today" else displayShortFormatter.format(Date(selectedDateMillis))
    }

    fun formatMinutes(minutes: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
        }
        return timeFormatter.format(cal.time)
    }

    fun pickTime(initialMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, h, m -> onPicked(h * 60 + m) },
            initialMinutes / 60,
            initialMinutes % 60,
            false
        ).show()
    }

    // ── Date picker dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = dpState) }
    }

    // ── Auto-focus (with a slight delay to allow smooth sheet slide-up) ──────
    LaunchedEffect(Unit) {
        delay(250)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = cs.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(cs.onSurface.copy(alpha = 0.15f))
            )
        },
        tonalElevation = 0.dp
    ) {
        // Switch between sub-picker views smoothly inside the same layout hierarchy
        AnimatedContent(
            targetState = currentView,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "sheet_view_transition"
        ) { view ->
            when (view) {
                QuickAddTaskSheetView.MAIN -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize() // Smooth transition when description expands or active chips render
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        // ── Input area ───────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            // Title field
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "What would you like to do?",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = cs.onSurface.copy(alpha = 0.35f)
                                    )
                                }
                                BasicTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    textStyle = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = cs.onSurface
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(cs.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    onTextLayout = {
                                        if (title.isNotEmpty() && !showDescription) {
                                            showDescription = true
                                        }
                                    }
                                )
                            }

                            // Description field — appears after user starts typing
                            AnimatedVisibility(
                                visible = showDescription,
                                enter = expandVertically(
                                    animationSpec = tween(220, easing = EaseOutCubic)
                                ) + fadeIn(tween(180))
                            ) {
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (description.isEmpty()) {
                                            Text(
                                                text = "Add a description…",
                                                fontSize = 14.sp,
                                                color = cs.onSurface.copy(alpha = 0.28f)
                                            )
                                        }
                                        BasicTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                color = cs.onSurface.copy(alpha = 0.75f)
                                            ),
                                            cursorBrush = SolidColor(cs.primary),
                                            maxLines = 3,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ── Active chips row ─────────────────────────────
                            val hasChips = !isToday || notificationMinutes != null ||
                                    selectedCategory != null || isScheduled
                            AnimatedVisibility(visible = hasChips) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    if (!isToday) {
                                        item {
                                            ActiveChip(
                                                label = dateLabel,
                                                color = cs.primaryContainer,
                                                labelColor = if (isDark) Color.White else cs.onPrimaryContainer,
                                                onDismiss = {
                                                    selectedDateMillis = todayMillis
                                                }
                                            )
                                        }
                                    }
                                    notificationMinutes?.let { mins ->
                                        item {
                                            ActiveChip(
                                                label = formatMinutes(mins),
                                                color = cs.secondaryContainer,
                                                labelColor = if (isDark) Color.White else cs.onSecondaryContainer,
                                                onDismiss = { notificationMinutes = null }
                                            )
                                        }
                                    }
                                    selectedCategory?.let { cat ->
                                        item {
                                            ActiveChip(
                                                label = cat,
                                                color = cs.tertiaryContainer,
                                                labelColor = if (isDark) Color.White else cs.onTertiaryContainer,
                                                onDismiss = { selectedCategory = null }
                                            )
                                        }
                                    }
                                    if (isScheduled) {
                                        item {
                                            ActiveChip(
                                                label = "${formatMinutes(startMinutes)} – ${formatMinutes(endMinutes)}",
                                                color = cs.primary.copy(alpha = 0.15f),
                                                labelColor = if (isDark) Color.White else cs.primary,
                                                onDismiss = { isScheduled = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = cs.outline.copy(alpha = 0.14f),
                            thickness = 1.dp
                        )

                        // ── Bottom toolbar ───────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date icon
                            ToolbarIconButton(
                                icon = Icons.Outlined.CalendarMonth,
                                isActive = !isToday,
                                activeColor = cs.primary,
                                inactiveColor = cs.onSurface.copy(alpha = 0.5f),
                                onClick = { showDatePicker = true }
                            )

                            // Clock / notification icon
                            ToolbarIconButton(
                                icon = Icons.Outlined.AccessTime,
                                isActive = notificationMinutes != null,
                                activeColor = cs.secondary,
                                inactiveColor = cs.onSurface.copy(alpha = 0.5f),
                                onClick = {
                                    val init = notificationMinutes
                                        ?: (Calendar.getInstance().run {
                                            get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)
                                        })
                                    pickTime(init) { notificationMinutes = it }
                                }
                            )

                            // Tag / category icon (Switches to internal category page)
                            ToolbarIconButton(
                                icon = Icons.Outlined.Label,
                                isActive = selectedCategory != null,
                                activeColor = cs.tertiary,
                                inactiveColor = cs.onSurface.copy(alpha = 0.5f),
                                onClick = { currentView = QuickAddTaskSheetView.CATEGORY_PICKER }
                            )

                            // Schedule icon (Switches to internal schedule page)
                            ToolbarIconButton(
                                icon = Icons.Outlined.Schedule,
                                isActive = isScheduled,
                                activeColor = cs.primary,
                                inactiveColor = cs.onSurface.copy(alpha = 0.5f),
                                onClick = { currentView = QuickAddTaskSheetView.SCHEDULE_PICKER }
                            )

                            // Expand to full editor
                            TextButton(
                                onClick = {
                                    scope.launch { sheetState.hide() }
                                        .invokeOnCompletion { onOpenFullEditor() }
                                },
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    "More",
                                    fontSize = 13.sp,
                                    color = cs.onSurface.copy(alpha = 0.45f)
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            // Send button
                            val sendEnabled = title.isNotBlank()
                            val sendScale by animateFloatAsState(
                                targetValue = if (sendEnabled) 1f else 0.82f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "send_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .scale(sendScale)
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (sendEnabled) cs.primary
                                        else cs.onSurface.copy(alpha = 0.08f)
                                    )
                                    .clickable(
                                        enabled = sendEnabled,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val dateStr = dateFormatter.format(Date(selectedDateMillis))
                                        val normalizedEnd = if (isScheduled && endMinutes <= startMinutes) {
                                            (startMinutes + 60).coerceAtMost(23 * 60 + 59)
                                        } else endMinutes

                                        taskViewModel.addTask(
                                            title = title.trim(),
                                            description = description.trim(),
                                            category = selectedCategory ?: "Personal",
                                            date = dateStr,
                                            isScheduled = isScheduled,
                                            scheduleStartMinutes = if (isScheduled) startMinutes else null,
                                            scheduleEndMinutes = if (isScheduled) normalizedEnd else null,
                                            notificationMinutes = notificationMinutes
                                        )
                                        scope.launch { sheetState.hide() }
                                            .invokeOnCompletion { onDismiss() }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Add task",
                                    tint = if (sendEnabled) {
                                        if (isDark) Color.White else cs.onPrimary
                                    } else cs.onSurface.copy(alpha = 0.25f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                QuickAddTaskSheetView.CATEGORY_PICKER -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            TextButton(onClick = { currentView = QuickAddTaskSheetView.MAIN }) {
                                Text("Back")
                            }
                        }
                        QuickAddCategories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCategory = if (isSelected) null else cat
                                        currentView = QuickAddTaskSheetView.MAIN
                                    }
                                    .background(
                                        if (isSelected) cs.primaryContainer
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    cat,
                                    fontSize = 15.sp,
                                    color = if (isSelected) {
                                        if (isDark) Color.White else cs.onPrimaryContainer
                                    } else cs.onSurface,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                QuickAddTaskSheetView.SCHEDULE_PICKER -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Schedule task",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Text(
                                    "Show as a time block on the calendar",
                                    fontSize = 12.sp,
                                    color = cs.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            Switch(
                                checked = isScheduled,
                                onCheckedChange = { isScheduled = it }
                            )
                        }

                        AnimatedVisibility(
                            visible = isScheduled,
                            enter = expandVertically(tween(200)) + fadeIn(tween(150))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { pickTime(startMinutes) { startMinutes = it } },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Start", fontSize = 11.sp, color = cs.onSurface.copy(alpha = 0.6f))
                                        Text(
                                            formatMinutes(startMinutes),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { pickTime(endMinutes) { endMinutes = it } },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("End", fontSize = 11.sp, color = cs.onSurface.copy(alpha = 0.6f))
                                        Text(
                                            formatMinutes(endMinutes),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { currentView = QuickAddTaskSheetView.MAIN },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

// ── ToolbarIconButton ─────────────────────────────────────────────────────────

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(180),
        label = "icon_tint"
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ── ActiveChip ────────────────────────────────────────────────────────────────

@Composable
private fun ActiveChip(
    label: String,
    color: Color,
    labelColor: Color,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 12.sp, color = labelColor, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(labelColor.copy(alpha = 0.15f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("×", fontSize = 12.sp, color = labelColor, fontWeight = FontWeight.Bold)
        }
    }
}