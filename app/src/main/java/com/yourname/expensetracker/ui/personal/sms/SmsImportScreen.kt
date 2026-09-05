package com.yourname.expensetracker.ui.personal.sms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.TransactionType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsImportScreen(
    onBack: () -> Unit,
    viewModel: SmsImportViewModel = hiltViewModel()
) {
    val results by viewModel.parsedResults.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.importedSuccess.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bank/UPI SMS Auto-Import") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Input / Paste Box
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Paste Bank / UPI SMS", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Paste any transaction SMS from SBI, HDFC, ICICI, Axis, Paytm, PhonePe or GPay to auto-extract details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("e.g. Rs 350.00 debited for Zomato via UPI...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            trailingIcon = {
                                IconButton(onClick = {
                                    clipboardManager.getText()?.let {
                                        inputText = it.text
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.parseSms(inputText)
                                inputText = ""
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = inputText.isNotBlank()
                        ) {
                            Text("Parse & Detect")
                        }
                    }
                }
            }

            // Detected Results
            item {
                Text("Detected Transactions (${results.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            if (results.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No pending SMS to import.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(results) { result ->
                    val isExpense = result.type == TransactionType.EXPENSE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(result.merchant, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Text(result.suggestedCategory, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "${if (isExpense) "-" else "+"}$currencySymbol ${String.format(Locale.getDefault(), "%.2f", result.amount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "\"${result.rawText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.importParsed(result) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add to Expenses")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
