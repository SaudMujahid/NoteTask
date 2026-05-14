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
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Categories = listOf("Personal", "Work", "University", "Other")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
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

    var notificationMinutes by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }

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
        ) {
            DatePicker(state = dpState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Close")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Text("New Task", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            }
            item { Spacer(Modifier.height(24.dp)) }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = cs.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, cs.outline)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (title.isEmpty()) {
                                Text("What would you like to do?", fontSize = 15.sp, color = cs.onSurface.copy(alpha = 0.35f))
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = cs.onSurface),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = cs.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (description.isEmpty()) {
                                Text("description", fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.3f))
                            }
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.75f)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Categories) { cat ->
                        val selected = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) cs.primary else cs.surfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(cat, fontSize = 13.sp, color = if (selected) cs.onPrimary else cs.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                Text("Date & Time", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, cs.outline)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(dateDisplay, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.DateRange, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        onClick = {
                            val init = notificationMinutes ?: (Calendar.getInstance().run { get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE) })
                            pickTime(init) { notificationMinutes = it }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (notificationMinutes != null) cs.primaryContainer else cs.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (notificationMinutes != null) cs.primary else cs.outline)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(notificationMinutes?.let { formatMinutes(it) } ?: "Set Time", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            if (notificationMinutes != null) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp).clickable { notificationMinutes = null })
                            } else {
                                Icon(Icons.Outlined.AccessTime, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Schedule task", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Show this task as a time block on the calendar", fontSize = 11.sp, color = cs.onSurface.copy(alpha = 0.38f))
                    }
                    Switch(checked = isScheduled, onCheckedChange = { isScheduled = it })
                }
            }

            if (isScheduled) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { pickTime(startMinutes) { startMinutes = it } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                            Column {
                                Text("Start", fontSize = 11.sp)
                                Text(formatMinutes(startMinutes), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        OutlinedButton(onClick = { pickTime(endMinutes) { endMinutes = it } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                            Column {
                                Text("End", fontSize = 11.sp)
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
                            val normalizedEnd = if (isScheduled && endMinutes <= startMinutes) (startMinutes + 60).coerceAtMost(23 * 60 + 59) else endMinutes
                            taskViewModel.addTask(
                                title = title,
                                description = description,
                                category = category,
                                date = date,
                                isScheduled = isScheduled,
                                scheduleStartMinutes = if (isScheduled) startMinutes else null,
                                scheduleEndMinutes = if (isScheduled) normalizedEnd else null,
                                notificationMinutes = notificationMinutes
                            )
                            onClose()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Save", fontSize = 16.sp)
                }
            }
        }
    }
}
