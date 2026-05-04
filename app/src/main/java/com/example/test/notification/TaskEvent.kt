package com.example.test.notification

// com/example/test/notifications/TaskEvent.kt

import com.example.test.data.models.Task

sealed class TaskEvent {
    data class TaskScheduled(val task: Task, val dateString: String) : TaskEvent()
    data class TaskDeleted(val task: Task) : TaskEvent()
    data class TaskCompleted(val task: Task) : TaskEvent()
    data class TaskUncompleted(val task: Task) : TaskEvent()
}