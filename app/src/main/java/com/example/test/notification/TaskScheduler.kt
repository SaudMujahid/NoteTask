package com.example.test.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.test.data.models.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TaskScheduler {

    fun scheduleTaskNotification(context: Context, task: Task, taskDate: String) {
        val triggerAtMillis = parseTaskDateToMillis(taskDate, task.scheduleStartMinutes)
            ?: return

        // Skip if the time has already passed
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun parseTaskDateToMillis(dateString: String, startMinutes: Int?): Long? {
        return try {
            // Adjust this pattern if your date format is different (e.g., "dd/MM/yyyy")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return null

            val calendar = Calendar.getInstance().apply {
                time = date
                startMinutes?.let { min ->
                    set(Calendar.HOUR_OF_DAY, min / 60)
                    set(Calendar.MINUTE, min % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}