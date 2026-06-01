package com.example.test.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test.MainActivity
import com.example.test.R
import com.example.test.data.AppDatabase
import com.example.test.data.models.Task
import com.example.test.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

                sendDigestNotification(context, overdueTasks, upcomingTasks)

            } catch (e: Exception) {
                Log.e(TAG, "Error in DailyDigestReceiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendDigestNotification(
        context: Context,
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

        val todayDisplay = getTodayDisplayDate()
        val summaryText = when {
            overdueTasks.isNotEmpty() && upcomingTasks.isNotEmpty() ->
                "${upcomingTasks.size} due today, ${overdueTasks.size} overdue"
            overdueTasks.isNotEmpty() ->
                "${overdueTasks.size} overdue"
            else ->
                "${upcomingTasks.size} due today"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(todayDisplay)
            .setSummaryText(summaryText)

        // Add upcoming tasks first (limit to a few lines)
        upcomingTasks.take(5).forEach { task ->
            inboxStyle.addLine("Today ${task.title}")
        }
        
        // Add overdue tasks, maintaining a reasonable total line count
        val remainingLines = 6 - upcomingTasks.size.coerceAtMost(5)
        if (remainingLines > 0) {
            overdueTasks.take(remainingLines).forEach { task ->
                inboxStyle.addLine("${formatTaskDate(task.date)} ${task.title}")
            }
        }

        if (upcomingTasks.size + overdueTasks.size > 6) {
            inboxStyle.addLine("... and more")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(todayDisplay)
            .setContentText(summaryText) // Fallback summary
            .setSubText(summaryText) // Appears next to app name on many devices
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "View", pendingIntent)
            .addAction(0, "Plan Now", pendingIntent)
            .build()

        notificationManager.notify(DIGEST_NOTIFICATION_ID, notification)
        Log.d(TAG, "Digest sent: $summaryText")
    }

    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun getTodayDisplayDate(): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    private fun formatTaskDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (_: Exception) {
            dateStr
        }
    }

    companion object {
        private const val TAG = "DailyDigestReceiver"
        private const val CHANNEL_ID = "task_reminder_channel"
        private const val DIGEST_NOTIFICATION_ID = Int.MAX_VALUE
    }
}
