package com.example.test.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.test.data.AppDatabase
import com.example.test.data.repository.TaskRepository
import com.example.test.notification.TaskScheduler
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
                // Re-arm for tomorrow immediately, before doing any work
                TaskScheduler.scheduleDailyDigest(context)

                val dao = AppDatabase.getDatabase(context).taskDao()
                val repository = TaskRepository(dao)

                val today = getTodayDateString()
                val allTasks = repository.getAllTasks().first() // collect one emission from Flow

                allTasks.forEach { task ->
                    val type = when {
                        task.date < today && !task.isChecked -> "overdue"
                        task.date == today && !task.isChecked -> "upcoming"
                        else -> return@forEach  // skip completed or future tasks
                    }

                    val notifIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
                        putExtra("task_id", task.id)
                        putExtra("task_title", task.title)
                        putExtra("task_type", type)
                    }
                    context.sendBroadcast(notifIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in DailyDigestReceiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
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
    }
}