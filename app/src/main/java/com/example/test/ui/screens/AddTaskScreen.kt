package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.BackgroundGray
import com.example.test.ui.theme.CardWhite
import com.example.test.ui.theme.PrimaryBlue

//@Composable
//fun AddTaskScreen() {
//    Text(text = "Add Task Screen")
//}

@Composable
fun AddTaskScreen(onClose: () -> Unit, onSave: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        // Close button top right
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp)) {

            // Date pickers row - 3 dashed-border boxes
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Day", Modifier.weight(1f))
                DateField("Month", Modifier.weight(1.5f))
                DateField("Year", Modifier.weight(2f))
            }

            Spacer(Modifier.height(16.dp))

            // Tasks card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    // existing subtasks list
                    SubtaskRow("Get a notebook")
                    SubtaskRow("Follow the youtube tutorial")
                    Spacer(Modifier.weight(1f))
                    // Add Task inline input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = false, onCheckedChange = {},
                            colors = CheckboxDefaults.colors(uncheckedColor = Color.LightGray))
                        Text("Add Task", color = Color.LightGray)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DateField(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Color.Gray)
        }
    }
}

@Composable
fun SubtaskRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = true,
            onCheckedChange = {},
            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
