package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.BackgroundGray
import com.example.test.ui.theme.PrimaryBlue

@Composable
fun CalendarScreen() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundGray)
        .padding(16.dp)
    ) {
        Text("Calendar", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))

        // Horizontal date strip
        val days = listOf("WED 25", "THU 26", "FRI 27", "SAT 28", "SUN 29", "MON 30")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days) { day ->
                val isSelected = day.contains("26")
                DayChip(day, isSelected)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Time slots
        val times = listOf("6:00","7:00","8:00","9:00","10:00","14:00","15:00","16:00","17:00","18:00","19:00","21:00")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            items(times) { time ->
                Text(time, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DayChip(day: String, isSelected: Boolean) {
    val parts = day.split(" ")
    Column(
        modifier = Modifier
            .background(
                if (isSelected) PrimaryBlue else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = parts[0],
            fontSize = 10.sp,
            color = if (isSelected) Color.White else Color.Gray
        )
        Text(
            text = parts[1],
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}
