package com.example.test

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.test.data.models.Task
import com.example.test.data.repository.ProfileRepository
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.NoteRepository
import com.example.test.data.repository.AppPreferencesRepository
import com.example.test.ui.screens.*
import com.example.test.ui.theme.TestTheme
import com.example.test.ui.viewmodels.*
import kotlinx.coroutines.launch

@Composable
fun MyApp(
    taskRepository: TaskRepository,
    noteRepository: NoteRepository
) {
    val context = LocalContext.current
    val prefsRepo = remember { AppPreferencesRepository.getInstance(context) }
    val profileRepository = remember { ProfileRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val isDarkThemeFlow = remember { prefsRepo.isDarkThemeFlow }
    val paletteIndexFlow = remember { prefsRepo.paletteIndexFlow }

    var isDarkTheme by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var paletteIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    val collectedDarkTheme by isDarkThemeFlow.collectAsState(initial = isDarkTheme)
    val collectedPaletteIndex by paletteIndexFlow.collectAsState(initial = paletteIndex)

    LaunchedEffect(collectedDarkTheme) {
        if (collectedDarkTheme != null) isDarkTheme = collectedDarkTheme
    }
    LaunchedEffect(collectedPaletteIndex) {
        if (collectedPaletteIndex != null) paletteIndex = collectedPaletteIndex
    }

    if (isDarkTheme == null || paletteIndex == null) {
        return
    }

    val currentDarkTheme = isDarkTheme!!
    val currentPaletteIndex = paletteIndex!!

    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val calendarViewModel: CalendarViewModel = viewModel(factory = ViewModelFactory(taskRepository))
    val noteViewModel: NoteViewModel = viewModel(factory = ViewModelFactory(noteRepository))

    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    TestTheme(
        darkTheme = currentDarkTheme,
        paletteIndex = currentPaletteIndex
    ) {
        AppLockGate(profileRepository = profileRepository) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {

                    composable("stats") {
                        StatsScreen(
                            taskViewModel = taskViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            taskViewModel = taskViewModel,
                            isDarkTheme = currentDarkTheme,
                            paletteIndex = currentPaletteIndex,
                            onToggleDarkMode = {
                                scope.launch { prefsRepo.setDarkTheme(!currentDarkTheme) }
                            },
                            onAddTask = { task ->
                                taskToEdit = task
                                navController.navigate("add_task")
                            },
                            onCalendarClick = { navController.navigate("calendar") },
                            onNotesClick = { navController.navigate("notes") },
                            onTasksClick = { navController.navigate("today_tasks") },
                            onStatsClick = { navController.navigate("stats") },
                            onPaletteChange = { index ->
                                scope.launch { prefsRepo.setPaletteIndex(index) }
                            },
                            onProfileClick = { navController.navigate("profile") }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("today_tasks") {
                        TodayTasksScreen(
                            taskViewModel = taskViewModel,
                            onAddTask = { task ->
                                taskToEdit = task
                                navController.navigate("add_task")
                            },
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
                        AddTaskScreen(
                            taskViewModel = taskViewModel,
                            onClose = { navController.popBackStack() },
                            initialDateMillis = if (dateMillis > 0) dateMillis else null,
                            existingTask = taskToEdit
                        )
                        // Clear taskToEdit when screen is disposed
                        DisposableEffect(Unit) {
                            onDispose { taskToEdit = null }
                        }
                    }

                    composable("calendar") {
                        CalendarScreen(
                            viewModel = calendarViewModel,
                            onNavigateHome = { navController.popBackStack("home", false) },
                            onAddTask = { task ->
                                taskToEdit = task
                                if (task == null) {
                                    val selectedDate = calendarViewModel.selectedDate.value
                                    val dateMillis = selectedDate.time
                                    navController.navigate("add_task?dateMillis=$dateMillis")
                                } else {
                                    navController.navigate("add_task")
                                }
                            }
                        )
                    }

                    composable("notes") {
                        NotesScreen(
                            noteViewModel = noteViewModel,
                            onNoteClick = { noteId ->
                                navController.navigate("note_editor/$noteId/NOTE")
                            },
                            onNewNote = { type ->
                                navController.navigate("note_editor/-1/$type")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("note_editor/{noteId}/{noteType}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: -1L
                        val noteType = backStackEntry.arguments?.getString("noteType") ?: "NOTE"
                        NoteEditorScreen(
                            noteId = noteId,
                            noteType = noteType,
                            noteViewModel = noteViewModel,
                            profileRepository = profileRepository,
                            onNavigateToAuth = { navController.navigate("profile") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
