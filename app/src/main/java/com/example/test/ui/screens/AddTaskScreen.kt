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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.test.ui.components.CategoryChip
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

// Must stay in sync with CategoryChip's when-branches.
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

    val todayMillis         = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis  by remember { mutableStateOf(initialDateMillis ?: todayMillis) }
    var showDatePicker      by remember { mutableStateOf(false) }
    var isScheduled         by remember { mutableStateOf(false) }
    var startMinutes        by remember { mutableStateOf(9 * 60) }
    var endMinutes          by remember { mutableStateOf(10 * 60) }

    val dateFormatter    = remember { SimpleDateFormat("yyyy-MM-dd",      Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("EEEE, MMM d",     Locale.getDefault()) }

    val date        = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }
    val dateDisplay = remember(selectedDateMillis) { displayFormatter.format(Date(selectedDateMillis)) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    fun formatMinutes(minutes: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return timeFormatter.format(calendar.time)
    }

    fun pickTime(initialMinutes: Int, onPicked: (Int) -> Unit) {
        val hour = initialMinutes / 60
        val minute = initialMinutes % 60
        TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute ->
                onPicked(pickedHour * 60 + pickedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    // ── Date Picker Dialog ───────────────────────────────────────────────
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
                containerColor            = cs.surface,
                titleContentColor         = cs.onSurface,
                headlineContentColor      = cs.onSurface,
                weekdayContentColor       = cs.onSurfaceVariant,
                subheadContentColor       = cs.onSurfaceVariant,
                yearContentColor          = cs.onSurfaceVariant,
                currentYearContentColor   = cs.primary,
                selectedYearContentColor  = cs.onPrimary,
                selectedYearContainerColor= cs.primary,
                dayContentColor           = cs.onSurface,
                selectedDayContentColor   = cs.onPrimary,
                selectedDayContainerColor = cs.primary,
                todayContentColor         = cs.primary,
                todayDateBorderColor      = cs.primary
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

            item {
                Spacer(Modifier.height(24.dp))
            }

            item {
                // ── Task name ────────────────────────────────────────────────
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = cs.primary,
                        unfocusedBorderColor = cs.outline
                    )
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
            }

            item {
                // ── Category label ───────────────────────────────────────────
                Text(
                    "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                // Chip selector — tapping a chip selects it
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Categories.forEach { cat ->
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
            }

            item {
                Spacer(Modifier.height(6.dp))

                // Live chip preview so user sees exactly how it will look in the list
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 2.dp, top = 4.dp)
                ) {
                    Text(
                        "Preview:",
                        fontSize = 11.sp,
                        color = cs.onSurface.copy(alpha = 0.35f)
                    )
                    CategoryChip(category = category)
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
            }

            item {
                // ── Date picker ──────────────────────────────────────────────
                Text(
                    "Date",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = cs.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, cs.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dateDisplay, color = cs.onSurface)
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select date",
                            tint = cs.primary
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

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
                        onCheckedChange = { checked -> isScheduled = checked }
                    )
                }
            }

            if (isScheduled) {
                item {
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { pickTime(startMinutes) { picked -> startMinutes = picked } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Start", fontSize = 11.sp, color = cs.onSurfaceVariant)
                                Text(formatMinutes(startMinutes), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        OutlinedButton(
                            onClick = { pickTime(endMinutes) { picked -> endMinutes = picked } },
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

            item {
                Spacer(Modifier.height(32.dp))
            }

            item {
                // ── Save button ──────────────────────────────────────────────
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val normalizedEnd = if (isScheduled && endMinutes <= startMinutes) {
                                (startMinutes + 60).coerceAtMost(23 * 60 + 59)
                            } else {
                                endMinutes
                            }
                            taskViewModel.addTask(
                                userId,
                                title,
                                category,
                                date,
                                isScheduled = isScheduled,
                                scheduleStartMinutes = if (isScheduled) startMinutes else null,
                                scheduleEndMinutes = if (isScheduled) normalizedEnd else null
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