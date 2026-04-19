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
    // ── Theme state (single source of truth) ──
    var isDarkTheme by remember { mutableStateOf(false) }

    val navController = rememberNavController()

    // ── ViewModels via Factory ──
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(userRepository))
    val taskViewModel: TaskViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(noteRepository))

    val currentUser by authViewModel.currentUser.collectAsState()

    // Auto-load user data on login
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            taskViewModel.setUser(user.id)
            noteViewModel.setUser(user.id)
        }
    }

    TestTheme(darkTheme = isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "signup",
                modifier = Modifier.padding(innerPadding)
            ) {

                // ── Auth screens ───────────────────────────────
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
                        onLoginClick = {
                            navController.navigate("login")
                        }
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
                        onSignUpClick = {
                            navController.navigate("signup")
                        }
                    )
                }

                // ── Main app screens ───────────────────────────
                composable("home") {
                    HomeScreen(
                        taskViewModel = taskViewModel,
                        userId = currentUser?.id,
                        onAddClick = {
                            if (currentUser != null) navController.navigate("add_task")
                            else navController.navigate("login")
                        },
                        onCalendarClick = { navController.navigate("calendar") },
                        onNotesClick = { navController.navigate("notes") }
                    )
                }

                composable("add_task") {
                    AddTaskScreen(
                        onClose = { navController.popBackStack() },
                        onSave = { navController.popBackStack() }
                    )
                }

                composable("calendar") { CalendarScreen() }

                composable("notes") { NotesScreen() }
            }
        }
    }
}
