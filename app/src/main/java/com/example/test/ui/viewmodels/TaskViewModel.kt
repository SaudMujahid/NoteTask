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

    // Called from MainActivity's LaunchedEffect once user is logged in
    fun setUser(userId: Long) {
        viewModelScope.launch {
            taskRepository.getTasksForUser(userId).collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(
        userId: Long,
        title: String,
        category: String,
        date: String,
        isScheduled: Boolean = false,
        scheduleStartMinutes: Int? = null,
        scheduleEndMinutes: Int? = null,
        onSaved: (Task) -> Unit = {}
    ) {
        viewModelScope.launch {
            val task = Task(
                userId               = userId,
                title                = title,
                category             = category,
                date                 = date,
                isScheduled          = isScheduled,
                scheduleStartMinutes = scheduleStartMinutes,
                scheduleEndMinutes   = scheduleEndMinutes
            )
            val insertedId = taskRepository.addTask(task)  // Room returns Long
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