package com.example.test.data.repository

import com.example.test.data.dao.TaskDao
import com.example.test.data.models.Subtask
import com.example.test.data.models.Task
import com.example.test.notification.TaskEvent
import com.example.test.notification.TaskEventBus
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getTasksForUser(userId: Long): Flow<List<Task>> = taskDao.getTasksForUser(userId)
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun addTask(
        userId: Long,
        title: String,
        category: String,
        date: String,
        isScheduled: Boolean = false,
        scheduleStartMinutes: Int? = null,
        scheduleEndMinutes: Int? = null
    ) {
        val task = Task(
            userId = userId,
            title = title,
            category = category,
            date = date,
            isScheduled = isScheduled,
            scheduleStartMinutes = scheduleStartMinutes,
            scheduleEndMinutes = scheduleEndMinutes
        )
        taskDao.insertTask(task)

        if (isScheduled) {
            TaskEventBus.notifyObservers(TaskEvent.TaskScheduled(task, date))
        }
    }

    suspend fun toggleTask(taskId: Long, isChecked: Boolean) {
        taskDao.updateTaskChecked(taskId, isChecked)

        // Fetch the full task so observers get complete data
        val task = taskDao.getTaskById(taskId) ?: return

        if (isChecked) {
            TaskEventBus.notifyObservers(TaskEvent.TaskCompleted(task))
        } else {
            TaskEventBus.notifyObservers(TaskEvent.TaskUncompleted(task))
        }
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        TaskEventBus.notifyObservers(TaskEvent.TaskDeleted(task))
    }

    fun getSubtasks(taskId: Long): Flow<List<Subtask>> = taskDao.getSubtasks(taskId)

    suspend fun addSubtask(taskId: Long, title: String) {
        taskDao.insertSubtask(Subtask(taskId = taskId, title = title))
    }
}