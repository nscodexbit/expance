package com.yourname.expensetracker.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.yourname.expensetracker.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    sessionManager: SessionManager,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val biometricEnabled by sessionManager.biometricEnabled.collectAsState(initial = false)

    fun launchBiometric() {
        (context as? FragmentActivity)?.let { activity ->
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = "Unlock Expense Tracker",
                subtitle = "Confirm fingerprint or face to unlock",
                onSuccess = onUnlocked,
                onError = {
                    // fall back to PIN silently or show hint
                }
            )
        }
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && BiometricAuthHelper.isBiometricAvailable(context)) {
            launchBiometric()
        }
    }

    fun handleDigit(d: String) {
        if (pin.length < 4) {
            val newPin = pin + d
            pin = newPin
            error = false
            if (newPin.length == 4) {
                scope.launch {
                    val savedPin = sessionManager.pinHash.firstOrNull()
                    if (savedPin.isNullOrBlank() || savedPin == newPin) {
                        onUnlocked()
                    } else {
                        error = true
                        pin = ""
                    }
                }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Enter App PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (error) "Incorrect PIN, please try again" else "Protecting your financial data",
                style = MaterialTheme.typography.bodyMedium,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val filled = i < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (k in row) {
                        if (k == "BIO") {
                            if (biometricEnabled && BiometricAuthHelper.isBiometricAvailable(context)) {
                                IconButton(
                                    onClick = { launchBiometric() },
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        contentDescription = "Fingerprint",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(64.dp))
                            }
                        } else if (k == "DEL") {
                            IconButton(
                                onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete")
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable { handleDigit(k) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = k,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}
