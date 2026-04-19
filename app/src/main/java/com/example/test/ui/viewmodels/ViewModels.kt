package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.*
import com.example.test.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun signUp(firstName: String, lastName: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val id = userRepository.signUp(firstName, lastName, email, password)
            if (id > 0) {
                login(email, password, onSuccess)
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = userRepository.login(email, password)
            if (user != null) {
                _currentUser.value = user
                onSuccess()
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    private val _userId = MutableStateFlow<Long?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks = _userId.flatMapLatest { id ->
        if (id != null) taskRepository.getTasks(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUser(userId: Long) {
        _userId.value = userId
    }

    fun getTasks(userId: Long) = taskRepository.getTasks(userId)

    fun addTask(userId: Long, title: String, category: String, date: String) {
        viewModelScope.launch {
            taskRepository.addTask(userId, title, category, date)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun getSubtasks(taskId: Long) = taskRepository.getSubtasks(taskId)

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            taskRepository.addSubtask(taskId, title)
        }
    }
}

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    fun getNotes(userId: Long) = noteRepository.getNotes(userId)

    fun saveNote(userId: Long, content: String) {
        viewModelScope.launch {
            noteRepository.saveNote(userId, content)
        }
    }
}

class ViewModelFactory(private val repository: Any) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as UserRepository) as T
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> TaskViewModel(repository as TaskRepository) as T
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> NoteViewModel(repository as NoteRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
