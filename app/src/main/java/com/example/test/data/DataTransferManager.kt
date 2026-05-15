package com.example.test.data

import android.content.Context
import android.net.Uri
import com.example.test.data.dao.NoteDao
import com.example.test.data.dao.TaskDao
import com.example.test.data.models.BackupData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DataTransferManager(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun buildBackup(userName: String): BackupData {
        val tasks = taskDao.getAllTasks().first()
        val subtasks = taskDao.getAllSubtasks()
        val notes = noteDao.getAllNotes().first()
        return BackupData(
            exportedAt = System.currentTimeMillis(),
            userName = userName,
            tasks = tasks,
            subtasks = subtasks,
            notes = notes
        )
    }

    fun toJson(backup: BackupData): String = gson.toJson(backup)

    fun fromJson(json: String): BackupData = gson.fromJson(json, BackupData::class.java)

    suspend fun writeToUri(context: Context, uri: Uri, json: String) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                ?: error("Could not open output stream")
        }
    }

    suspend fun readFromUri(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                ?: error("Could not open input stream")
        }
    }

    suspend fun restoreBackup(backup: BackupData) {
        if (backup.version > 1) {
        // Future: handle migrations here before restoring
        // e.g. if (backup.version == 2) transformV2Fields(backup)
    }
    taskDao.deleteAllSubtasks()
    taskDao.deleteAllTasks()
    noteDao.deleteAllNotes()

    backup.tasks.forEach { task ->
        val oldId = task.id
        val newId = taskDao.insertTask(task.copy(id = 0))
        backup.subtasks
            .filter { it.taskId == oldId }
            .forEach { subtask ->
                taskDao.insertSubtask(subtask.copy(id = 0, taskId = newId))
            }
    }
    backup.notes.forEach { note ->
        noteDao.insert(note.copy(id = 0))
    }
    }
    suspend fun hasExistingData(): Boolean {
    val tasks = taskDao.getAllTasks().first()
    val notes = noteDao.getAllNotes().first()
    return tasks.isNotEmpty() || notes.isNotEmpty()
}

// Appends imported data on top of existing — no deletions
suspend fun mergeBackup(backup: BackupData) {
    backup.tasks.forEach { task ->
        val oldId = task.id
        val newId = taskDao.insertTask(task.copy(id = 0))
        backup.subtasks
            .filter { it.taskId == oldId }
            .forEach { subtask ->
                taskDao.insertSubtask(subtask.copy(id = 0, taskId = newId))
            }
    }
    backup.notes.forEach { note ->
        noteDao.insert(note.copy(id = 0))
    }
}
}
