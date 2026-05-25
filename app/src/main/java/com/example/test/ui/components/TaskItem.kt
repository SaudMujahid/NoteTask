package com.example.test.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.theme.SubtaskIndent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun minutesToLabel(minutes: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, minutes / 60)
        set(Calendar.MINUTE, minutes % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
}

// ── Component ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    subtasks: List<String> = emptyList(),
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onEditTask: (Task) -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme

    // Internal sheet state
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Derive a single time label shown both in the row and the sheet
    val timeLabel: String? = when {
        task.isScheduled && task.scheduleStartMinutes != null && task.scheduleEndMinutes != null ->
            "${minutesToLabel(task.scheduleStartMinutes)} – ${minutesToLabel(task.scheduleEndMinutes)}"
        task.notificationMinutes != null ->
            minutesToLabel(task.notificationMinutes)
        else -> null
    }

    // ── Bottom sheet ────────────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = cs.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp)
            ) {
                // Title row — clickable → edit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSheet = false; onEditTask(task) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(task.category)
                }

                // Time info
                if (timeLabel != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = cs.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.primary
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = cs.outlineVariant)
                Spacer(Modifier.height(20.dp))

                // Description — clickable → edit
                val hasDescription = task.description.isNotBlank()
                Text(
                    text = if (hasDescription) task.description else "No description",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasDescription) cs.onSurface.copy(alpha = 0.85f)
                    else cs.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSheet = false; onEditTask(task) }
                        .padding(vertical = 6.dp)
                )

                // Delete action
                if (onDelete != null) {
                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { showSheet = false; onDelete() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Task", fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // ── Task row ──────────────────────────────────────────────────────────────
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                uncheckedColor = cs.outline,
                checkedColor = cs.primary
            ),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
        )
        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { showSheet = true }
                .padding(end = 16.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (checked) cs.onSurface.copy(alpha = 0.4f) else cs.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    if (timeLabel != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    CategoryChip(task.category)
                }
            }
        }
    }

    // Subtasks
    subtasks.forEach { subtask ->
        Row(
            modifier = Modifier.padding(start = SubtaskIndent, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.size(20.dp),
                colors = CheckboxDefaults.colors(uncheckedColor = cs.outline)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = subtask,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }
    }
}
