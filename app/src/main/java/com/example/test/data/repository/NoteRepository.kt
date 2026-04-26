package com.example.test.data.repository

import com.example.test.data.dao.NoteDao
import com.example.test.data.models.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun getNotesForUser(userId: Long): Flow<List<Note>> = noteDao.getNotesForUser(userId)

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }
}