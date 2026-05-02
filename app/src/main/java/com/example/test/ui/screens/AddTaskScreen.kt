package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.components.CategoryChip
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

// Must stay in sync with CategoryChip's when-branches.
private val Categories = listOf("Personal", "Work", "University", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    userId: Long,
    taskViewModel: TaskViewModel,
    onClose: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var title    by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Categories.first()) }

    val todayMillis         = remember { Calendar.getInstance().timeInMillis }
    var selectedDateMillis  by remember { mutableStateOf(todayMillis) }
    var showDatePicker      by remember { mutableStateOf(false) }

    val dateFormatter    = remember { SimpleDateFormat("yyyy-MM-dd",      Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("EEEE, MMM d",     Locale.getDefault()) }

    val date        = remember(selectedDateMillis) { dateFormatter.format(Date(selectedDateMillis)) }
    val dateDisplay = remember(selectedDateMillis) { displayFormatter.format(Date(selectedDateMillis)) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "New Task",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = cs.onBackground
            )

            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(20.dp))

            // ── Category label ───────────────────────────────────────────
            Text(
                "Category",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = cs.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
            )

            // Chip selector — tapping a chip selects it
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Categories.forEach { cat ->
                    val selected = cat == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (selected) cs.primary else cs.surfaceVariant
                            )
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
                            color = if (selected) cs.onPrimary
                            else cs.onSurfaceVariant
                        )
                    }
                }
            }

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

            Spacer(Modifier.height(20.dp))

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

            Spacer(Modifier.weight(1f))

            // ── Save button ──────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        taskViewModel.addTask(userId, title, category, date)
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

            Spacer(Modifier.height(16.dp))
        }
    }
}