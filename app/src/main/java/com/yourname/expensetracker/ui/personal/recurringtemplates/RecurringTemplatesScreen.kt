package com.yourname.expensetracker.ui.personal.recurringtemplates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.Frequency
import java.util.Locale

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTemplatesScreen(
    viewModel: RecurringTemplatesViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Template")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Recurring Templates", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (templates.isEmpty()) {
                item {
                    Text(
                        text = "No recurring templates yet. Tap + to add one (e.g. Netflix, Rent, Wifi).",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(templates, key = { it.template.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.template.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹ ${String.format(Locale.getDefault(), "%.2f", item.template.amount)} • ${item.template.frequency.name} • ${item.category?.name ?: "Category"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.deleteTemplate(item.template) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTemplateDialog(
            categories = categories,
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onConfirm = { label, amount, freq, accId, catId ->
                viewModel.addTemplate(label, amount, freq, accId, catId)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTemplateDialog(
    categories: List<com.yourname.expensetracker.data.local.entity.Category>,
    accounts: List<com.yourname.expensetracker.data.local.entity.Account>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Frequency, Long, Long) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Recurring Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. Rent, Netflix)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Frequency", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Frequency.values().forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (label.isNotBlank() && amt > 0) {
                        onConfirm(label, amt, frequency, selectedAccountId, selectedCategoryId)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
