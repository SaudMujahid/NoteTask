package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.screens.*

import com.example.test.ui.theme.TestTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.data.AppDatabase
import com.example.test.data.repository.*
import com.example.test.ui.viewmodels.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        val taskRepository = TaskRepository(database.taskDao())
        val noteRepository = NoteRepository(database.noteDao())

        setContent {
            TestTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(userRepository))
                val taskViewModel: TaskViewModel = viewModel(factory = ViewModelFactory(taskRepository))
                val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(noteRepository))
                
                val currentUser by authViewModel.currentUser.collectAsState()
                LaunchedEffect(currentUser) {
                    currentUser?.let {
                        taskViewModel.setUser(it.id)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home", // 👈 start from signup
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // 🔐 AUTH SCREENS
                        composable("signup") {
                            SignUpScreen(
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

                        // 🏠 MAIN APP SCREENS
                        composable("home") {
                            HomeScreen(
                                taskViewModel = taskViewModel,
                                userId = currentUser?.id,
                                onAddClick = {
                                    if (currentUser != null) {
                                        navController.navigate("add_task")
                                    } else {
                                        navController.navigate("login")
                                    }
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
    }
}