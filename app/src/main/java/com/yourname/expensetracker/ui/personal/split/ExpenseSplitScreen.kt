package com.yourname.expensetracker.ui.personal.split

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.ExpenseSplit
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSplitScreen(
    onBack: () -> Unit,
    viewModel: ExpenseSplitViewModel = hiltViewModel()
) {
    val splits by viewModel.splits.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val activeSplits = splits.filter { !it.isSettled }
    val settledSplits = splits.filter { it.isSettled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared & Family Splits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Split")
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
            if (activeSplits.isNotEmpty()) {
                item {
                    Text("Active Splits", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(activeSplits, key = { it.id }) { split ->
                    SplitCard(
                        split = split,
                        currencySymbol = currencySymbol,
                        onToggleSettled = { viewModel.toggleSettled(split) },
                        onDelete = { viewModel.deleteSplit(split) },
                        onShare = {
                            val names = split.participants.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val count = (names.size).coerceAtLeast(1)
                            val perPerson = split.totalAmount / count
                            val shareMsg = "Hey! For ${split.title} (Total: $currencySymbol ${String.format(Locale.getDefault(), "%.2f", split.totalAmount)}), each person's share is $currencySymbol ${String.format(Locale.getDefault(), "%.2f", perPerson)}. Paid by ${split.paidBy}. Please settle when convenient!"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMsg)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Split Reminder"))
                        }
                    )
                }
            }

            if (settledSplits.isNotEmpty()) {
                item {
                    Text("Settled Splits", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(settledSplits, key = { it.id }) { split ->
                    SplitCard(
                        split = split,
                        currencySymbol = currencySymbol,
                        onToggleSettled = { viewModel.toggleSettled(split) },
                        onDelete = { viewModel.deleteSplit(split) },
                        onShare = {}
                    )
                }
            }

            if (splits.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No group or family expense splits yet.\nTap + to split dinner, trip, or household bills with friends.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var paidBy by remember { mutableStateOf("You") }
        var participants by remember { mutableStateOf("You, Alice, Bob") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Split New Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title (e.g. Goa Dinner)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Total Amount ($currencySymbol)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paidBy,
                        onValueChange = { paidBy = it },
                        label = { Text("Paid By") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it },
                        label = { Text("Participants (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0) {
                            viewModel.addSplit(title, amount, paidBy, participants)
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Split")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SplitCard(
    split: ExpenseSplit,
    currencySymbol: String,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val names = split.participants.split(",").map { it.trim() }.filter { it.isNotBlank() }
    val count = (names.size).coerceAtLeast(1)
    val perPerson = split.totalAmount / count
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (split.isSettled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(split.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        "${dateFormat.format(Date(split.date))} • Paid by ${split.paidBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", split.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Split among $count people: ${split.participants}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Per person share: $currencySymbol ${String.format(Locale.getDefault(), "%.2f", perPerson)}",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleSettled,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (split.isSettled) {
                        Text("Reopen")
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Settled")
                    }
                }

                Row {
                    if (!split.isSettled) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Share on WhatsApp", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
