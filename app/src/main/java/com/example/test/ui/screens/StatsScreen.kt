package com.example.test.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// Palette — a rich, distinct set of colours for category bars / dots
// ─────────────────────────────────────────────────────────────────────────────
private val ChartPalette = listOf(
    Color(0xFF6C63FF),
    Color(0xFF43C59E),
    Color(0xFFFF7A65),
    Color(0xFFFFBF3F),
    Color(0xFF5BC0EB),
    Color(0xFFE040FB),
    Color(0xFFFF6E97),
    Color(0xFF00C9A7),
)

private val CompletedGreen = Color(0xFF4ADE80)
private val PendingAmber  = Color(0xFFFFA040)

private enum class StatsPeriod { WEEK, MONTH }

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatsScreen(
    taskViewModel: TaskViewModel,
    userId: Long?,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val cs = MaterialTheme.colorScheme
    var period by remember { mutableStateOf(StatsPeriod.WEEK) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // ── Date ranges ──────────────────────────────────────────────────────────
    val (periodStart, periodEnd) = remember(period) {
        val cal = Calendar.getInstance()
        when (period) {
            StatsPeriod.WEEK -> {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                cal.add(Calendar.DAY_OF_YEAR, -(dow - Calendar.MONDAY + 7) % 7)
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

    val filteredTasks = remember(tasks, periodStart, periodEnd) {
        tasks.filter { it.date in periodStart..periodEnd }
    }

    val completed   = filteredTasks.count { it.isChecked }
    val pending     = filteredTasks.count { !it.isChecked }
    val total       = filteredTasks.size
    val rate        = if (total > 0) completed.toFloat() / total else 0f

    val categoryStats = remember(filteredTasks) {
        filteredTasks.groupBy { it.category.ifBlank { "Uncategorized" } }
            .map { (cat, list) -> cat to list.size }
            .sortedByDescending { it.second }
    }

    // Week bars always reflect the current calendar week regardless of toggle
    val weekDayStats = remember(tasks) {
        val cal = Calendar.getInstance()
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        cal.add(Calendar.DAY_OF_YEAR, -(dow - Calendar.MONDAY + 7) % 7)
        (0..6).map { offset ->
            val day = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val dateStr  = sdf.format(day.time)
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(day.time).take(2)
            val dayTasks = tasks.filter { it.date == dateStr }
            Triple(dayLabel, dayTasks.count { it.isChecked }, dayTasks.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 16.dp, top = 48.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = cs.onBackground)
            }
            Text(
                "Progress",
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                color = cs.onBackground
            )
        }

        // Accent bar below header
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(cs.primary)
        )

        Spacer(Modifier.height(22.dp))

        // ── Period toggle ────────────────────────────────────────────────────
        PeriodToggle(
            selected = period,
            onSelect = { period = it },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        )

        Spacer(Modifier.height(22.dp))

        // ── Summary cards ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("Total",   "$total",     cs.primary,    Modifier.weight(1f))
            SummaryCard("Done",    "$completed", CompletedGreen, Modifier.weight(1f))
            SummaryCard("Pending", "$pending",   PendingAmber,  Modifier.weight(1f))
        }

        Spacer(Modifier.height(30.dp))

        // ── Donut chart ───────────────────────────────────────────────────────
        SectionLabel("Completion Rate", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedDonutChart(rate = rate, completed = completed, total = total)
        }

        Spacer(Modifier.height(36.dp))

        // ── Week day bars ─────────────────────────────────────────────────────
        SectionLabel("This Week, Day by Day", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(14.dp))
        WeekDayBars(stats = weekDayStats, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(36.dp))

        // ── Category breakdown ────────────────────────────────────────────────
        if (categoryStats.isNotEmpty()) {
            SectionLabel("By Category", Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    categoryStats.forEachIndexed { idx, (cat, count) ->
                        CategoryRow(
                            category = cat,
                            count = count,
                            total = total,
                            color = ChartPalette[idx % ChartPalette.size]
                        )
                        if (idx < categoryStats.lastIndex) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(
                                color = cs.outline.copy(alpha = 0.15f),
                                thickness = 1.dp
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Period toggle  (Week / Month)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PeriodToggle(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cs.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatsPeriod.values().forEach { p ->
            val active = p == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) cs.primary else Color.Transparent)
                    .clickable { onSelect(p) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = p.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) cs.onPrimary else cs.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary card  (Total / Done / Pending)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SummaryCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.11f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                color = color
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = cs.onSurface.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated donut chart
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnimatedDonutChart(
    rate: Float,
    completed: Int,
    total: Int
) {
    val cs = MaterialTheme.colorScheme
    val trackColor = cs.primary.copy(alpha = 0.12f)

    val animRate by animateFloatAsState(
        targetValue = rate,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "donut"
    )

    val pct = (animRate * 100).toInt()
    val ringColor = when {
        rate >= 0.75f -> CompletedGreen
        rate >= 0.40f -> Color(0xFF60C8FF)
        rate > 0f     -> PendingAmber
        else          -> trackColor
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val stroke = 30.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val tl     = Offset(center.x - radius, center.y - radius)
            val arcSz  = Size(radius * 2f, radius * 2f)

            // Track ring
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = tl,
                size       = arcSz,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Progress ring
            if (animRate > 0.005f) {
                drawArc(
                    color      = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animRate,
                    useCenter  = false,
                    topLeft    = tl,
                    size       = arcSz,
                    style      = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "$pct%",
                fontWeight = FontWeight.Black,
                fontSize   = 40.sp,
                color      = if (rate > 0f) ringColor
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text     = "$completed / $total tasks",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Week day bar chart
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WeekDayBars(
    stats: List<Triple<String, Int, Int>>,   // label, done, total
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val maxTotal = stats.maxOfOrNull { it.third }.takeIf { it != null && it > 0 } ?: 1
    val todayLabel = remember {
        SimpleDateFormat("EEE", Locale.getDefault()).format(Date()).take(2)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.forEach { (label, done, total) ->
                WeekDayBar(
                    label     = label,
                    completed = done,
                    total     = total,
                    maxTotal  = maxTotal,
                    isToday   = label == todayLabel,
                    primaryColor = cs.primary
                )
            }
        }
    }
}

@Composable
private fun WeekDayBar(
    label: String,
    completed: Int,
    total: Int,
    maxTotal: Int,
    isToday: Boolean,
    primaryColor: Color
) {
    val maxH    = 110.dp
    val barW    = 26.dp
    val bgColor = primaryColor.copy(alpha = 0.15f)

    val totalFrac = (total.toFloat() / maxTotal).coerceIn(0f, 1f)
    val doneFrac  = if (total > 0) (completed.toFloat() / maxTotal).coerceIn(0f, 1f) else 0f

    val animTotal by animateFloatAsState(
        targetValue = totalFrac,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "total"
    )
    val animDone by animateFloatAsState(
        targetValue = doneFrac,
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "done"
    )

    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(maxH + 36.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.width(barW).height(maxH)
        ) {
            // Background (total tasks)
            Box(
                modifier = Modifier
                    .width(barW)
                    .fillMaxHeight(if (animTotal > 0f) animTotal else 0.04f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(bgColor)
                    .align(Alignment.BottomCenter)
            )
            // Completed overlay
            if (animDone > 0f) {
                Box(
                    modifier = Modifier
                        .width(barW)
                        .fillMaxHeight(animDone)
                        .clip(RoundedCornerShape(7.dp))
                        .background(CompletedGreen)
                        .align(Alignment.BottomCenter)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color      = if (isToday) primaryColor else labelColor
        )

        // Today dot
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isToday) primaryColor else Color.Transparent)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category row (label + animated progress bar)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CategoryRow(
    category: String,
    count: Int,
    total: Int,
    color: Color
) {
    val cs    = MaterialTheme.colorScheme
    val frac  = if (total > 0) count.toFloat() / total else 0f

    val animFrac by animateFloatAsState(
        targetValue  = frac,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label        = "catFrac"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text       = category,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp,
                    color      = cs.onSurface
                )
            }
            Text(
                text  = "$count task${if (count != 1) "s" else ""}",
                fontSize = 13.sp,
                color = cs.onSurface.copy(alpha = 0.50f)
            )
        }
        Spacer(Modifier.height(7.dp))

        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.14f))
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFrac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tiny helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontWeight = FontWeight.Bold,
        fontSize   = 17.sp,
        color      = MaterialTheme.colorScheme.onBackground,
        modifier   = modifier
    )
}
