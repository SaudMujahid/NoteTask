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

    init {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(
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
                title                = title,
                description          = description,
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

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
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
