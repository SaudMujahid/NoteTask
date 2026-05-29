package com.example.test.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.test.R

class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1L)
        val taskTitle = intent.getStringExtra("task_title") ?: "Task Reminder"
        val taskType = intent.getStringExtra("task_type") ?: "upcoming" // "overdue" or "upcoming"

        if (taskId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Differentiate title and body based on task type
        val notifTitle = if (taskType == "overdue") "⚠️ Overdue Task" else "⏰ Upcoming Task"
        val notifBody = if (taskType == "overdue")
            "\"$taskTitle\" is overdue. Don't forget to complete it!"
        else
            "\"$taskTitle\" is coming up. Get ready!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notifTitle)
            .setContentText(notifBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifBody)) // expand for long titles
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "task_reminder_channel"
    }
}