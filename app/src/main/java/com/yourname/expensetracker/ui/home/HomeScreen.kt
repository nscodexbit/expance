package com.yourname.expensetracker.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.navigation.Screen

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activeProfile by viewModel.activeProfile.collectAsState()

    val navItems = listOf(
        BottomNavItem("Overview", Icons.Filled.Dashboard),
        BottomNavItem("Transactions", Icons.Filled.ReceiptLong),
        BottomNavItem("Budgets", Icons.Filled.PieChart),
        BottomNavItem("Insights", Icons.Filled.TrendingUp),
        BottomNavItem("Settings", Icons.Filled.Tune)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedIndex == 0 || selectedIndex == 1) {
                FloatingActionButton(
                    onClick = { onNavigate(Screen.AddTransaction.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { padding ->
        when (selectedIndex) {
            0 -> PersonalDashboard(onNavigate)
            1 -> Box(modifier = Modifier.padding(padding)) {
                com.yourname.expensetracker.ui.personal.transactions.TransactionListScreen(
                    onNavigateBack = null
                )
            }
            2 -> Box(modifier = Modifier.padding(padding)) {
                com.yourname.expensetracker.ui.personal.budgets.BudgetsScreen(
                    onAddBudget = { onNavigate(Screen.AddEditBudget.route) },
                    onBack = { selectedIndex = 0 }
                )
            }
            3 -> Box(modifier = Modifier.padding(padding)) {
                com.yourname.expensetracker.ui.insights.InsightsScreen()
            }
            else -> Box(modifier = Modifier.padding(padding)) {
                CleanSettingsScreen(
                    profileName = activeProfile?.name ?: "Personal Account",
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun PersonalDashboard(onNavigate: (String) -> Unit) {
    com.yourname.expensetracker.ui.personal.dashboard.PersonalDashboardScreen(onNavigate = onNavigate)
}

@Composable
private fun CleanSettingsScreen(
    profileName: String,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User Profile Header
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Personal Financial Workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: Financial Tools
        SettingsSection(title = "Financial Tools") {
            SettingsActionTile(
                icon = Icons.Default.Savings,
                title = "Savings Goals",
                subtitle = "Track targeted savings & milestone progress",
                onClick = { onNavigate(Screen.SavingsGoals.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Event,
                title = "Bill Reminders",
                subtitle = "Upcoming utility bills & due date alerts",
                onClick = { onNavigate(Screen.BillReminders.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.AccountBalance,
                title = "Net Worth",
                subtitle = "Total assets, liabilities & wealth growth",
                onClick = { onNavigate(Screen.NetWorth.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Autorenew,
                title = "Subscriptions",
                subtitle = "Recurring digital services & memberships",
                onClick = { onNavigate(Screen.Subscriptions.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.CallSplit,
                title = "Expense Split",
                subtitle = "Share bills with friends & roommates",
                onClick = { onNavigate(Screen.ExpenseSplit.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Sms,
                title = "SMS Auto-Import",
                subtitle = "Extract bank & UPI transaction alerts",
                onClick = { onNavigate(Screen.SmsImport.route) }
            )
        }

        // Section: Customization & Security
        SettingsSection(title = "Preferences & Security") {
            SettingsActionTile(
                icon = Icons.Default.Category,
                title = "Categories",
                subtitle = "Manage expense & income tags",
                onClick = { onNavigate(Screen.Categories.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Palette,
                title = "Currency & Appearance",
                subtitle = "Currency symbol and light/dark theme",
                onClick = { onNavigate(Screen.CurrencySettings.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Lock,
                title = "Security & PIN Lock",
                subtitle = "Protect sensitive accounts with PIN",
                onClick = { onNavigate(Screen.SecuritySettings.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Language,
                title = "Language / भाषा",
                subtitle = "Choose preferred application language",
                onClick = { onNavigate(Screen.LanguageSettings.route) }
            )
        }

        // Section: System & About
        SettingsSection(title = "System & Support") {
            SettingsActionTile(
                icon = Icons.Default.CloudSync,
                title = "Cloud Backup & Restore",
                subtitle = "Export encrypted database & import backups",
                onClick = { onNavigate(Screen.BackupRestore.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Notifications,
                title = "Notification Center",
                subtitle = "Review system and balance alerts",
                onClick = { onNavigate(Screen.NotificationCenter.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Star,
                title = "Expense Tracker Pro",
                subtitle = "Unlock unlimited features and tools",
                onClick = { onNavigate(Screen.Paywall.route) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            SettingsActionTile(
                icon = Icons.Default.Shield,
                title = "Privacy Policy",
                subtitle = "100% offline & local data guarantee",
                onClick = { onNavigate(Screen.PrivacyPolicy.route) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)
