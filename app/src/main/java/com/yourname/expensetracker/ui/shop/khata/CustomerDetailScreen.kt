package com.yourname.expensetracker.ui.shop.khata

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.CreditEntry
import com.yourname.expensetracker.data.local.entity.CreditType
import com.yourname.expensetracker.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onBack: () -> Unit,
    viewModel: KhataViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    var showEntryDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var entryType by remember { mutableStateOf(CreditType.CREDIT_GIVEN) }
    var entryToDelete by remember { mutableStateOf<CreditEntry?>(null) }

    LaunchedEffect(customerId) {
        viewModel.loadCustomerDetail(customerId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDetail() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.customer?.name ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val name = state.customer?.name ?: "Customer"
                        val balance = state.outstanding
                        val shareText = "Khata Account Statement for $name\n" +
                                "Current Balance Due: ${CurrencyHelper.format(balance, "₹")}\n" +
                                "Total Transactions: ${state.entries.size}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Khata Statement"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Statement")
                    }
                    TextButton(onClick = { showLimitDialog = true }) {
                        Text("Set Limit")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.outstanding > 0)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Outstanding Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            CurrencyHelper.format(state.outstanding, "₹"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.outstanding > 0)
                                MaterialTheme.colorScheme.error
                            else Color(0xFF2E7D32)
                        )
                        val limit = state.customer?.creditLimit ?: 0.0
                        if (limit > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Credit Limit: ${CurrencyHelper.format(limit, "₹")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (state.outstanding > limit) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("LIMIT EXCEEDED", color = MaterialTheme.colorScheme.onError, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            entryType = CreditType.CREDIT_GIVEN
                            showEntryDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("+ Credit Given")
                    }
                    Button(
                        onClick = {
                            entryType = CreditType.PAYMENT_RECEIVED
                            showEntryDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Receive Payment")
                    }
                }
            }

            state.customer?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                item {
                    val msg = "Dear ${state.customer?.name ?: ""}, your outstanding balance with us is ${CurrencyHelper.format(state.outstanding, "₹")}. Kindly clear it. Thank you."
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("sms:$phone"))
                                intent.putExtra("sms_body", msg)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS")
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    val uri = Uri.parse("whatsapp://send?phone=${phone.replace(" ", "").replace("+", "")}&text=${Uri.encode(msg)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e: Exception) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Send Reminder"))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WhatsApp")
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Customer", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            item {
                Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            if (state.entries.isEmpty()) {
                item {
                    Text(
                        "No credit entries yet. Use the buttons above to record credit or payments.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            items(state.entries, key = { it.id }) { entry ->
                EntryRow(entry = entry, onDelete = { entryToDelete = entry })
            }
        }
    }

    if (showEntryDialog) {
        EntryDialog(
            isCredit = entryType == CreditType.CREDIT_GIVEN,
            onDismiss = { showEntryDialog = false },
            onConfirm = { amount, note ->
                viewModel.addCreditEntry(customerId, entryType, amount, note)
                showEntryDialog = false
            }
        )
    }

    if (showLimitDialog) {
        var limitText by remember { mutableStateOf(state.customer?.creditLimit?.takeIf { it > 0 }?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Set Credit Limit") },
            text = {
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Max Credit Limit (₹)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    state.customer?.let {
                        viewModel.updateCustomer(it.copy(creditLimit = limit))
                    }
                    showLimitDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Entry?") },
            text = {
                Text("Are you sure you want to delete this ${if (entry.type == CreditType.CREDIT_GIVEN) "credit" else "payment"} entry of ${CurrencyHelper.format(entry.amount, "₹")}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCreditEntry(entry)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EntryRow(entry: CreditEntry, onDelete: () -> Unit) {
    val isCredit = entry.type == CreditType.CREDIT_GIVEN
    val color = if (isCredit) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCredit) "Credit Given" else "Payment Received",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                entry.note?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    formatDate(entry.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${if (isCredit) "+" else "-"} ${CurrencyHelper.format(entry.amount, "₹")}",
                fontWeight = FontWeight.Bold,
                color = color
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun EntryDialog(isCredit: Boolean, onDismiss: () -> Unit, onConfirm: (Double, String?) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCredit) "Add Credit Given" else "Receive Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (items, invoice, reason)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) onConfirm(amt, note.ifBlank { null })
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(timestamp)
}
