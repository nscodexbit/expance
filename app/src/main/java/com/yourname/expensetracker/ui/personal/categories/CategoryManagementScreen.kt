package com.yourname.expensetracker.ui.personal.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.TransactionKind

@Composable
fun CategoryManagementScreen(
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newKind by remember { mutableStateOf(TransactionKind.EXPENSE) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
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
                Text("Expense Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(categories.filter { it.kind == TransactionKind.EXPENSE }) { category ->
                CategoryRow(category, onDelete = { viewModel.deleteCategory(category) })
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Income Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(categories.filter { it.kind == TransactionKind.INCOME }) { category ->
                CategoryRow(category, onDelete = { viewModel.deleteCategory(category) })
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onKindChange = { newKind = it },
            selectedKind = newKind,
            onConfirm = { name ->
                viewModel.addCategory(name, newKind)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoryRow(category: Category, onDelete: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(10.dp),
                        color = parseColor(category.colorHex)
                    ) {}
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (category.isCustom) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onKindChange: (TransactionKind) -> Unit,
    selectedKind: TransactionKind
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedKind == TransactionKind.EXPENSE,
                        onClick = { onKindChange(TransactionKind.EXPENSE) },
                        label = { Text("Expense") }
                    )
                    FilterChip(
                        selected = selectedKind == TransactionKind.INCOME,
                        onClick = { onKindChange(TransactionKind.INCOME) },
                        label = { Text("Income") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
