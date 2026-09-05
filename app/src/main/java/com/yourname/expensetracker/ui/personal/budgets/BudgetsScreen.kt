package com.yourname.expensetracker.ui.personal.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onAddBudget: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Budgets") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudget) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onBack == null) {
                item {
                    Text(
                        text = "Budgets",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            if (budgets.isEmpty()) {
                item {
                    Text(
                        text = "No budgets yet. Tap + to create one.",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
            items(budgets, key = { it.budget.id }) { item ->
                BudgetCard(item = item, onDelete = { viewModel.deleteBudget(item.budget) })
            }
        }
    }
}

@Composable
private fun BudgetCard(item: BudgetWithProgress, onDelete: () -> Unit) {
    val color = when {
        item.progress >= 1f -> MaterialTheme.colorScheme.error
        item.progress >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category?.name ?: "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹ ${formatAmount(item.spent)} of ₹ ${formatAmount(item.budget.amount)} (${item.budget.period.name})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%.2f", amount)
}
