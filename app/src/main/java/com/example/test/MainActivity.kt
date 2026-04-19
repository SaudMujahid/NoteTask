package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.screens.*

import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TestTheme {
                val navController = rememberNavController()
                var isLoggedIn = false

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home", // 👈 start from signup
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // 🔐 AUTH SCREENS
                        composable("signup") {
                            SignUpScreen(
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
                                onAddClick = {
                                    if (isLoggedIn) {
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