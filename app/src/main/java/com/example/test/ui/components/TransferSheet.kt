package com.example.test.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.data.DataTransferManager
import com.example.test.data.models.BackupData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    transferManager: DataTransferManager,
    userName: String,
    onDismiss: () -> Unit,
    onRestored: (userName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var pendingImportBackup by remember { mutableStateOf<BackupData?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }

    // Conflict dialog — shown when device already has data
    if (showConflictDialog && pendingImportBackup != null) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false; pendingImportBackup = null },
            title = { Text("You already have data", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Merge adds the imported data alongside what you already have.\n\n" +
                            "Replace All deletes everything and restores only the imported data.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConflictDialog = false
                    val backup = pendingImportBackup ?: return@TextButton
                    pendingImportBackup = null
                    scope.launch {
                        isLoading = true
                        statusMessage = null
                        try {
                            transferManager.mergeBackup(backup)
                            statusMessage = true to "Merged successfully."
                        } catch (e: Exception) {
                            statusMessage = false to "Merge failed: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) { Text("Merge") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConflictDialog = false
                        val backup = pendingImportBackup ?: return@TextButton
                        pendingImportBackup = null
                        scope.launch {
                            isLoading = true
                            statusMessage = null
                            try {
                                transferManager.restoreBackup(backup)
                                onRestored(backup.userName)
                                statusMessage = true to "Restored successfully."
                            } catch (e: Exception) {
                                statusMessage = false to "Restore failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) { Text("Replace All") }
            }
        )
    }

    // SAF launcher for export — system file picker IS the permission grant
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            statusMessage = null
            try {
                val backup = transferManager.buildBackup(userName)
                val json = transferManager.toJson(backup)
                transferManager.writeToUri(context, uri, json)
                statusMessage = true to "Export successful! File saved."
            } catch (e: Exception) {
                statusMessage = false to "Export failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // SAF launcher for import — system file picker IS the permission grant
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            statusMessage = null
            try {
                val json = transferManager.readFromUri(context, uri)
                val backup = transferManager.fromJson(json)
                val hasData = transferManager.hasExistingData()
                isLoading = false
                if (hasData) {
                    pendingImportBackup = backup
                    showConflictDialog = true
                } else {
                    // Nothing to conflict with — restore directly
                    isLoading = true
                    transferManager.restoreBackup(backup)
                    onRestored(backup.userName)
                    statusMessage = true to "Restored successfully."
                    isLoading = false
                }
            } catch (e: Exception) {
                statusMessage = false to "Import failed: ${e.message}"
                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Transfer Data",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(40.dp)
                    .height(3.dp)
                    .background(colorScheme.primary, RoundedCornerShape(50))
            )

            Spacer(Modifier.height(20.dp))

            // Explanation card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = "Exports all your tasks, subtasks, and notes to a JSON file. " +
                                "To move to a new device, export here then import on the other device.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Export button
            Button(
                onClick = {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                    exportLauncher.launch("tasks_backup_$timestamp.json")
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Export as JSON", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Import button
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Import from JSON", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            // Warning note
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    "Importing will replace all current data.",
                    fontSize = 11.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            // Loading indicator
            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                }
            }

            // Status message
            AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                statusMessage?.let { (success, message) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (success) Color(0xFF4CAF50).copy(alpha = 0.12f)
                        else colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            color = if (success) Color(0xFF2E7D32) else colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
