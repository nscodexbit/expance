package com.yourname.expensetracker.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.yourname.expensetracker.data.local.entity.BackupLog
import com.yourname.expensetracker.data.local.entity.BackupStatus
import com.yourname.expensetracker.data.local.entity.BackupType
import com.yourname.expensetracker.data.repository.BackupRepository
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    backupRepository: BackupRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val logs by backupRepository.getBackupLogs().collectAsState(initial = emptyList())

    var isProcessing by remember { mutableStateOf(false) }
    var restoreConfirmPendingUri by remember { mutableStateOf<Uri?>(null) }
    var restoreSuccessMessage by remember { mutableStateOf<String?>(null) }

    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            restoreConfirmPendingUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "100% On-Device Data Portability",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Export and import your entire financial database as a standard JSON file. No external servers or internet required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Export Local File Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Export Backup (.json)", fontWeight = FontWeight.SemiBold)
                                Text("Generates a complete JSON backup file of all transactions, customers & suppliers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    val result = backupRepository.createLocalBackup()
                                    isProcessing = false
                                    if (result.isSuccess) {
                                        val file = result.getOrNull()
                                        if (file != null) {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Backup File"))
                                            snackbarHostState.showSnackbar("Backup exported successfully!")
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to create backup.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            Text("Export & Share JSON Backup")
                        }
                    }
                }
            }

            // Import / Restore JSON File Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import & Restore (.json)", fontWeight = FontWeight.SemiBold)
                                Text("Select a previously exported JSON backup to merge and restore your records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                jsonFilePickerLauncher.launch("*/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            Text("Select JSON File to Restore")
                        }
                    }
                }
            }

            // Backup History Logs
            item {
                Text(
                    text = "Backup History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (logs.isEmpty()) {
                item {
                    Text(
                        "No backups created yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (log.type == BackupType.DRIVE) Icons.Default.CloudDone else Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (log.type == BackupType.DRIVE) "Google Drive Backup" else "Local File Backup",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (log.status == BackupStatus.SUCCESS) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = log.status.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (log.status == BackupStatus.SUCCESS) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (restoreConfirmPendingUri != null) {
        val uri = restoreConfirmPendingUri!!
        AlertDialog(
            onDismissRequest = { restoreConfirmPendingUri = null },
            title = { Text("Restore From Backup?") },
            text = {
                Text("This will parse the selected JSON backup file and merge all valid transactions, customers, and suppliers into your database.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pendingUri = restoreConfirmPendingUri
                        restoreConfirmPendingUri = null
                        if (pendingUri != null) {
                            scope.launch {
                                isProcessing = true
                                try {
                                    val inputStream = context.contentResolver.openInputStream(pendingUri)
                                    val reader = BufferedReader(InputStreamReader(inputStream))
                                    val jsonString = reader.readText()
                                    reader.close()
                                    inputStream?.close()

                                    val result = backupRepository.restoreFromJson(jsonString)
                                    isProcessing = false
                                    if (result.isSuccess) {
                                        val count = result.getOrNull() ?: 0
                                        restoreSuccessMessage = "Successfully restored $count entries from backup file!"
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to parse or restore backup JSON.")
                                    }
                                } catch (e: Exception) {
                                    isProcessing = false
                                    snackbarHostState.showSnackbar("Error reading file: ${e.localizedMessage}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreConfirmPendingUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (restoreSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { restoreSuccessMessage = null },
            title = { Text("Restore Completed") },
            text = { Text(restoreSuccessMessage ?: "") },
            confirmButton = {
                Button(onClick = { restoreSuccessMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}
