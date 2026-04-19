package com.example.test.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.models.User
import com.example.test.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Called from SignUpScreen — onSuccess is run on the UI after success
    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (firstName.isBlank() || lastName.isBlank()) {
            _authState.value = AuthState.Error("Please enter your full name")
            return
        }
        if (!email.contains("@") || email.isBlank()) {
            _authState.value = AuthState.Error("Please enter a valid email")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = userRepository.signUp(firstName, lastName, email, password)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Sign up failed")
                }
            )
        }
    }

    // Called from LoginScreen
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = userRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Invalid email or password")
                }
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetError() {
        _authState.value = AuthState.Idle
    }
}
