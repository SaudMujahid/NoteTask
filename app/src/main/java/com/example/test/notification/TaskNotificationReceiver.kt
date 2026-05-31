package com.example.test.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

        if (taskId == -1L) return

        val notifId = taskId.toInt()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val notifTitle = "⏰ Task Starting Now"
        val notifBody = "\"$taskTitle\" is starting now. Time to get it done!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notifTitle)
            .setContentText(notifBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen
            .addAction(buildCompleteAction(context, taskId, notifId, taskTitle))
            .addAction(buildSnoozeAction(context, taskId, notifId, taskTitle))
            .build()

        notificationManager.notify(notifId, notification)
    }

    companion object {
        const val CHANNEL_ID = "task_reminder_channel"

        fun buildCompleteAction(
            context: Context,
            taskId: Long,
            notifId: Int,
            taskTitle: String
        ): NotificationCompat.Action {
            val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_COMPLETE
                putExtra("task_id", taskId)
                putExtra("notif_id", notifId)
                putExtra("task_title", taskTitle)
            }
            val completePendingIntent = PendingIntent.getBroadcast(
                context,
                // Unique request code per task to avoid PendingIntent collisions
                (taskId * 10 + 1).toInt(),
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, // replace with a checkmark icon
                "✅ Complete",
                completePendingIntent
            ).build()
        }

        fun buildSnoozeAction(
            context: Context,
            taskId: Long,
            notifId: Int,
            taskTitle: String
        ): NotificationCompat.Action {
            val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE
                putExtra("task_id", taskId)
                putExtra("notif_id", notifId)
                putExtra("task_title", taskTitle)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (taskId * 10 + 2).toInt(), // distinct from complete's request code
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground, // replace with a snooze/clock icon
                "🔕 Snooze 30m",
                snoozePendingIntent
            ).build()
        }
    }
}