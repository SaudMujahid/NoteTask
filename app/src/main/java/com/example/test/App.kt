package com.example.test

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.UserRepository
import com.example.test.data.repository.NoteRepository
import com.example.test.ui.screens.*
import com.example.test.ui.theme.TestTheme
import com.example.test.ui.viewmodels.*

@Composable
fun MyApp(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    noteRepository: NoteRepository
) {
    var isDarkTheme  by remember { mutableStateOf(false) }
    var paletteIndex by remember { mutableStateOf(0) }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(userRepository))
    val taskViewModel: TaskViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val calendarViewModel: CalendarViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(noteRepository))
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            taskViewModel.setUser(user.id)
            noteViewModel.setUser(user.id)
            calendarViewModel.setUser(user.id)
        }
    }

    TestTheme(
        darkTheme = isDarkTheme,
        paletteIndex = paletteIndex
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {

                composable("signup") {
                    SignUpScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkMode = { isDarkTheme = !isDarkTheme },
                        authViewModel = authViewModel,
                        onSignUpClick = {
                            navController.navigate("home") {
                                popUpTo("signup") { inclusive = true }
                            }
                        },
                        onLoginClick = { navController.navigate("login") }
                    )
                }

                composable("stats") {
                    StatsScreen(
                        taskViewModel = taskViewModel,
                        userId = currentUser?.id,
                        onBack = { navController.popBackStack() }
                    )
                }

                    composable("login") {
                    LoginScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkMode = { isDarkTheme = !isDarkTheme },
                        authViewModel = authViewModel,
                        onLoginClick = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onSignUpClick = { navController.navigate("signup") }
                    )
                }

                composable("home") {
                    HomeScreen(
                        taskViewModel = taskViewModel,
                        userId = currentUser?.id,
                        firstName = currentUser?.firstName ?: "",
                        isDarkTheme = isDarkTheme,
                        isLoggedIn = currentUser != null,
                        paletteIndex    = paletteIndex,
                        onToggleDarkMode = { isDarkTheme = !isDarkTheme },
                        onAuthAction = {
                            if (currentUser != null) {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            } else {
                                navController.navigate("login")
                            }
                        },
                        onAddClick = {
                            if (currentUser != null) navController.navigate("add_task")
                            else navController.navigate("login")
                        },
                        onCalendarClick = { navController.navigate("calendar") },
                        onNotesClick    = { navController.navigate("notes") },
                        onTasksClick    = { navController.navigate("today_tasks") },
                        onStatsClick = { navController.navigate("stats") }
                    ) { paletteIndex = it }
                }

                composable("today_tasks") {
                    TodayTasksScreen(
                        taskViewModel = taskViewModel,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(
                    "add_task?dateMillis={dateMillis}",
                    arguments = listOf(
                        navArgument("dateMillis") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: -1L
                    currentUser?.let { user ->
                        AddTaskScreen(
                            userId = user.id,
                            taskViewModel = taskViewModel,
                            onClose = { navController.popBackStack() },
                            initialDateMillis = if (dateMillis > 0) dateMillis else null
                        )
                    }
                }

                composable("calendar") {
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        onNavigateHome = { navController.popBackStack("home", false) },
                        onAddTaskClick = {
                            if (currentUser != null) {
                                val selectedDate = calendarViewModel.selectedDate.value
                                val dateMillis = selectedDate.time
                                navController.navigate("add_task?dateMillis=$dateMillis")
                            } else navController.navigate("login")
                        }
                    )
                }

                composable("notes") {
                    NotesScreen(
                        userId = currentUser?.id ?: 0L,
                        noteViewModel = noteViewModel,
                        onNoteClick = { noteId ->
                            navController.navigate("note_editor/$noteId/NOTE")
                        },
                        onNewNote = { type ->
                            navController.navigate("note_editor/-1/$type")
                        }
                    )
                }

                composable("note_editor/{noteId}/{noteType}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: -1L
                    val noteType = backStackEntry.arguments?.getString("noteType") ?: "NOTE"
                    NoteEditorScreen(
                        noteId = noteId,
                        noteType = noteType,
                        userId = currentUser?.id ?: 0L,
                        noteViewModel = noteViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
