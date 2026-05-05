package com.example.test.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import com.example.test.notification.TaskEvent
import com.example.test.notification.TaskEventBus
import com.example.test.ui.components.CategoryChip
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Categories = listOf("Personal", "Work", "University", "Other")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    userId: Long,
    taskViewModel: TaskViewModel,
    onClose: () -> Unit,
    initialDateMillis: Long? = null
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current

    var title    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Categories.first()) }

    val todayMillis        = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis ?: todayMillis) }
    var showDatePicker     by remember { mutableStateOf(false) }
    var isScheduled        by remember { mutableStateOf(false) }
    var startMinutes       by remember { mutableStateOf(9 * 60) }
    var endMinutes         by remember { mutableStateOf(10 * 60) }

    // ── NEW: optional notification time (null = no notification) ──
    var notificationMinutes by remember { mutableStateOf<Int?>(null) }

    var description by remember {  mutableStateOf("")}

    val dateFormatter    = remember { SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    val date        = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }
    val dateDisplay = remember(selectedDateMillis) { displayFormatter.format(Date(selectedDateMillis)) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    fun formatMinutes(minutes: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return timeFormatter.format(cal.time)
    }

    fun pickTime(initialMinutes: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute -> onPicked(pickedHour * 60 + pickedMinute) },
            initialMinutes / 60,
            initialMinutes % 60,
            false
        ).show()
    }

    // ── Date Picker Dialog (unchanged) ───────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = true
                override fun isSelectableYear(year: Int)           = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK", color = cs.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = cs.onSurfaceVariant)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor             = cs.surface,
                titleContentColor          = cs.onSurface,
                headlineContentColor       = cs.onSurface,
                weekdayContentColor        = cs.onSurfaceVariant,
                subheadContentColor        = cs.onSurfaceVariant,
                yearContentColor           = cs.onSurfaceVariant,
                currentYearContentColor    = cs.primary,
                selectedYearContentColor   = cs.onPrimary,
                selectedYearContainerColor = cs.primary,
                dayContentColor            = cs.onSurface,
                selectedDayContentColor    = cs.onPrimary,
                selectedDayContainerColor  = cs.primary,
                todayContentColor          = cs.primary,
                todayDateBorderColor       = cs.primary
            )
        ) {
            DatePicker(
                state = dpState,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    selectedDayContentColor   = cs.onPrimary,
                    selectedDayContainerColor = cs.primary,
                    todayContentColor         = cs.primary,
                    todayDateBorderColor      = cs.primary
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Close", tint = cs.onBackground)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = cs.onBackground
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            // ── Combined title + description box ────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = cs.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, cs.outline)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                        // ── Title — hint overlaid behind the field ──
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (title.isEmpty()) {
                                Text(
                                    text = "What would you like to do?",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = cs.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = cs.onSurface
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                cursorBrush = SolidColor(cs.primary)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider(
                            color = cs.outline.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Spacer(Modifier.height(8.dp))

                        // ── Description — expands only as user types ──
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (description.isEmpty()) {
                                Text(
                                    text = "description",
                                    fontSize = 13.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = cs.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 13.sp,
                                    color = cs.onSurface.copy(alpha = 0.75f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                cursorBrush = SolidColor(cs.primary)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Text(
                    "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                // ── Single scrollable row — no wrapping ──
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(Categories) { cat ->
                        val selected = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) cs.primary else cs.surfaceVariant)
                                .border(
                                    width = if (selected) 0.dp else 1.dp,
                                    color = cs.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) cs.onPrimary else cs.onSurfaceVariant
                            )
                        }
                    }
                }
                // Preview row is removed
            }


            item { Spacer(Modifier.height(20.dp)) }

            item {
                // ── Date + Time side by side ─────────────────────────────────
                Text(
                    "Date & Time",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ── Date box (existing style) ──
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dateDisplay,
                                fontSize = 13.sp,
                                color = cs.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = cs.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // ── Time box (NEW — same style as Date) ──
                    Surface(
                        onClick = {
                            val init = notificationMinutes ?: (Calendar.getInstance().run {
                                get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)
                            })
                            pickTime(init) { picked -> notificationMinutes = picked }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        // Tinted when a time is set — matches selected Date feel
                        color = if (notificationMinutes != null)
                            cs.primaryContainer
                        else
                            cs.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (notificationMinutes != null) 1.5.dp else 1.dp,
                            color = if (notificationMinutes != null) cs.primary else cs.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notificationMinutes?.let { formatMinutes(it) } ?: "Set Time",
                                fontSize = 13.sp,
                                color = if (notificationMinutes != null) cs.primary
                                else cs.onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            )
                            // Clock icon — or ✕ to clear if time is set
                            if (notificationMinutes != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear time",
                                    tint = cs.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { notificationMinutes = null }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Set time",
                                    tint = cs.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Schedule task",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onSurface.copy(alpha = 0.55f)
                        )
                        Text(
                            "Show this task as a time block on the calendar",
                            fontSize = 11.sp,
                            color = cs.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    Switch(
                        checked = isScheduled,
                        onCheckedChange = { isScheduled = it }
                    )
                }
            }

            if (isScheduled) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { pickTime(startMinutes) { startMinutes = it } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Start", fontSize = 11.sp, color = cs.onSurfaceVariant)
                                Text(formatMinutes(startMinutes), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        OutlinedButton(
                            onClick = { pickTime(endMinutes) { endMinutes = it } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("End", fontSize = 11.sp, color = cs.onSurfaceVariant)
                                Text(formatMinutes(endMinutes), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }

            item {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val normalizedEnd = if (isScheduled && endMinutes <= startMinutes) {
                                (startMinutes + 60).coerceAtMost(23 * 60 + 59)
                            } else endMinutes

                            taskViewModel.addTask(
                                userId    = userId,
                                title     = title,
                                description = description,
                                category  = category,
                                date      = date,
                                isScheduled           = isScheduled,
                                scheduleStartMinutes  = if (isScheduled) startMinutes else null,
                                scheduleEndMinutes    = if (isScheduled) normalizedEnd else null,
                                notificationMinutes   = notificationMinutes
                            )
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary),
                    enabled = title.isNotBlank()
                ) {
                    Text("Save", color = cs.onPrimary, fontSize = 16.sp)
                }
            }
        }
    }
}