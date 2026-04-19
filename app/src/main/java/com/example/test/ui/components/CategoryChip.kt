package com.example.test.ui.components

import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CategoryChip(label: String) {
    SuggestionChip(
        onClick = { /* TODO */ },
        label = { Text(label) }
    )
}
