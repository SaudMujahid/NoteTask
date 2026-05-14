package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test.data.repository.TaskRepository
import com.example.test.data.repository.NoteRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repository: Any) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TaskViewModel::class.java) ->
                TaskViewModel(repository as TaskRepository) as T
            modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                CalendarViewModel(repository as TaskRepository) as T
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> {
                NoteViewModel(repository as NoteRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
