package com.example.test.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test.R
import com.example.test.data.AppDatabase
import com.example.test.data.models.Task
import com.example.test.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyDigestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DailyDigestReceiver fired at noon")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                TaskScheduler.scheduleDailyDigest(context)

                val dao = AppDatabase.getDatabase(context).taskDao()
                val repository = TaskRepository(dao)

                val today = getTodayDateString()
                val allTasks = repository.getAllTasks().first()

                val overdueTasks = allTasks.filter { it.date < today && !it.isChecked }
                val upcomingTasks = allTasks.filter { it.date == today && !it.isChecked }

                if (overdueTasks.isEmpty() && upcomingTasks.isEmpty()) {
                    Log.d(TAG, "No overdue or upcoming tasks — skipping digest notification")
                    return@launch
                }

                // Build title here, pass lists down — no more String body
                val notifTitle = when {
                    overdueTasks.isNotEmpty() && upcomingTasks.isNotEmpty() ->
                        "You have ${overdueTasks.size} overdue & ${upcomingTasks.size} tasks due today"
                    overdueTasks.isNotEmpty() ->
                        "You have ${overdueTasks.size} overdue task(s)"
                    else ->
                        "You have ${upcomingTasks.size} task(s) due today"
                }

                sendDigestNotification(context, notifTitle, overdueTasks, upcomingTasks)

            } catch (e: Exception) {
                Log.e(TAG, "Error in DailyDigestReceiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendDigestNotification(
        context: Context,
        title: String,
        overdueTasks: List<Task>,
        upcomingTasks: List<Task>
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val allTasks = overdueTasks + upcomingTasks
        val today = getTodayDateString()

        // One notification per task with Complete + Snooze actions
        allTasks.forEachIndexed { index, task ->
            val notifId = DIGEST_BASE_NOTIF_ID + index
            val label = if (task.date < today) "⚠️ Overdue" else "📅 Due Today"
            val body = "$label: ${task.title}"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(task.title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setGroup(DIGEST_GROUP_KEY)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                    TaskNotificationReceiver.buildCompleteAction(
                        context, task.id, notifId, task.title
                    )
                )
                .addAction(
                    TaskNotificationReceiver.buildSnoozeAction(
                        context, task.id, notifId, task.title
                    )
                )
                .build()

            notificationManager.notify(notifId, notification)
        }

        // Grouped summary notification in the shade
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(title)
                    .also { style -> allTasks.forEach { style.addLine(it.title) } }
            )
            .setGroup(DIGEST_GROUP_KEY)
            .setGroupSummary(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(DIGEST_NOTIFICATION_ID, summaryNotification)
        Log.d(TAG, "Digest sent: ${overdueTasks.size} overdue, ${upcomingTasks.size} upcoming")
    }

    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    companion object {
        private const val TAG = "DailyDigestReceiver"
        private const val CHANNEL_ID = "task_reminder_channel"
        private const val DIGEST_NOTIFICATION_ID = Int.MAX_VALUE
        private const val DIGEST_BASE_NOTIF_ID = Int.MAX_VALUE - 500
        private const val DIGEST_GROUP_KEY = "com.example.test.TASK_DIGEST"
    }
}