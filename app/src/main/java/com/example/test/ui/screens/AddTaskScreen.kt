package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    userId: Long,
    taskViewModel: TaskViewModel,
    onClose: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }

    // Date state
    val todayMillis = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis by remember { mutableStateOf(todayMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }

    val date = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }
    val dateDisplay = remember(selectedDateMillis) { displayFormatter.format(Date(selectedDateMillis)) }

    val categories = listOf("Work", "Health", "Mental Health", "Other")

    // ── Date Picker Dialog ───────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                // Optional: allow any date. Remove this block if you want to restrict range.
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = colorScheme.surface,
                titleContentColor = colorScheme.onSurface,
                headlineContentColor = colorScheme.onSurface,
                weekdayContentColor = colorScheme.onSurfaceVariant,
                subheadContentColor = colorScheme.onSurfaceVariant,
                yearContentColor = colorScheme.onSurfaceVariant,
                currentYearContentColor = colorScheme.primary,
                selectedYearContentColor = colorScheme.onPrimary,
                selectedYearContainerColor = colorScheme.primary,
                dayContentColor = colorScheme.onSurface,
                selectedDayContentColor = colorScheme.onPrimary,
                selectedDayContainerColor = colorScheme.primary,
                todayContentColor = colorScheme.primary,
                todayDateBorderColor = colorScheme.primary
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    selectedDayContentColor = colorScheme.onPrimary,
                    selectedDayContainerColor = colorScheme.primary,
                    todayContentColor = colorScheme.primary,
                    todayDateBorderColor = colorScheme.primary
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "New Task",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                category = selection
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Clickable date field ─────────────────────────────────────
            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateDisplay,
                        color = colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        taskViewModel.addTask(userId, title, category, date)
                        onClose()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                enabled = title.isNotBlank()
            ) {
                Text("Save", color = colorScheme.onPrimary, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}