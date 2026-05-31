package com.example.test.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.test.data.AppDatabase
import com.example.test.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1L)
        val notifId = intent.getIntExtra("notif_id", -1)

        if (taskId == -1L) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            ACTION_COMPLETE -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = AppDatabase.getDatabase(context).taskDao()
                        dao.updateTaskChecked(taskId, true) // call DAO directly — no ViewModel outside UI
                        Log.d(TAG, "Task $taskId marked complete from notification")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error completing task: ${e.message}")
                    } finally {
                        notificationManager.cancel(notifId)
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE -> {
                // Dismiss current notification and reschedule for 30 minutes later
                notificationManager.cancel(notifId)

                val taskTitle = intent.getStringExtra("task_title") ?: "Task Reminder"
                val snoozeMillis = System.currentTimeMillis() + SNOOZE_DURATION_MS

                TaskScheduler.scheduleSnoozeNotification(context, taskId, taskTitle, snoozeMillis, notifId)
                Log.d(TAG, "Task $taskId snoozed for 30 minutes")
            }
        }
    }

    companion object {
        const val TAG = "NotificationActionReceiver"
        const val ACTION_COMPLETE = "com.example.test.ACTION_COMPLETE"
        const val ACTION_SNOOZE = "com.example.test.ACTION_SNOOZE"
        const val SNOOZE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }
}