package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.ListItem
import com.example.test.data.models.Note
import com.example.test.data.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed interface NoteSaveEvent {
    data class Success(val noteId: Long) : NoteSaveEvent
    data class Error(val message: String) : NoteSaveEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow("ALL")
    private val _saveEvents = MutableSharedFlow<NoteSaveEvent>(extraBufferCapacity = 1)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val filterType: StateFlow<String> = _filterType.asStateFlow()
    val saveEvents: SharedFlow<NoteSaveEvent> = _saveEvents.asSharedFlow()

    val notes: StateFlow<List<Note>> = combine(_searchQuery, _filterType) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        repository.getAllNotes().map { list ->
            list.filter { note ->
                val matchesQuery = query.isBlank() ||
                        note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
                val matchesFilter = filter == "ALL" || note.type == filter
                matchesQuery && matchesFilter
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setFilterType(t: String) { _filterType.value = t }

    fun addNote(note: Note): Job = viewModelScope.launch {
        try {
            val insertedId = repository.insert(note)
            _saveEvents.emit(NoteSaveEvent.Success(insertedId))
        } catch (e: Exception) {
            _saveEvents.emit(NoteSaveEvent.Error(e.message ?: "Failed to save note"))
        }
    }

    fun updateNote(note: Note): Job = viewModelScope.launch {
        try {
            repository.updateNote(note.copy(dateModified = System.currentTimeMillis()))
            _saveEvents.emit(NoteSaveEvent.Success(note.id))
        } catch (e: Exception) {
            _saveEvents.emit(NoteSaveEvent.Error(e.message ?: "Failed to save note"))
        }
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun togglePin(note: Note) = viewModelScope.launch {
        repository.updateNote(note.copy(isPinned = !note.isPinned))
    }

    fun toggleLock(note: Note) = viewModelScope.launch {
        repository.updateNote(note.copy(isLocked = !note.isLocked, dateModified = System.currentTimeMillis()))
    }

    fun parseListItems(json: String): List<ListItem> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ListItem(
                obj.getString("id"),
                obj.getString("text"),
                obj.getBoolean("isChecked")
            )
        }
    } catch (e: Exception) { emptyList() }

    fun serializeListItems(items: List<ListItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("isChecked", item.isChecked)
            })
        }
        return arr.toString()
    }
}
