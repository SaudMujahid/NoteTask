package com.example.test

import android.Manifest
import android.app.AlarmManager
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
        if (!isGranted) {
            // Optional: show a snackbar/dialog explaining why it matters
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        checkExactAlarmPermission()

        notificationObserver = NotificationObserver(applicationContext)
        TaskEventBus.addObserver(notificationObserver)

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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted, nothing to do
                }
                else -> {
                    // This triggers the system dialog asking the user
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // Below Android 13, POST_NOTIFICATIONS doesn't exist — notifications work automatically
    }

    // ── Fix 3: Handle exact alarm permission properly ──
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Send user to system settings to grant exact alarm permission
                // This is required on Android 12+ — without it alarms won't fire exactly
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}