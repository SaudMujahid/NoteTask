package com.example.test.notification

import android.content.Context

class NotificationObserver(private val context: Context) : TaskObserver {

    override fun onTaskEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.TaskScheduled -> {
                TaskScheduler.scheduleTaskNotification(
                    context = context,
                    task = event.task,
                    taskDate = event.dateString
                )
            }
            is TaskEvent.TaskDeleted -> {
                TaskScheduler.cancelTaskNotification(context, event.task)
            }
            is TaskEvent.TaskCompleted -> {
                TaskScheduler.cancelTaskNotification(context, event.task)
            }
            is TaskEvent.TaskUncompleted -> {
                // Only re-schedule if this task was originally scheduled or has a reminder
                if (event.task.isScheduled || event.task.notificationMinutes != null) {
                    TaskScheduler.scheduleTaskNotification(
                        context = context,
                        task = event.task,
                        taskDate = event.task.date
                    )
                }
            }
        }
    }
}