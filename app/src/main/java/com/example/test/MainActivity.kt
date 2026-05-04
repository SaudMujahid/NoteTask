package com.example.test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.test.data.AppDatabase
import com.example.test.data.repository.NoteRepository
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.UserRepository
import com.example.test.notification.NotificationObserver
import com.example.test.notification.TaskEventBus

class MainActivity : ComponentActivity() {
    private lateinit var notificationObserver: NotificationObserver

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied - maybe show a message to user
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        checkExactAlarmPermission()

        notificationObserver = NotificationObserver(applicationContext)
        TaskEventBus.addObserver(notificationObserver)

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