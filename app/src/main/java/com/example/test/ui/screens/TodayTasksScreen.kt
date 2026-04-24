package com.example.test.ui.screens

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.models.Task
import com.example.test.ui.components.CategoryChip
import com.example.test.ui.components.TaskItem
import com.example.test.ui.viewmodels.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Audio
private fun playCheckSound(context: android.content.Context) {
    try {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build()
        // EFFECT_TICK is a tiny built-in system sound available on all devices
        val soundUri = android.media.RingtoneManager.getDefaultUri(
            android.media.RingtoneManager.TYPE_NOTIFICATION
        )
        // Use a lightweight MediaPlayer one-shot instead so there's no asset dependency
        val mp = android.media.MediaPlayer()
        mp.setAudioAttributes(attrs)
        mp.setDataSource(context, soundUri)
        mp.setVolume(0.4f, 0.4f)
        mp.setOnPreparedListener { it.start() }
        mp.setOnCompletionListener { it.release() }
        mp.prepareAsync()
        sp.release()
    } catch (_: Exception) { /* silently skip if sound fails */ }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksScreen(
    taskViewModel: TaskViewModel,
    onClose: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayLabel = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }
    val displayFormatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    val todayTasks = remember(tasks) { tasks.filter { it.date == today } }
    val overdueTasks = remember(tasks) {
        // Only show tasks that are NOT yet completed in the overdue list
        tasks.filter { it.date < today && it.date.isNotBlank() && !it.isChecked }
            .sortedBy { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tasks",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onBackground
                        )
                        Text(
                            "Today · $todayLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                            letterSpacing = 0.3.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── TODAY ─────────────────────────────────────────────────────
            if (todayTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks for today.",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(todayTasks.size) { index ->
                    val task = todayTasks[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        TaskItem(
                            title = task.title,
                            category = task.category,
                            checked = task.isChecked,
                            onCheckedChange = { taskViewModel.toggleTask(task) }
                        )
                    }
                }
            }

            // ── OVERDUE ───────────────────────────────────────────────────
            if (overdueTasks.isNotEmpty()) {
                item { Spacer(Modifier.height(24.dp)) }

                item {
                    Text(
                        text = "Overdue",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        // Red header — no change here
                        color = colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            overdueTasks.forEachIndexed { index, task ->
                                val parsedDate = remember(task.date) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            .parse(task.date)
                                    } catch (_: Exception) { null }
                                }
                                val dateText = parsedDate?.let { displayFormatter.format(it) } ?: task.date

                                OverdueTaskRow(
                                    task = task,
                                    dateText = dateText,
                                    onToggle = {
                                        playCheckSound(context)
                                        taskViewModel.toggleTask(task)
                                    }
                                )
                                if (index < overdueTasks.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdueTaskRow(
    task: Task,
    dateText: String,
    onToggle: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Animation state — slides right and fades out when checked
    val offsetX = remember { Animatable(0f) }
    val alpha   = remember { Animatable(1f) }
    var pendingToggle by remember { mutableStateOf(false) }

    // When pendingToggle is set, run the swipe-off animation then fire onToggle
    LaunchedEffect(pendingToggle) {
        if (!pendingToggle) return@LaunchedEffect
        // Animate simultaneously: slide right 300dp, fade to 0
        kotlinx.coroutines.coroutineScope {
            launch { offsetX.animateTo(600f, animationSpec = tween(durationMillis = 320)) }
            launch { alpha.animateTo(0f,   animationSpec = tween(durationMillis = 280)) }
        }
        onToggle()          // Commit the state change after animation
        // Reset so the row is gone from the list (ViewModel removes it)
        offsetX.snapTo(0f)
        alpha.snapTo(1f)
        pendingToggle = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = offsetX.value }
            .alpha(alpha.value),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = {
                    // Trigger animated removal instead of toggling directly
                    if (!pendingToggle) pendingToggle = true
                },
                colors = CheckboxDefaults.colors(
                    // Checkbox accent uses error colour to match the overdue theme,
                    // but the row background stays neutral
                    checkedColor   = colorScheme.error,
                    uncheckedColor = colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorScheme.onSurface   // normal text, not struck-through red
                )
                Spacer(Modifier.height(4.dp))
                CategoryChip(category = task.category)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.error.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.error
            )
        }
    }
}