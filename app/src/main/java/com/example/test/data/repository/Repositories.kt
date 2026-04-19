package com.example.test.data.repository

import com.example.test.data.dao.UserDao
import com.example.test.data.models.User
import org.mindrot.jbcrypt.BCrypt

class UserRepository(private val userDao: UserDao) {
    suspend fun signUp(firstName: String, lastName: String, email: String, password: String): Result<User> {
        return try {
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
            val user = User(firstName = firstName, lastName = lastName, email = email, passwordHash = passwordHash)
            val id = userDao.insert(user)
            Result.success(user.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}