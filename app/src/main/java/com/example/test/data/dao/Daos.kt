package com.example.test.data.dao

import androidx.room.*
import com.example.test.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>
}

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY id DESC")
    fun getTasksForUser(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertSubtask(subtask: Subtask)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubtasks(taskId: Long): Flow<List<Subtask>>
}

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Upsert
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getNotesForUser(userId: Long): Flow<List<Note>>
}
