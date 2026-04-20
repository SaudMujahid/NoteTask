package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.test.data.models.Task
import com.example.test.ui.theme.*
import com.example.test.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.test.ui.components.TaskItem
@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    userId: Long?,
    firstName: String = "",
    isDarkTheme: Boolean = false, // only used for toggle icon/label now
    onToggleDarkMode: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onNotesClick: () -> Unit = {}
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(userId) {
        userId?.let { taskViewModel.setUser(it) }
    }
    var menuOpen by remember { mutableStateOf(false) }
    var tasksExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                HomeHeader(
                    firstName = firstName,
                    onMenuClick = { menuOpen = true }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }

            item {
                AnimatedVisibility(
                    visible = !tasksExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        QuickAccessRow(
                            onCalendarClick = onCalendarClick,
                            onNotesClick = onNotesClick
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { tasksExpanded = !tasksExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tasks",
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = colorScheme.onBackground
                    )
                    val rotation by animateFloatAsState(
                        targetValue = if (tasksExpanded) 180f else 0f,
                        label = "chevron"
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (tasksExpanded) "Collapse" else "Expand",
                            tint = colorScheme.primary,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = tasksExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val todayLabel = remember {
                        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
                    }
                    Column {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Today · $todayLabel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                TaskListCard(
                    tasks = tasks,
                    onCheckedChange = { task -> taskViewModel.toggleTask(task) }
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        // Bottom bar + FAB
        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(top = 10.dp)
                    .background(
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
            )
            LargeFloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-15).dp)
                    .size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "Add Task", modifier = Modifier.size(32.dp))
            }
        }

        // Scrim
        AnimatedVisibility(
            visible = menuOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { menuOpen = false }
            )
        }

        // Drawer
        AnimatedVisibility(
            visible = menuOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().zIndex(11f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.72f)
                        .align(Alignment.CenterEnd)
                        .background(
                            color = colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                        )
                        .clickable { /* consume */ }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { menuOpen = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Close, "Close menu", tint = colorScheme.onSurface)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = if (firstName.isBlank()) "Hello!" else "Hi, $firstName 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colorScheme.primary)
                        )

                        Spacer(Modifier.height(36.dp))

                        DrawerMenuItem(
                            label = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            iconTint = Color(0xFFFFD700),
                            textColor = colorScheme.onSurface,
                            onClick = {
                                onToggleDarkMode()
                                menuOpen = false
                            }
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))

                        DrawerMenuItem(
                            label = "Log Out",
                            icon = Icons.Default.Logout,
                            iconTint = colorScheme.error,
                            textColor = colorScheme.error,
                            onClick = {
                                menuOpen = false
                                onLogout()
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            "v1.0.0",
                            fontSize = 11.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
fun HomeHeader(
    firstName: String,
    onMenuClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else      -> "Good night"
        }
    }
    val dateString = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }
    val displayName = if (firstName.isBlank()) "there" else firstName

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                .background(colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, $displayName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = colorScheme.onSurface.copy(alpha = 0.45f),
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(Modifier.width(12.dp))
                HamburgerButton(onClick = onMenuClick)
            }
        }

        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.primary)
        )
    }
}

@Composable
fun HamburgerButton(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.width(22.dp).height(2.5.dp).clip(RoundedCornerShape(50)).background(color)
            )
            Box(
                modifier = Modifier
                    .width(16.dp).height(2.5.dp).clip(RoundedCornerShape(50))
                    .align(Alignment.Start).offset(x = 3.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier.width(22.dp).height(2.5.dp).clip(RoundedCornerShape(50)).background(color)
            )
        }
    }
}

@Composable
fun QuickAccessRow(
    onCalendarClick: () -> Unit,
    onNotesClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickAccessCard(
            title = "Notes",
            subtitle = "Tap to open",
            icon = Icons.Default.Add,
            backgroundColor = Color(0xFFFFD54F),
            modifier = Modifier.weight(1f),
            onClick = onNotesClick
        )
        QuickAccessCard(
            title = "Calendar",
            subtitle = "View schedule",
            icon = Icons.Default.CalendarMonth,
            backgroundColor = Color(0xFF81C784),
            modifier = Modifier.weight(1f),
            onClick = onCalendarClick
        )
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TaskListCard(
    tasks: List<Task>,
    onCheckedChange: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tasks yet. Tap + to add one!",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskItem(
                        title = task.title,
                        category = task.category,
                        checked = task.isChecked,
                        onCheckedChange = { onCheckedChange(task) }
                    )
                    if (index < tasks.lastIndex) {
                        HorizontalDivider(
                            color = colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}



