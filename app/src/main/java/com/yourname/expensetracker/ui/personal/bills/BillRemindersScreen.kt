package com.yourname.expensetracker.ui.personal.bills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.BillReminder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillRemindersScreen(
    onBack: () -> Unit,
    viewModel: BillRemindersViewModel = hiltViewModel()
) {
    val bills by viewModel.bills.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingBills = bills.filter { !it.isPaid }
    val paidBills = bills.filter { it.isPaid }
    val totalPendingAmount = pendingBills.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Total Pending Due Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Due Upcoming", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", totalPendingAmount)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${pendingBills.size} unpaid bills pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (pendingBills.isNotEmpty()) {
                item {
                    Text("Pending Bills", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(pendingBills, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        currencySymbol = currencySymbol,
                        onTogglePaid = { viewModel.togglePaid(bill) },
                        onDelete = { viewModel.deleteBill(bill) }
                    )
                }
            }

            if (paidBills.isNotEmpty()) {
                item {
                    Text("Paid Bills", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(paidBills, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        currencySymbol = currencySymbol,
                        onTogglePaid = { viewModel.togglePaid(bill) },
                        onDelete = { viewModel.deleteBill(bill) }
                    )
                }
            }

            if (bills.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No bill reminders set up yet.\nTap + to add your rent, electricity, credit card or internet bill.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBillDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, daysAhead, isRecurring, category ->
                val dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysAhead)
                viewModel.addBill(title, amount, dueDate, isRecurring, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun BillCard(
    bill: BillReminder,
    currencySymbol: String,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val diffDays = ((bill.dueDate - now) / (1000 * 60 * 60 * 24)).toInt()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val statusText = when {
        bill.isPaid -> "Paid"
        diffDays < 0 -> "Overdue by ${-diffDays} day(s)!"
        diffDays == 0 -> "Due today!"
        diffDays == 1 -> "Due tomorrow"
        else -> "Due in $diffDays days"
    }

    val statusColor = when {
        bill.isPaid -> MaterialTheme.colorScheme.primary
        diffDays <= 1 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else if (diffDays < 0)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = bill.isPaid,
                onCheckedChange = { onTogglePaid() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(bill.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${dateFormat.format(Date(bill.dueDate))} • ${bill.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(containerColor = statusColor.copy(alpha = 0.15f)) {
                    Text(statusText, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", bill.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddBillDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, daysAhead: Long, isRecurring: Boolean, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("7") }
    var isRecurring by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf("Utilities") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Name (e.g. Electricity, Wifi)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Due in (days from now)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Text("Repeats monthly")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val days = daysText.toLongOrNull() ?: 7L
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, days, isRecurring, category)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
