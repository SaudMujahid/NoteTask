package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.Task
import com.example.test.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var userJob: kotlinx.coroutines.Job? = null

    fun setUser(userId: Long) {
        userJob?.cancel()
        userJob = viewModelScope.launch {
            taskRepository.getTasksForUser(userId).collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(
        userId: Long,
        title: String,
        description: String = "",
        category: String,
        date: String,
        isScheduled: Boolean = false,
        scheduleStartMinutes: Int? = null,
        scheduleEndMinutes: Int? = null,
        notificationMinutes: Int? = null,
        onSaved: (Task) -> Unit = {}
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {
            val task = Task(
                userId               = userId,
                title                = title,
                description           = description,
                category             = category,
                date                 = date,
                isScheduled          = isScheduled,
                scheduleStartMinutes = scheduleStartMinutes,
                scheduleEndMinutes   = scheduleEndMinutes,
                notificationMinutes  = notificationMinutes
            )
            val insertedId = taskRepository.addTask(task)
            onSaved(task.copy(id = insertedId))
        }
    }



    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTask(task.id, !task.isChecked)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}