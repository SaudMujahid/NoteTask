package com.example.test.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.SubtaskIndent

@Composable
fun TaskItem(
    title: String,
    category: String,
    subtasks: List<String> = emptyList(),
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    uncheckedColor = colorScheme.outline,
                    checkedColor = colorScheme.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (checked) colorScheme.onSurface.copy(alpha = 0.4f)
                    else colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                CategoryChip(category)
            }
        }

        // Subtasks (indented)
        subtasks.forEach { subtask ->
            Row(
                modifier = Modifier.padding(start = SubtaskIndent, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {},
                    modifier = Modifier.size(20.dp),
                    colors = CheckboxDefaults.colors(uncheckedColor = colorScheme.outline)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subtask,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}