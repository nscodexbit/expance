package com.yourname.expensetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.ui.common.AmountKeypad
import com.yourname.expensetracker.ui.security.BiometricAuthHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentPin by sessionManager.pinHash.collectAsState(initial = null)
    val biometricEnabled by sessionManager.biometricEnabled.collectAsState(initial = false)
    var pinInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isBiometricSupported = remember { BiometricAuthHelper.isBiometricAvailable(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & Security", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PIN Lock Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (currentPin.isNullOrBlank()) "4-Digit App PIN" else "PIN Protection Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (currentPin.isNullOrBlank())
                            "Set a 4-digit PIN to safeguard your financial transactions and reports."
                        else
                            "App access is secured. Enter a new PIN below if you wish to change it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("4-digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (pinInput.length == 4) {
                                    scope.launch {
                                        sessionManager.setPinHash(pinInput)
                                        message = "PIN successfully updated!"
                                        isError = false
                                        pinInput = ""
                                    }
                                } else {
                                    message = "PIN must be exactly 4 digits."
                                    isError = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save PIN")
                        }

                        if (!currentPin.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        sessionManager.setPinHash("")
                                        sessionManager.setBiometricEnabled(false)
                                        message = "PIN and Biometric lock removed."
                                        isError = false
                                        pinInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Remove PIN")
                            }
                        }
                    }
                }
            }

            // Biometric Auth Card (Gated)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Biometric Unlock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBiometricSupported) "Fingerprint / Face recognition" else "Hardware not available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = biometricEnabled,
                            enabled = isBiometricSupported && !currentPin.isNullOrBlank(),
                            onCheckedChange = { enable ->
                                if (enable) {
                                    // Gate biometric activation with immediate BiometricPrompt verification
                                    (context as? FragmentActivity)?.let { activity ->
                                        BiometricAuthHelper.authenticate(
                                            activity = activity,
                                            title = "Verify Identity",
                                            subtitle = "Scan your fingerprint to activate biometric unlock",
                                            onSuccess = {
                                                scope.launch {
                                                    sessionManager.setBiometricEnabled(true)
                                                    message = "Biometric lock activated successfully."
                                                    isError = false
                                                }
                                            },
                                            onError = { err ->
                                                message = "Biometric verification failed: $err"
                                                isError = true
                                            }
                                        )
                                    } ?: run {
                                        message = "Unable to launch biometric prompt."
                                        isError = true
                                    }
                                } else {
                                    scope.launch {
                                        sessionManager.setBiometricEnabled(false)
                                        message = "Biometric unlock disabled."
                                        isError = false
                                    }
                                }
                            }
                        )
                    }

                    if (currentPin.isNullOrBlank()) {
                        Text(
                            text = "Note: Please set a 4-digit PIN before enabling biometric unlock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            message?.let {
                Surface(
                    color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
