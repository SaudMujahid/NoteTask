package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.test.data.AppDatabase
import com.example.test.data.repository.NoteRepository
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ── Database & Repositories (created once) ──
        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        val taskRepository = TaskRepository(database.taskDao())
        val noteRepository = NoteRepository(database.noteDao())


        setContent {
            MyApp(
                userRepository = userRepository,
                taskRepository = taskRepository,
                noteRepository = noteRepository
            )
        }
    }
}