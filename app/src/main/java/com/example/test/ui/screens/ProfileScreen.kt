package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test.data.models.AuthType
import com.example.test.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val setupState = viewModel.setupState
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    var nameField by rememberSaveable { mutableStateOf(profile.firstName) }
    var isEditingName by rememberSaveable { mutableStateOf(false) }
    var showNameWarning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(profile.firstName) {
        if (!isEditingName) {
            nameField = profile.firstName
        }
    }

    var showPinDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showFingerprintDisclaimer by rememberSaveable { mutableStateOf(false) }
    var showFingerprintPinDialog by rememberSaveable { mutableStateOf(false) }
    var showSecurityConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(setupState) {
        when (setupState) {
            is ProfileViewModel.SetupState.PinSetup -> showPinDialog = true
            is ProfileViewModel.SetupState.PasswordSetup -> showPasswordDialog = true
            is ProfileViewModel.SetupState.FingerprintDisclaimer -> showFingerprintDisclaimer = true
            is ProfileViewModel.SetupState.FingerprintBackupPin -> {
                showFingerprintDisclaimer = false
                showFingerprintPinDialog = true
            }
            is ProfileViewModel.SetupState.Success -> {
                showPinDialog = false
                showPasswordDialog = false
                showFingerprintPinDialog = false
                viewModel.resetSetupState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Black, color = if (isDark) Color.White else colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Name Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Your Name", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { if (isEditingName) nameField = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        enabled = isEditingName,
                        readOnly = !isEditingName,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditingName) {
                            TextButton(
                                onClick = {
                                    nameField = profile.firstName
                                    isEditingName = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                        Button(
                            onClick = {
                                if (isEditingName) {
                                    viewModel.updateFirstName(nameField)
                                    isEditingName = false
                                } else {
                                    showNameWarning = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = if (isDark) Color.White else colorScheme.onPrimary
                            )
                        ) {
                            Text(if (isEditingName) "Save Name" else "Change Name")
                        }
                    }
                }
            }

            // Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("App Security", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Authentication: ${profile.authType.displayName()}",
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    AuthOptionCard(
                        title = "4-Digit PIN",
                        subtitle = "Numeric passcode",
                        icon = Icons.Default.Lock,
                        isSelected = profile.authType == AuthType.PIN,
                        onClick = { viewModel.startPinSetup() }
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthOptionCard(
                        title = "Password",
                        subtitle = "Alphanumeric passcode",
                        icon = Icons.Default.Password,
                        isSelected = profile.authType == AuthType.PASSWORD,
                        onClick = { viewModel.startPasswordSetup() }
                    )
                    Spacer(Modifier.height(10.dp))
                    AuthOptionCard(
                        title = "Fingerprint",
                        subtitle = "Biometric authentication",
                        icon = Icons.Default.Fingerprint,
                        isSelected = profile.authType == AuthType.BIOMETRIC,
                        onClick = { viewModel.startFingerprintSetup() }
                    )

                    if (profile.authType != AuthType.NONE) {
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { showSecurityConfirmation = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Remove Security", color = colorScheme.error)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showSecurityConfirmation) {
        AlertDialog(
            onDismissRequest = { showSecurityConfirmation = false },
            title = { Text("Confirm Removal") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Please verify your identity to remove security.", 
                        fontSize = 14.sp, 
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    when (profile.authType) {
                        AuthType.PIN -> PinChallenge(
                            onSubmit = { pin ->
                                if (viewModel.verifyPin(pin)) {
                                    viewModel.clearSecurity()
                                    showSecurityConfirmation = false
                                }
                            }
                        )
                        AuthType.PASSWORD -> PasswordChallenge(
                            onSubmit = { password ->
                                if (viewModel.verifyPassword(password)) {
                                    viewModel.clearSecurity()
                                    showSecurityConfirmation = false
                                }
                            }
                        )
                        AuthType.BIOMETRIC -> BiometricChallenge(
                            onUnlock = {
                                viewModel.clearSecurity()
                                showSecurityConfirmation = false
                            },
                            onBackupPinSubmit = { pin ->
                                if (viewModel.verifyBackupPin(pin)) {
                                    viewModel.clearSecurity()
                                    showSecurityConfirmation = false
                                    true
                                } else false
                            }
                        )
                        else -> {}
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSecurityConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Name change warning dialog
    if (showNameWarning) {
        AlertDialog(
            onDismissRequest = { showNameWarning = false },
            title = { Text("Change Name?") },
            text = {
                Text("Are you sure you want to change your name? No information will be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNameWarning = false
                        isEditingName = true
                    }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showNameWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPinDialog) {
        PinSetupDialog(
            title = "Set 4-Digit PIN",
            subtitle = "Enter and confirm your PIN",
            onDismiss = {
                showPinDialog = false
                viewModel.resetSetupState()
            },
            onConfirm = { viewModel.savePin(it) }
        )
    }

    if (showPasswordDialog) {
        PasswordSetupDialog(
            onDismiss = {
                showPasswordDialog = false
                viewModel.resetSetupState()
            },
            onConfirm = { viewModel.savePassword(it) }
        )
    }

    if (showFingerprintDisclaimer) {
        FingerprintDisclaimerDialog(
            onDismiss = {
                showFingerprintDisclaimer = false
                viewModel.resetSetupState()
            },
            onContinue = { viewModel.dismissFingerprintDisclaimer() }
        )
    }

    if (showFingerprintPinDialog) {
        PinSetupDialog(
            title = "Backup PIN",
            subtitle = "Required when fingerprint is unavailable",
            onDismiss = {
                showFingerprintPinDialog = false
                viewModel.resetSetupState()
            },
            onConfirm = { viewModel.saveFingerprintWithBackupPin(it) }
        )
    }
}

@Composable
private fun AuthOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) (if (isDark) Color.White else colorScheme.primary) else colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                if (isSelected) colorScheme.primary.copy(alpha = 0.08f)
                else colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isDark) Color.White else colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = if (isDark) Color.White else colorScheme.onSurface)
            Text(
                subtitle,
                fontSize = 13.sp,
                color = (if (isDark) Color.White else colorScheme.onSurface).copy(alpha = 0.5f)
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = if (isDark) Color.White else colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun AuthType.displayName(): String = when (this) {
    AuthType.NONE -> "None"
    AuthType.PIN -> "4-Digit PIN"
    AuthType.PASSWORD -> "Password"
    AuthType.BIOMETRIC -> "Fingerprint"
}

@Composable
private fun PinSetupDialog(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin1 by rememberSaveable { mutableStateOf("") }
    var pin2 by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(20.dp))

                // First PIN Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (index < pin1.length) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Second PIN Row (Confirm) - "alot like the name thing"
                AnimatedVisibility(
                    visible = pin1.length == 4,
                    enter = expandVertically() + fadeIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        Text("Confirm your PIN", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(
                                            if (index < pin2.length) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
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
                                Spacer(Modifier.size(64.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            when (key) {
                                                "⌫" -> {
                                                    if (pin2.isNotEmpty()) {
                                                        pin2 = pin2.dropLast(1)
                                                    } else if (pin1.isNotEmpty()) {
                                                        pin1 = pin1.dropLast(1)
                                                    }
                                                    error = null
                                                }
                                                else -> {
                                                    if (pin1.length < 4) {
                                                        pin1 += key
                                                    } else if (pin2.length < 4) {
                                                        pin2 += key
                                                        if (pin2.length == 4) {
                                                            if (pin1 == pin2) {
                                                                onConfirm(pin1)
                                                            } else {
                                                                error = "PINs do not match"
                                                                pin2 = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = if (key == "⌫") 20.sp else 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PasswordSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pass1 by rememberSaveable { mutableStateOf("") }
    var pass2 by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = pass1,
                    onValueChange = { pass1 = it; error = null },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Confirm Password Field - "alot like the name thing"
                AnimatedVisibility(
                    visible = pass1.isNotEmpty(),
                    enter = expandVertically() + fadeIn()
                ) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pass2,
                            onValueChange = { pass2 = it; error = null },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        pass1.length < 4 -> error = "Minimum 4 characters"
                        pass1 != pass2 -> error = "Passwords do not match"
                        else -> onConfirm(pass1)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FingerprintDisclaimerDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Fingerprint Authentication") },
        text = {
            Column {
                Text("Your fingerprint data is never stored by this app.")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Authentication is handled entirely by your device's secure hardware. " +
                            "Everything remains local to this device."
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "You will need to set a backup PIN for situations when fingerprint is unavailable.",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(onClick = onContinue) { Text("Continue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
