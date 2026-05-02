package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryChip(category: String) {
    val cs = MaterialTheme.colorScheme

    val (bg, textColor) = when (category.trim().lowercase()) {
        "personal"   -> cs.primaryContainer   to cs.onPrimaryContainer
        "work"       -> cs.secondaryContainer to cs.onSecondaryContainer
        "university" -> cs.tertiaryContainer  to cs.onTertiaryContainer
        else         -> cs.surfaceVariant     to cs.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}