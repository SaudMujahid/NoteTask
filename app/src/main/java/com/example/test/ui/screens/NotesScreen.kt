package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.BackgroundGray
import com.example.test.ui.theme.CardWhite

@Composable
fun NotesScreen() {
    var noteText by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundGray)
        .padding(16.dp)
    ) {
        Text(
            "Notes", 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            BasicTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                decorationBox = { inner ->
                    if (noteText.isEmpty()) {
                        Text("Text here...", color = Color.LightGray)
                    }
                    inner()
                }
            )
        }
    }
}
