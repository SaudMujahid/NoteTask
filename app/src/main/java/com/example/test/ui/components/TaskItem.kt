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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.SubtaskIndent

//@Composable
//fun TaskItem() {
//    Text(text = "Task Item Component")
//}

@Composable
fun TaskItem(
    title: String,
    category: String,
    subtasks: List<String> = emptyList(),
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(uncheckedColor = Color.LightGray))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
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
                Checkbox(checked = false, onCheckedChange = {},
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(subtask, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}
