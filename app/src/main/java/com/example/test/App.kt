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
import com.example.test.data.repository.NoteRepository
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.UserRepository
import com.example.test.ui.screens.*
import com.example.test.ui.theme.TestTheme
import com.example.test.ui.viewmodels.*

@Composable
fun MyApp(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    noteRepository: NoteRepository
) {
    var isDarkTheme by remember { mutableStateOf(false) }

    val navController  = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(userRepository))
    val taskViewModel: TaskViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(noteRepository))
    val calendarViewModel: CalendarViewModel = viewModel(factory = ViewModelFactory(taskRepository))

    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            taskViewModel.setUser(user.id)
            noteViewModel.setUser(user.id)
            calendarViewModel.setUser(user.id)
        }
    }

    TestTheme(darkTheme = isDarkTheme) {
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
                        firstName = currentUser?.firstName ?: "",   // ← real name
                        isDarkTheme = isDarkTheme,
                        onToggleDarkMode = { isDarkTheme = !isDarkTheme },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onAddClick = {
                            if (currentUser != null) navController.navigate("add_task")
                            else navController.navigate("login")
                        },
                        onCalendarClick = { navController.navigate("calendar") },
                        onNotesClick    = { navController.navigate("notes") },
                        onTasksClick = { navController.navigate("today_tasks") }
                    )
                }

                composable("today_tasks") {
                    TodayTasksScreen(
                        taskViewModel = taskViewModel,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable("add_task") {
                    currentUser?.let { user ->
                        AddTaskScreen(
                            userId = user.id,
                            taskViewModel = taskViewModel,
                            onClose = { navController.popBackStack() }
                        )
                    }
                }

                composable("calendar") {
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateHome = { navController.popBackStack("home", false) }
                    )
                }

                composable("notes") { NotesScreen() }
            }
        }
    }
}