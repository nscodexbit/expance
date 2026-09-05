package com.yourname.expensetracker.ui.personal.networth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
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
fun NetWorthScreen(
    onBack: () -> Unit,
    viewModel: NetWorthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Combined Net Worth") },
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
            // Hero Net Worth Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.netWorth >= 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text("Total Net Worth", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", state.netWorth)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.netWorth >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Assets", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", state.totalAssets)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Liabilities", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", state.totalLiabilities)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Assets Section
            item {
                Text("Your Assets", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(state.accounts.filter { it.currentBalance >= 0 }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.account.name, fontWeight = FontWeight.SemiBold)
                                Text(item.account.type.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", item.currentBalance)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.savingsGoals.isNotEmpty()) {
                items(state.savingsGoals) { goal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(goal.name, fontWeight = FontWeight.SemiBold)
                                    Text("Savings Goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", goal.currentAmount)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Liabilities Section
            val liabilitiesAccounts = state.accounts.filter { it.currentBalance < 0 }
            if (liabilitiesAccounts.isNotEmpty()) {
                item {
                    Text("Your Liabilities", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(liabilitiesAccounts) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.account.name, fontWeight = FontWeight.SemiBold)
                                Text("Outstanding Debt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            Text(
                                "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", -item.currentBalance)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
