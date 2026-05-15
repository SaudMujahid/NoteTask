package com.example.test.data.dao

import androidx.room.*
import com.example.test.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): Task?

    @Insert
    suspend fun insertSubtask(subtask: Subtask)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubtasks(taskId: Long): Flow<List<Subtask>>

    @Query("UPDATE tasks SET isChecked = :isChecked WHERE id = :taskId")
    suspend fun updateTaskChecked(taskId: Long, isChecked: Boolean)

    @Query("SELECT * FROM subtasks")
	suspend fun getAllSubtasks(): List<Subtask>

	@Query("DELETE FROM tasks")
	suspend fun deleteAllTasks()

	@Query("DELETE FROM subtasks")
	suspend fun deleteAllSubtasks()
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, dateModified DESC")
    fun getAllNotes(): Flow<List<Note>>
	

    @Query("DELETE FROM notes")
suspend fun deleteAllNotes()

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, dateModified DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>
}
