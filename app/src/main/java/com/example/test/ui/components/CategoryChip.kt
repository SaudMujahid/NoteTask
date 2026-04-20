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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.theme.ChipHealthBg
import com.example.test.ui.theme.ChipHealthText
import com.example.test.ui.theme.ChipMentalBg
import com.example.test.ui.theme.ChipMentalText
import com.example.test.ui.theme.ChipWorkBg
import com.example.test.ui.theme.ChipWorkText

@Composable
fun CategoryChip(category: String) {
    val (bg, textColor) = when (category.uppercase()) {
        "HEALTH"        -> ChipHealthBg to ChipHealthText
        "WORK"          -> ChipWorkBg to ChipWorkText
        "MENTAL HEALTH" -> ChipMentalBg to ChipMentalText
        else            -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
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