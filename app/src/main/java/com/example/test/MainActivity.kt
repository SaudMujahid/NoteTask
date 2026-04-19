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
import com.example.test.ui.screens.AddTaskScreen
import com.example.test.ui.screens.CalendarScreen
import com.example.test.ui.screens.HomeScreen
import com.example.test.ui.screens.NotesScreen
import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { 
                            HomeScreen(
                                onAddClick = { navController.navigate("add_task") },
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
