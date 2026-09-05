package com.yourname.expensetracker.ui.personal.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: PersonalDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${state.profile?.name ?: "Wallet"}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personal Spending & Budgets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onNavigate(Screen.NotificationCenter.route) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                        }
                        AssistChip(
                            onClick = { onNavigate(Screen.TransactionList.route) },
                            label = { Text("History") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                        )
                    }
                }
            }

            // Net Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${state.currencySymbol} ${String.format(Locale.getDefault(), "%,.2f", state.totalBalance)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (state.accounts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.accounts) { acc ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "${acc.name}: ${state.currencySymbol}${String.format(Locale.getDefault(), "%.0f", acc.startingBalance)}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Monthly In / Out Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonthlyStatPill(
                        label = "This Month Income",
                        amount = state.monthIncome,
                        symbol = state.currencySymbol,
                        icon = Icons.Default.ArrowDownward,
                        iconColor = Color(0xFF2E7D32),
                        containerColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                    MonthlyStatPill(
                        label = "This Month Spent",
                        amount = state.monthExpense,
                        symbol = state.currencySymbol,
                        icon = Icons.Default.ArrowUpward,
                        iconColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Expense",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.AddTransaction.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Savings,
                        label = "Budgets",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Budgets.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Flag,
                        label = "Goals",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.SavingsGoals.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Repeat,
                        label = "Recurring",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.RecurringTemplates.route) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Schedule,
                        label = "Bills",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.BillReminders.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.AccountBalance,
                        label = "Net Worth",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.NetWorth.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Sms,
                        label = "SMS Import",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.SmsImport.route) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.CallSplit,
                        label = "Split",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.ExpenseSplit.route) }
                    )
                }
            }

            // Budgets Snapshot
            if (state.topBudgets.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Budgets Snapshot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { onNavigate(Screen.Budgets.route) }) {
                            Text("See All")
                        }
                    }
                }

                items(state.topBudgets, key = { it.budget.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.category?.name ?: "Category Budget",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${state.currencySymbol}${String.format(Locale.getDefault(), "%.0f", item.spent)} / ${state.currencySymbol}${String.format(Locale.getDefault(), "%.0f", item.budget.amount)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progressColor = when {
                                item.progress >= 1f -> MaterialTheme.colorScheme.error
                                item.progress >= 0.8f -> Color(0xFFFFA000)
                                else -> Color(0xFF2E7D32)
                            }
                            LinearProgressIndicator(
                                progress = { item.progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = progressColor
                            )
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { onNavigate(Screen.TransactionList.route) }) {
                        Text("View All")
                    }
                }
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No transactions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(onClick = { onNavigate(Screen.AddTransaction.route) }) {
                                Text("Add Your First Entry")
                            }
                        }
                    }
                }
            } else {
                items(state.recentTransactions, key = { it.transaction.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.transaction.type == TransactionType.INCOME || item.transaction.type == TransactionType.CASH_IN)
                                            Color(0xFFE8F5E9)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (item.transaction.type == TransactionType.INCOME || item.transaction.type == TransactionType.CASH_IN)
                                        Icons.Default.ArrowDownward
                                    else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (item.transaction.type == TransactionType.INCOME || item.transaction.type == TransactionType.CASH_IN)
                                        Color(0xFF2E7D32)
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.transaction.note ?: item.category?.name ?: "Transaction",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(item.transaction.date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val isPositive = item.transaction.type == TransactionType.INCOME || item.transaction.type == TransactionType.CASH_IN
                            Text(
                                text = "${if (isPositive) "+" else "-"} ${state.currencySymbol}${String.format(Locale.getDefault(), "%.2f", item.transaction.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                viewModel.deleteTransaction(item.transaction.id) { undoAction ->
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Transaction deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            undoAction()
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatPill(
    label: String,
    amount: Double,
    symbol: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$symbol${String.format(Locale.getDefault(), "%,.0f", amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}
