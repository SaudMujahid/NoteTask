package com.example.test.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.test.data.models.AuthType
import com.example.test.data.models.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileRepository(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: ProfileRepository? = null

        fun getInstance(context: Context): ProfileRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProfileRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_profile_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If decryption fails (common AEADBadTagException), delete corrupted preferences and try again
        context.deleteSharedPreferences("secure_profile_prefs")
        EncryptedSharedPreferences.create(
            context,
            "secure_profile_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _profileFlow = MutableStateFlow(getProfile())
    val profileFlow: StateFlow<Profile> = _profileFlow.asStateFlow()

    fun getProfile(): Profile {
        return Profile(
            firstName = prefs.getString("first_name", "") ?: "",
            authType = AuthType.valueOf(
                prefs.getString("auth_type", AuthType.NONE.name) ?: AuthType.NONE.name
            ),
            credentialHash = prefs.getString("credential_hash", null),
            backupPinHash = prefs.getString("backup_pin_hash", null),
            salt = prefs.getString("salt", "") ?: "",
            isAppLockEnabled = prefs.getBoolean("is_app_lock_enabled", false)
        )
    }

    fun saveProfile(profile: Profile) {
        prefs.edit().apply {
            putString("first_name", profile.firstName)
            putString("auth_type", profile.authType.name)
            if (profile.credentialHash != null) {
                putString("credential_hash", profile.credentialHash)
            } else {
                remove("credential_hash")
            }
            if (profile.backupPinHash != null) {
                putString("backup_pin_hash", profile.backupPinHash)
            } else {
                remove("backup_pin_hash")
            }
            putString("salt", profile.salt)
            putBoolean("is_app_lock_enabled", profile.isAppLockEnabled)
            apply()
        }
        _profileFlow.value = profile
    }

    fun clearAuth() {
        val current = getProfile()
        saveProfile(
            current.copy(
                authType = AuthType.NONE,
                credentialHash = null,
                backupPinHash = null,
                isAppLockEnabled = false
            )
        )
    }
}
