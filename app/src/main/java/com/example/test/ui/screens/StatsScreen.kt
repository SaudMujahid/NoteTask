package com.example.test.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private val ChartPalette = listOf(
    Color(0xFF6C63FF), Color(0xFF43C59E), Color(0xFFFF7A65), Color(0xFFFFBF3F),
    Color(0xFF5BC0EB), Color(0xFFE040FB), Color(0xFFFF6E97), Color(0xFF00C9A7),
)
private val CompletedGreen = Color(0xFF4ADE80)
private val PendingAmber  = Color(0xFFFFA040)
private enum class StatsPeriod { WEEK, MONTH }

private data class BarSegmentData(
    val label: String,
    val subLabel: String? = null,
    val total: Int,
    val categoryBreakdown: List<Pair<String, Int>>,
    val isHighlighted: Boolean = false
)

@Composable
fun StatsScreen(
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val cs = MaterialTheme.colorScheme
    var period by remember { mutableStateOf(StatsPeriod.WEEK) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val (periodStart, periodEnd) = remember(period) {
        val cal = Calendar.getInstance()
        when (period) {
            StatsPeriod.WEEK -> {
                // Week starts Friday, ends Thursday
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val daysSinceFriday = (dow - Calendar.FRIDAY + 7) % 7
                cal.add(Calendar.DAY_OF_YEAR, -daysSinceFriday)
                val s = sdf.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 6)
                s to sdf.format(cal.time)
            }
            StatsPeriod.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val s = sdf.format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                s to sdf.format(cal.time)
            }
        }
    }

    val filteredTasks = remember(tasks, periodStart, periodEnd) { tasks.filter { it.date in periodStart..periodEnd } }
    val completed = filteredTasks.count { it.isChecked }
    val pending = filteredTasks.count { !it.isChecked }
    val total = filteredTasks.size
    val rate = if (total > 0) completed.toFloat() / total else 0f
    val categoryStats = remember(filteredTasks) {
        filteredTasks.groupBy { it.category.ifBlank { "Uncategorized" } }.map { (cat, list) -> cat to list.size }.sortedByDescending { it.second }
    }

    // Day-by-day stats for WEEK view (Friday to Thursday) — uses filteredTasks
    val weekDayStats = remember(filteredTasks, periodStart, periodEnd) {
        val cal = Calendar.getInstance().apply { time = sdf.parse(periodStart)!! }
        (0..6).map { offset ->
            val day = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val dateStr = sdf.format(day.time)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(day.time).take(2)
            val dayTasks = filteredTasks.filter { it.date == dateStr }
            val categoryBreakdown = dayTasks.groupBy { it.category.ifBlank { "Uncategorized" } }
                .map { (cat, list) -> cat to list.size }
                .sortedByDescending { it.second }
            val isToday = dateStr == sdf.format(Date())
            BarSegmentData(
                label = dayLabel,
                total = dayTasks.size,
                categoryBreakdown = categoryBreakdown,
                isHighlighted = isToday
            )
        }
    }

    // Week-by-week stats for MONTH view — uses filteredTasks
    val monthWeekStats = remember(filteredTasks, periodStart, periodEnd) {
        val endCal = Calendar.getInstance().apply { time = sdf.parse(periodEnd)!! }
        val maxDay = endCal.get(Calendar.DAY_OF_MONTH)
        val yearMonth = periodStart.dropLast(3)

        val weeks = mutableListOf<BarSegmentData>()
        var currentDay = 1
        var weekNum = 1

        while (currentDay <= maxDay) {
            val weekStart = currentDay
            val weekEnd = minOf(currentDay + 6, maxDay)

            val weekStartStr = "$yearMonth-${weekStart.toString().padStart(2, '0')}"
            val weekEndStr = "$yearMonth-${weekEnd.toString().padStart(2, '0')}"

            val weekTasks = filteredTasks.filter { it.date in weekStartStr..weekEndStr }
            val weekCompleted = weekTasks.count { it.isChecked }
            val categoryBreakdown = weekTasks.groupBy { it.category.ifBlank { "Uncategorized" } }
                .map { (cat, list) -> cat to list.size }
                .sortedByDescending { it.second }

            weeks.add(BarSegmentData(
                label = weekCompleted.toString(),
                subLabel = "W$weekNum",
                total = weekTasks.size,
                categoryBreakdown = categoryBreakdown
            ))
            currentDay = weekEnd + 1
            weekNum++
        }
        weeks
    }

    Column(modifier = Modifier.fillMaxSize().background(cs.background).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 16.dp, top = 48.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Progress", fontWeight = FontWeight.Black, fontSize = 30.sp)
        }
        Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(3.dp).clip(RoundedCornerShape(50)).background(cs.primary))
        Spacer(Modifier.height(22.dp))
        PeriodToggle(selected = period, onSelect = { period = it }, modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth())
        Spacer(Modifier.height(22.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Total", "$total", cs.primary, Modifier.weight(1f))
            SummaryCard("Done", "$completed", CompletedGreen, Modifier.weight(1f))
            SummaryCard("Pending", "$pending", PendingAmber, Modifier.weight(1f))
        }
        Spacer(Modifier.height(30.dp))
        SectionLabel("Completion Rate", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { AnimatedDonutChart(rate = rate, completed = completed, total = total) }
        Spacer(Modifier.height(36.dp))

        // Conditional section: day-by-day for week, week-by-week for month
        when (period) {
            StatsPeriod.WEEK -> {
                SectionLabel("This Week, Day by Day", Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                StackedBarsContainer(stats = weekDayStats, barWidth = 26.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            StatsPeriod.MONTH -> {
                SectionLabel("This Month, Week by Week", Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                StackedBarsContainer(stats = monthWeekStats, barWidth = 32.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        Spacer(Modifier.height(36.dp))
        if (categoryStats.isNotEmpty()) {
            SectionLabel("By Category", Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(14.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cs.surface)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    categoryStats.forEachIndexed { idx, (cat, count) ->
                        CategoryRow(category = cat, count = count, total = total, color = ChartPalette[idx % ChartPalette.size])
                        if (idx < categoryStats.lastIndex) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = cs.outline.copy(alpha = 0.15f), thickness = 1.dp)
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun PeriodToggle(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant).padding(4.dp)) {
        StatsPeriod.values().forEach { p ->
            val active = p == selected
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (active) cs.primary else Color.Transparent).clickable { onSelect(p) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(text = p.name.lowercase().capitalize(), fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) cs.onPrimary else cs.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.11f))) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 30.sp, color = color)
            Spacer(Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun AnimatedDonutChart(rate: Float, completed: Int, total: Int) {
    val cs = MaterialTheme.colorScheme
    val trackColor = cs.primary.copy(alpha = 0.12f)
    val animRate by animateFloatAsState(targetValue = rate, animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing))
    val ringColor = when { rate >= 0.75f -> CompletedGreen; rate >= 0.40f -> Color(0xFF60C8FF); rate > 0f -> PendingAmber; else -> trackColor }
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val stroke = 30.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2f, radius * 2f), style = Stroke(width = stroke, cap = StrokeCap.Round))
            if (animRate > 0.005f) drawArc(color = ringColor, startAngle = -90f, sweepAngle = 360f * animRate, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2f, radius * 2f), style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${(animRate * 100).toInt()}%", fontWeight = FontWeight.Black, fontSize = 40.sp, color = if (rate > 0f) ringColor else cs.onSurface.copy(alpha = 0.3f))
            Text(text = "$completed / $total tasks", fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.50f))
        }
    }
}

@Composable
private fun StackedBarsContainer(
    stats: List<BarSegmentData>,
    barWidth: Dp,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val maxTotal = stats.maxOfOrNull { it.total }.takeIf { it != null && it > 0 } ?: 1
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cs.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.forEach { data ->
                StackedBar(data = data, maxTotal = maxTotal, primaryColor = cs.primary, barWidth = barWidth)
            }
        }
    }
}

@Composable
private fun StackedBar(
    data: BarSegmentData,
    maxTotal: Int,
    primaryColor: Color,
    barWidth: Dp
) {
    val maxH = 110.dp
    val animTotal by animateFloatAsState(
        targetValue = (data.total.toFloat() / maxTotal).coerceIn(0f, 1f),
        animationSpec = tween(900)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(maxH + 44.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.width(barWidth).height(maxH)
        ) {
            // Background track (total height)
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(if (animTotal > 0f) animTotal else 0.04f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(primaryColor.copy(alpha = 0.08f))
            ) {
                // Stacked category segments inside the total height
                if (data.total > 0) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        data.categoryBreakdown.forEachIndexed { idx, (_, count) ->
                            val fraction = count.toFloat() / data.total
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .background(ChartPalette[idx % ChartPalette.size])
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = data.label,
            fontSize = if (data.isHighlighted) 13.sp else 12.sp,
            fontWeight = if (data.isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (data.isHighlighted) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        data.subLabel?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                text = it,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
        if (data.isHighlighted) {
            Spacer(Modifier.height(3.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(primaryColor))
        }
    }
}

@Composable
private fun CategoryRow(category: String, count: Int, total: Int, color: Color) {
    val animFrac by animateFloatAsState(targetValue = if (total > 0) count.toFloat() / total else 0f, animationSpec = tween(1000))
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Text(text = category, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Text(text = "$count task${if (count != 1) "s" else ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f))
        }
        Spacer(Modifier.height(7.dp))
        Box(modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.14f))) {
            Box(modifier = Modifier.fillMaxWidth(animFrac).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = modifier)
}
