package com.example.test.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.test.data.models.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TaskScheduler {

    private const val TAG = "TaskScheduler"

    fun scheduleTaskNotification(context: Context, task: Task, taskDate: String) {
        if (task.scheduleStartMinutes == null) {
            Log.w(TAG, "Skipping task '${task.title}' — no start time set")
            return
        }

        val triggerAtMillis = parseTaskDateToMillis(taskDate, task.scheduleStartMinutes)
        if (triggerAtMillis == null) {
            Log.e(TAG, "Failed to parse date '$taskDate' for task '${task.title}'")
            return
        }

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Skipping task '${task.title}' — scheduled time already passed")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarm — permission not granted")
                return
            }
        }

        // ── Fix: unwrap safely before passing to setExactAndAllowWhileIdle ──
        val pendingIntent = buildPendingIntent(context, task) ?: run {
            Log.e(TAG, "Failed to build PendingIntent for task '${task.title}'")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d(TAG, "Scheduled notification for '${task.title}' at $triggerAtMillis")
    }

    fun cancelTaskNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, task, noCreate = true)
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled notification for task id=${task.id}")
        }
    }

    private fun buildPendingIntent(
        context: Context,
        task: Task,
        noCreate: Boolean = false
    ): PendingIntent? {
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
        }
        val flags = if (noCreate) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, task.id.toInt(), intent, flags)
    }

    // ── Fix 4: Robust date parsing with clear error logging ──
    private fun parseTaskDateToMillis(dateString: String, startMinutes: Int?): Long? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false // strict parsing — catches bad formats immediately
            val date = dateFormat.parse(dateString.trim())
                ?: run {
                    Log.e(TAG, "Date parsed to null for input: '$dateString'")
                    return null
                }

            Calendar.getInstance().apply {
                time = date
                startMinutes?.let { min ->
                    set(Calendar.HOUR_OF_DAY, min / 60)
                    set(Calendar.MINUTE, min % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Exception parsing date '$dateString': ${e.message}")
            null
        }
    }
}