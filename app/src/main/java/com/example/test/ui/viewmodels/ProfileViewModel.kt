package com.example.test.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.test.data.models.AuthType
import com.example.test.data.models.Profile
import com.example.test.data.repository.ProfileRepository
import com.example.test.security.CryptoUtils
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository.getInstance(application)
    val profile: StateFlow<Profile> = repository.profileFlow

    var setupState by mutableStateOf<SetupState>(SetupState.Idle)
        private set

    sealed class SetupState {
        object Idle : SetupState()
        object PinSetup : SetupState()
        object PasswordSetup : SetupState()
        object FingerprintDisclaimer : SetupState()
        object FingerprintBackupPin : SetupState()
        object Success : SetupState()
        class Error(val message: String) : SetupState()
    }

    fun updateFirstName(name: String) {
        val current = profile.value
        repository.saveProfile(current.copy(firstName = name))
    }

    fun startPinSetup() {
        setupState = SetupState.PinSetup
    }

    fun startPasswordSetup() {
        setupState = SetupState.PasswordSetup
    }

    fun startFingerprintSetup() {
        setupState = SetupState.FingerprintDisclaimer
    }

    fun dismissFingerprintDisclaimer() {
        setupState = SetupState.FingerprintBackupPin
    }

    fun toggleAppLock(enabled: Boolean) {
        val current = profile.value
        repository.saveProfile(current.copy(isAppLockEnabled = enabled))
    }

    fun savePin(pin: String) {
        val salt = CryptoUtils.generateSalt()
        val hash = CryptoUtils.hash(pin, salt)
        val current = profile.value
        repository.saveProfile(
            current.copy(
                authType = AuthType.PIN,
                credentialHash = hash,
                salt = salt,
                backupPinHash = null,
                isAppLockEnabled = true
            )
        )
        setupState = SetupState.Success
    }

    fun savePassword(password: String) {
        val salt = CryptoUtils.generateSalt()
        val hash = CryptoUtils.hash(password, salt)
        val current = profile.value
        repository.saveProfile(
            current.copy(
                authType = AuthType.PASSWORD,
                credentialHash = hash,
                salt = salt,
                backupPinHash = null,
                isAppLockEnabled = true
            )
        )
        setupState = SetupState.Success
    }

    fun saveFingerprintWithBackupPin(pin: String) {
        val salt = CryptoUtils.generateSalt()
        val hash = CryptoUtils.hash(pin, salt)
        val current = profile.value
        repository.saveProfile(
            current.copy(
                authType = AuthType.BIOMETRIC,
                credentialHash = null,
                backupPinHash = hash,
                salt = salt,
                isAppLockEnabled = true
            )
        )
        setupState = SetupState.Success
    }

    fun verifyPin(input: String): Boolean {
        val p = profile.value
        return p.credentialHash != null &&
                CryptoUtils.hash(input, p.salt) == p.credentialHash
    }

    fun verifyBackupPin(input: String): Boolean {
        val p = profile.value
        return p.backupPinHash != null &&
                CryptoUtils.hash(input, p.salt) == p.backupPinHash
    }

    fun verifyPassword(input: String): Boolean {
        val p = profile.value
        return p.credentialHash != null &&
                CryptoUtils.hash(input, p.salt) == p.credentialHash
    }

    fun clearSecurity() {
        repository.clearAuth()
        setupState = SetupState.Idle
    }

    fun resetSetupState() {
        setupState = SetupState.Idle
    }
}
