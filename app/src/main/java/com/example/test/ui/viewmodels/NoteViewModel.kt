package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.ListItem
import com.example.test.data.models.Note
import com.example.test.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _userId = MutableStateFlow(0L)
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow("ALL")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(_userId, _searchQuery, _filterType) { u, q, f ->
        Triple(u, q, f)
    }.flatMapLatest { (userId, query, filter) ->
        val flow = if (query.isEmpty()) {
            repository.getNotesForUser(userId)
        } else {
            repository.searchNotes(userId, query)
        }

        flow.map { list ->
            if (filter == "ALL") list else list.filter { it.type == filter }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUser(id: Long) { _userId.value = id }
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setFilterType(t: String) { _filterType.value = t }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note.copy(dateModified = System.currentTimeMillis()))
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun togglePin(note: Note) = viewModelScope.launch {
        repository.updateNote(note.copy(isPinned = !note.isPinned))
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