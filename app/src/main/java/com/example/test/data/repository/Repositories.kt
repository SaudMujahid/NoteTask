package com.example.test.data.repository

import com.example.test.data.dao.*
import com.example.test.data.models.*
import kotlinx.coroutines.flow.Flow
import org.mindrot.jbcrypt.BCrypt

class UserRepository(private val userDao: UserDao) {
    suspend fun signUp(firstName: String, lastName: String, email: String, password: String): Long {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(firstName = firstName, lastName = lastName, email = email, passwordHash = passwordHash)
        return userDao.insert(user)
    }

    suspend fun login(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }
}

class TaskRepository(private val taskDao: TaskDao) {
    fun getTasks(userId: Long): Flow<List<Task>> = taskDao.getTasksForUser(userId)

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    suspend fun addTask(userId: Long, title: String, category: String, date: String) {
        taskDao.insertTask(Task(userId = userId, title = title, category = category, date = date))
    }

    suspend fun toggleTask(task: Task) {
        taskDao.updateTask(task.copy(isChecked = !task.isChecked))
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    fun getSubtasks(taskId: Long): Flow<List<Subtask>> = taskDao.getSubtasks(taskId)

    suspend fun addSubtask(taskId: Long, title: String) {
        taskDao.insertSubtask(Subtask(taskId = taskId, title = title))
    }
}

class NoteRepository(private val noteDao: NoteDao) {
    fun getNotes(userId: Long): Flow<List<Note>> = noteDao.getNotesForUser(userId)

    suspend fun saveNote(userId: Long, content: String) {
        noteDao.upsertNote(Note(userId = userId, content = content))
    }
}
