package com.example.test.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.test.data.models.AuthType
import com.example.test.data.repository.ProfileRepository
import com.example.test.security.CryptoUtils
import kotlinx.coroutines.delay

@Composable
fun AppLockGate(
    profileRepository: ProfileRepository,
    content: @Composable () -> Unit
) {
    val profile by profileRepository.profileFlow.collectAsState()
    var isUnlocked by rememberSaveable { mutableStateOf(!profile.isAppLockEnabled || profile.authType == AuthType.NONE) }

    LaunchedEffect(profile.isAppLockEnabled, profile.authType) {
        if (!profile.isAppLockEnabled || profile.authType == AuthType.NONE) {
            isUnlocked = true
        }
    }

    if (isUnlocked) {
        content()
    } else {
        LockScreen(
            authType = profile.authType,
            onUnlock = { isUnlocked = true },
            profileRepository = profileRepository
        )
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun LockScreen(
    authType: AuthType,
    onUnlock: () -> Unit,
    profileRepository: ProfileRepository,
    modifier: Modifier = Modifier
) {
    val profile by profileRepository.profileFlow.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            shakeAnim.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    0f at 0
                    -10f at 50
                    10f at 100
                    -10f at 150
                    10f at 200
                    0f at 250
                }
            )
            shakeAnim.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .offset(x = shakeAnim.value.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isDark) Color.White else colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "App Locked",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Authenticate to continue",
                color = (if (isDark) Color.White else colorScheme.onBackground).copy(alpha = 0.6f),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(32.dp))

            when (authType) {
                AuthType.PIN -> PinChallenge(
                    onSubmit = { pin ->
                        if (CryptoUtils.hash(pin, profile.salt) == profile.credentialHash) {
                            onUnlock()
                        } else {
                            errorMsg = "Incorrect PIN"
                        }
                    }
                )
                AuthType.PASSWORD -> PasswordChallenge(
                    onSubmit = { password ->
                        if (CryptoUtils.hash(password, profile.salt) == profile.credentialHash) {
                            onUnlock()
                        } else {
                            errorMsg = "Incorrect password"
                        }
                    }
                )
                AuthType.BIOMETRIC -> BiometricChallenge(
                    onUnlock = onUnlock,
                    onBackupPinSubmit = { pin ->
                        CryptoUtils.hash(pin, profile.salt) == profile.backupPinHash
                    }
                )
                else -> { /* Should not happen */ }
            }

            errorMsg?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = colorScheme.error, fontSize = 14.sp)
                LaunchedEffect(it) {
                    delay(3000)
                    errorMsg = null
                }
            }
        }
    }
}

@Composable
fun NoteAuthDialog(
    profileRepository: ProfileRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val profile by profileRepository.profileFlow.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Lock,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text("Locked Note", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))

                when (profile.authType) {
                    AuthType.PIN -> PinChallenge(
                        onSubmit = { pin ->
                            if (CryptoUtils.hash(pin, profile.salt) == profile.credentialHash) {
                                onSuccess()
                            }
                        }
                    )
                    AuthType.PASSWORD -> PasswordChallenge(
                        onSubmit = { password ->
                            if (CryptoUtils.hash(password, profile.salt) == profile.credentialHash) {
                                onSuccess()
                            }
                        }
                    )
                    AuthType.BIOMETRIC -> BiometricChallenge(
                        onUnlock = onSuccess,
                        onBackupPinSubmit = { pin ->
                            CryptoUtils.hash(pin, profile.salt) == profile.backupPinHash
                        }
                    )
                    else -> Text("No security configured")
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun PinChallenge(onSubmit: (String) -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pin.length) (if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.primary)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )
        buttons.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(72.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    if (key == "⌫") {
                                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                    } else if (pin.length < 4) {
                                        pin += key
                                        if (pin.length == 4) {
                                            onSubmit(pin)
                                            pin = ""
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = if (key == "⌫") 22.sp else 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun PasswordChallenge(onSubmit: (String) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSubmit(password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Unlock")
        }
    }
}

@Composable
fun BiometricChallenge(
    onUnlock: () -> Unit,
    onBackupPinSubmit: (String) -> Boolean
) {
    val context = LocalContext.current
    // Unwrap safely — works inside Dialog {} too
    val activity = remember(context) { context.findFragmentActivity() }
    var showBackup by rememberSaveable { mutableStateOf(false) }
    var backupError by rememberSaveable { mutableStateOf<String?>(null) }

    fun launchBiometric() {
        activity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlock()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        showBackup = true
                    }
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock")
            .setSubtitle("Verify your identity")
            .setNegativeButtonText("Use Backup PIN")
            .build()
        prompt.authenticate(info)
    }

    // Fire automatically so the user doesn't need an extra tap
    LaunchedEffect(showBackup) {
        if (!showBackup) launchBiometric()
    }

    if (showBackup) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Enter Backup PIN", fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))
            PinChallenge(onSubmit = { pin ->
                if (onBackupPinSubmit(pin)) onUnlock()
                else backupError = "Incorrect PIN"
            })
            backupError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { showBackup = false }) { // triggers LaunchedEffect → re-fires biometric
                Text("Use Fingerprint")
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = ::launchBiometric,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = if (isSystemInDarkTheme()) Color.White
                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tap to unlock")
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { showBackup = true }) {
                Text("Use Backup PIN")
            }
        }
    }
}
