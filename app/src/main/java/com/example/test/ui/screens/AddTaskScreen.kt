package com.example.test.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Categories = listOf("Personal", "Work", "University", "Other")

private val CategoryIcons = mapOf(
    "Personal"   to Icons.Outlined.Person,
    "Work"       to Icons.Outlined.Work,
    "University" to Icons.Outlined.School,
    "Other"      to Icons.Outlined.Category
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    taskViewModel: TaskViewModel,
    onClose: () -> Unit,
    initialDateMillis: Long? = null,
    existingTask: Task? = null
) {
    val cs    = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    // ── Pre-fill ─────────────────────────────────────────────────────────────
    val existingDateMillis = remember(existingTask) {
        existingTask?.date?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
        }
    }

    // ── State ────────────────────────────────────────────────────────────────
    var title       by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }
    var category    by remember(existingTask) { mutableStateOf(existingTask?.category ?: Categories.first()) }

    val todayMillis = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis by remember(existingTask, initialDateMillis) {
        mutableStateOf(existingDateMillis ?: initialDateMillis ?: todayMillis)
    }
    var showDatePicker by remember { mutableStateOf(false) }

    var isScheduled  by remember(existingTask) { mutableStateOf(existingTask?.isScheduled ?: false) }
    var startMinutes by remember(existingTask) { mutableStateOf(existingTask?.scheduleStartMinutes ?: 9 * 60) }
    var endMinutes   by remember(existingTask) { mutableStateOf(existingTask?.scheduleEndMinutes   ?: 10 * 60) }

    var notificationMinutes by remember(existingTask) { mutableStateOf(existingTask?.notificationMinutes) }

    // ── Formatters ───────────────────────────────────────────────────────────
    val dateFormatter    = remember { SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("EEE, MMM d",  Locale.getDefault()) }
    val timeFormatter    = remember { SimpleDateFormat("h:mm a",      Locale.getDefault()) }

    val todayStr    = remember { dateFormatter.format(Date()) }
    val date        = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }
    val isToday     = date == todayStr
    val dateDisplay = remember(selectedDateMillis) {
        if (isToday) "Today" else displayFormatter.format(Date(selectedDateMillis))
    }

    fun formatMinutes(m: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60)
        }
        return timeFormatter.format(cal.time)
    }

    fun pickTime(initialMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(context, { _, h, min -> onPicked(h * 60 + min) },
            initialMinutes / 60, initialMinutes % 60, false).show()
    }

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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    // ── Colors ───────────────────────────────────────────────────────────────
    val surfaceCard  = if (isDark) cs.surface else cs.surface
    val accentColor  = cs.primary
    val labelAlpha   = if (isDark) 0.55f else 0.45f
    val divColor     = cs.outline.copy(alpha = if (isDark) 0.18f else 0.12f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {

        // ── Scrollable content ────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp, start = 16.dp, end = 16.dp, bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Top header ────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = if (existingTask == null) "New Task" else "Edit Task",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = cs.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = if (existingTask == null) "What's on your mind?" else "Update your task details",
                            fontSize = 14.sp,
                            color = cs.onBackground.copy(alpha = 0.45f),
                            letterSpacing = 0.1.sp
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cs.onSurface.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close, "Close",
                                modifier = Modifier.size(18.dp),
                                tint = cs.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // ── Title & Description card ──────────────────────────────────
            item {
                SectionCard(isDark = isDark, cs = cs) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SectionIcon(
                                icon = Icons.Outlined.EditNote,
                                tint = accentColor,
                                background = accentColor.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Task",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cs.onSurface.copy(alpha = labelAlpha),
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        // Title
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (title.isEmpty()) {
                                Text(
                                    "What would you like to do?",
                                    fontSize = 16.sp,
                                    color = cs.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface,
                                    lineHeight = 22.sp
                                ),
                                cursorBrush = SolidColor(accentColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = divColor)
                        Spacer(Modifier.height(10.dp))

                        // Description
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (description.isEmpty()) {
                                Text(
                                    "Add a note or description…",
                                    fontSize = 13.sp,
                                    color = cs.onSurface.copy(alpha = 0.28f)
                                )
                            }
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = cs.onSurface.copy(alpha = 0.75f),
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = SolidColor(accentColor),
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Category card ─────────────────────────────────────────────
            item {
                SectionCard(isDark = isDark, cs = cs) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(
                            icon = Icons.Outlined.Label,
                            label = "CATEGORY",
                            tint = cs.tertiary,
                            background = cs.tertiary.copy(alpha = 0.1f),
                            cs = cs,
                            labelAlpha = labelAlpha
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Categories) { cat ->
                                val selected = cat == category
                                val catIcon  = CategoryIcons[cat] ?: Icons.Outlined.Category
                                val bg       = if (selected) accentColor else cs.surfaceVariant
                                val fg       = if (selected) {
                                    if (isDark) Color.White else cs.onPrimary
                                } else cs.onSurfaceVariant.copy(alpha = 0.8f)

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(bg)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { category = cat }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(catIcon, null, tint = fg, modifier = Modifier.size(14.dp))
                                    Text(cat, fontSize = 13.sp, color = fg, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // ── Date & Notification card ──────────────────────────────────
            item {
                SectionCard(isDark = isDark, cs = cs) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "DATE & REMINDER",
                            tint = cs.secondary,
                            background = cs.secondary.copy(alpha = 0.1f),
                            cs = cs,
                            labelAlpha = labelAlpha
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Date picker button
                            DetailChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.CalendarToday,
                                label = dateDisplay,
                                isActive = !isToday,
                                activeContainerColor = cs.secondaryContainer,
                                activeLabelColor = if (isDark) Color.White else cs.onSecondaryContainer,
                                inactiveContainerColor = cs.surfaceVariant,
                                inactiveLabelColor = cs.onSurfaceVariant,
                                onClick = { showDatePicker = true },
                                onClear = null // date can't be "cleared" — always has a value
                            )
                            // Notification picker button
                            DetailChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.NotificationsNone,
                                label = notificationMinutes?.let { formatMinutes(it) } ?: "Remind me",
                                isActive = notificationMinutes != null,
                                activeContainerColor = cs.primaryContainer,
                                activeLabelColor = if (isDark) Color.White else cs.onPrimaryContainer,
                                inactiveContainerColor = cs.surfaceVariant,
                                inactiveLabelColor = cs.onSurfaceVariant,
                                onClick = {
                                    val init = notificationMinutes
                                        ?: Calendar.getInstance().run { get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE) }
                                    pickTime(init) { notificationMinutes = it }
                                },
                                onClear = { notificationMinutes = null }
                            )
                        }
                    }
                }
            }

            // ── Schedule card ─────────────────────────────────────────────
            item {
                SectionCard(isDark = isDark, cs = cs) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SectionIcon(
                                    icon = Icons.Outlined.Schedule,
                                    tint = if (isScheduled) accentColor else cs.onSurface.copy(alpha = 0.4f),
                                    background = if (isScheduled) accentColor.copy(alpha = 0.1f)
                                    else cs.onSurface.copy(alpha = 0.06f)
                                )
                                Column {
                                    Text(
                                        "Schedule",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = cs.onSurface
                                    )
                                    Text(
                                        "Show as a time block on calendar",
                                        fontSize = 11.sp,
                                        color = cs.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            Switch(
                                checked = isScheduled,
                                onCheckedChange = { isScheduled = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = accentColor,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }

                        // ── Time pickers (animated) ──────────────────────
                        AnimatedVisibility(
                            visible = isScheduled,
                            enter = expandVertically(
                                animationSpec = tween(240, easing = EaseOutCubic)
                            ) + fadeIn(tween(200)),
                            exit = shrinkVertically(
                                animationSpec = tween(200, easing = EaseInCubic)
                            ) + fadeOut(tween(150))
                        ) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = divColor)
                                Spacer(Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TimeBlock(
                                        modifier = Modifier.weight(1f),
                                        label = "Starts",
                                        time = formatMinutes(startMinutes),
                                        color = accentColor,
                                        cs = cs,
                                        isDark = isDark,
                                        onClick = { pickTime(startMinutes) { startMinutes = it } }
                                    )
                                    TimeBlock(
                                        modifier = Modifier.weight(1f),
                                        label = "Ends",
                                        time = formatMinutes(endMinutes),
                                        color = cs.secondary,
                                        cs = cs,
                                        isDark = isDark,
                                        onClick = { pickTime(endMinutes) { endMinutes = it } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Floating save button ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Gradient fade behind button so content doesn't bleed through
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cs.background.copy(alpha = 0.85f),
                                cs.background
                            )
                        )
                    )
            )
            val saveEnabled = title.isNotBlank()
            val btnScale by animateFloatAsState(
                targetValue = if (saveEnabled) 1f else 0.95f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "btn_scale"
            )
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val normalizedEnd = if (isScheduled && endMinutes <= startMinutes)
                        (startMinutes + 60).coerceAtMost(23 * 60 + 59) else endMinutes

                    if (existingTask != null) {
                        taskViewModel.updateTask(
                            existingTask.copy(
                                title       = title,
                                description = description,
                                category    = category,
                                date        = date,
                                isScheduled = isScheduled,
                                scheduleStartMinutes = if (isScheduled) startMinutes else null,
                                scheduleEndMinutes   = if (isScheduled) normalizedEnd else null,
                                notificationMinutes  = notificationMinutes
                            )
                        )
                    } else {
                        taskViewModel.addTask(
                            title       = title,
                            description = description,
                            category    = category,
                            date        = date,
                            isScheduled = isScheduled,
                            scheduleStartMinutes = if (isScheduled) startMinutes else null,
                            scheduleEndMinutes   = if (isScheduled) normalizedEnd else null,
                            notificationMinutes  = notificationMinutes
                        )
                    }
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(56.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(16.dp),
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor   = if (isDark) Color.White else cs.onPrimary,
                    disabledContainerColor = cs.onSurface.copy(alpha = 0.1f),
                    disabledContentColor   = cs.onSurface.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation  = 6.dp,
                    pressedElevation  = 2.dp,
                    disabledElevation = 0.dp
                )
            ) {
                AnimatedContent(
                    targetState = existingTask == null,
                    transitionSpec = {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                    },
                    label = "btn_label"
                ) { isNew ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isNew) "Save Task" else "Update Task",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
private fun SectionCard(
    isDark: Boolean,
    cs: ColorScheme,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = cs.primary.copy(alpha = 0.06f),
                spotColor    = cs.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(18.dp),
        color = cs.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = cs.outline.copy(alpha = if (isDark) 0.18f else 0.1f)
        )
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    label: String,
    tint: Color,
    background: Color,
    cs: ColorScheme,
    labelAlpha: Float
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionIcon(icon = icon, tint = tint, background = background)
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = cs.onSurface.copy(alpha = labelAlpha),
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun SectionIcon(
    icon: ImageVector,
    tint: Color,
    background: Color
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun DetailChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeContainerColor: Color,
    activeLabelColor: Color,
    inactiveContainerColor: Color,
    inactiveLabelColor: Color,
    onClick: () -> Unit,
    onClear: (() -> Unit)?
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) activeContainerColor else inactiveContainerColor,
        animationSpec = tween(200),
        label = "chip_bg"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isActive) activeLabelColor else inactiveLabelColor,
        animationSpec = tween(200),
        label = "chip_fg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = labelColor, modifier = Modifier.size(15.dp))
        Text(
            label,
            fontSize = 13.sp,
            color = labelColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        if (isActive && onClear != null) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(labelColor.copy(alpha = 0.18f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClear
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = labelColor, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun TimeBlock(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    color: Color,
    cs: ColorScheme,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = color.copy(alpha = 0.75f),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            time,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else cs.onSurface
        )
    }
}