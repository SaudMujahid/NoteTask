package com.example.test.data.models

data class Profile(
    val firstName: String = "",
    val authType: AuthType = AuthType.NONE,
    val credentialHash: String? = null,
    val backupPinHash: String? = null,
    val salt: String = "",
    val isAppLockEnabled: Boolean = false
)
