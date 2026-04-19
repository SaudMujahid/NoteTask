package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.Note
import com.example.test.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Called from MyApp's LaunchedEffect once the user logs in
    fun setUser(userId: Long) {
        viewModelScope.launch {
            noteRepository.getNotesForUser(userId).collect { noteList ->
                _notes.value = noteList
            }
        }
    }

    fun saveNote(userId: Long, title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            noteRepository.saveNote(userId, title, content)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }
}