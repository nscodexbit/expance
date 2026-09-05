package com.yourname.expensetracker.ui.shop.supplier

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.yourname.expensetracker.data.local.entity.SupplierPayment
import com.yourname.expensetracker.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(
    supplierId: Long,
    onBack: () -> Unit,
    viewModel: SupplierViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.detailState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<SupplierPayment?>(null) }

    LaunchedEffect(supplierId) { viewModel.loadSupplierDetail(supplierId) }
    DisposableEffect(Unit) { onDispose { viewModel.clearDetail() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.supplier?.name ?: "Supplier") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val name = state.supplier?.name ?: "Supplier"
                        val balance = state.payableBalance
                        val shareText = "Account Statement for $name\n" +
                                "Total Purchases / Bills: ${CurrencyHelper.format(state.totalBills, "₹")}\n" +
                                "Total Payments: ${CurrencyHelper.format(state.totalPaid, "₹")}\n" +
                                "Outstanding Balance: ${CurrencyHelper.format(balance, "₹")}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Supplier Statement"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Statement")
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
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.payableBalance > 0)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (state.payableBalance > 0) "Outstanding Payable" else "All Settled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyHelper.format(state.payableBalance, "₹"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.payableBalance > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Bills: ${CurrencyHelper.format(state.totalBills, "₹")}", style = MaterialTheme.typography.bodySmall)
                            Text("Total Paid: ${CurrencyHelper.format(state.totalPaid, "₹")}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Two Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showBillDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("+ Add Bill / Credit")
                    }
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Pay / Settle")
                    }
                }
            }

            item {
                Text("Transaction History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            if (state.payments.isEmpty()) {
                item {
                    Text(
                        "No transactions recorded yet. Use + Add Bill or Pay / Settle above.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            items(state.payments, key = { it.id }) { payment ->
                val isBill = payment.note?.startsWith("[BILL]") == true
                val cleanNote = payment.note
                    ?.removePrefix("[BILL]")
                    ?.removePrefix("[PAYMENT]")
                    ?.trim()
                    ?.ifBlank { if (isBill) "Purchase on Credit" else "Settlement Payment" }
                    ?: if (isBill) "Purchase on Credit" else "Settlement Payment"

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isBill) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = if (isBill) "BILL / CREDIT" else "PAID",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBill) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cleanNote, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(payment.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            CurrencyHelper.format(payment.amount, "₹"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isBill) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                        IconButton(onClick = { paymentToDelete = payment }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }

    if (showBillDialog) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBillDialog = false },
            title = { Text("Add Purchase / Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Bill Amount") },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Invoice # or Items note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.addBill(supplierId, amt, note.ifBlank { null })
                        showBillDialog = false
                    }
                }) { Text("Save Bill") }
            },
            dismissButton = {
                TextButton(onClick = { showBillDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPaymentDialog) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Pay / Settle Supplier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Payment Amount") },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Payment method / Note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        viewModel.addPayment(supplierId, amt, note.ifBlank { null })
                        showPaymentDialog = false
                    }
                }) { Text("Record Payment") }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (paymentToDelete != null) {
        val payment = paymentToDelete!!
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to delete this ${if (payment.note?.startsWith("[BILL]") == true) "bill" else "payment"} of ${CurrencyHelper.format(payment.amount, "₹")}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePayment(payment)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
