package com.example.test.data.repository

import com.example.test.data.dao.NoteDao
import com.example.test.data.models.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getNotesForUser(userId: Long): Flow<List<Note>> = noteDao.getNotesForUser(userId)

    suspend fun saveNote(userId: Long, title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        val fullContent = if (title.isNotBlank()) "$title\n\n$content" else content
        noteDao.upsertNote(Note(userId = userId, content = fullContent))
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }
}