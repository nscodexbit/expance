package com.yourname.expensetracker.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activeProfile by viewModel.activeProfile.collectAsState()

    if (activeProfile == null) {
        // Show loading while profile resolves
        Box(modifier = Modifier.fillMaxSize()) {}
        return
    }

    val type = activeProfile!!.type
    if (type == ProfileType.SHOP) {
        ShopHomeScaffold(onNavigate = onNavigate)
    } else {
        PersonalHomeScaffold(onNavigate = onNavigate)
    }
}

@Composable
private fun PersonalHomeScaffold(onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home),
        BottomNavItem("History", Icons.Filled.History),
        BottomNavItem("Budgets", Icons.Filled.Savings),
        BottomNavItem("Insights", Icons.Filled.Insights),
        BottomNavItem("More", Icons.Filled.MoreVert)
    )

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedIndex == 0 || selectedIndex == 1) {
                FloatingActionButton(onClick = { onNavigate(Screen.AddTransaction.route) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { padding ->
        when (selectedIndex) {
            0 -> PersonalDashboard(onNavigate)
            1 -> PersonalHistory(padding)
            2 -> PersonalBudgets(onNavigate, padding)
            3 -> PersonalInsights(padding)
            else -> PersonalMore(onNavigate)
        }
    }
}

@Composable
private fun ShopHomeScaffold(onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home),
        BottomNavItem("Ledger", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Khata", Icons.Filled.Book),
        BottomNavItem("Insights", Icons.Filled.Insights),
        BottomNavItem("More", Icons.Filled.MoreVert)
    )

    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedIndex <= 1) {
                FloatingActionButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        when (selectedIndex) {
            0 -> ShopDashboard(onNavigate, padding)
            1 -> ShopLedger(padding)
            2 -> ShopKhata(onNavigate, padding)
            3 -> ShopInsights(padding)
            else -> ShopMore(onNavigate)
        }
    }
}

@Composable
private fun PersonalDashboard(onNavigate: (String) -> Unit) {
    com.yourname.expensetracker.ui.personal.dashboard.PersonalDashboardScreen(onNavigate = onNavigate)
}

@Composable
private fun PersonalHistory(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.personal.transactions.TransactionListScreen()
    }
}

@Composable
private fun PersonalBudgets(onNavigate: (String) -> Unit, padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.personal.budgets.BudgetsScreen(
            onAddBudget = { onNavigate(Screen.AddEditBudget.route) }
        )
    }
}

@Composable
private fun PersonalInsights(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.insights.InsightsScreen()
    }
}

@Composable
private fun PersonalMore(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text("More Features", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        SettingsRow("📅 Bill Reminders") { onNavigate(Screen.BillReminders.route) }
        SettingsRow("💎 Net Worth View") { onNavigate(Screen.NetWorth.route) }
        SettingsRow("🔄 Subscriptions & Recurring") { onNavigate(Screen.Subscriptions.route) }
        SettingsRow("📩 Bank/UPI SMS Auto-Import") { onNavigate(Screen.SmsImport.route) }
        SettingsRow("👥 Shared & Family Splits") { onNavigate(Screen.ExpenseSplit.route) }
        SettingsRow("🔔 Notification Center") { onNavigate(Screen.NotificationCenter.route) }
        SettingsRow("🏷️ Categories") { onNavigate(Screen.Categories.route) }
        SettingsRow("🎯 Savings Goals") { onNavigate(Screen.SavingsGoals.route) }
        SettingsRow("🌐 Language / भाषा") { onNavigate(Screen.LanguageSettings.route) }
        SettingsRow("💱 Currency & Appearance") { onNavigate(Screen.CurrencySettings.route) }
        SettingsRow("☁️ Cloud Backup & Restore") { onNavigate(Screen.BackupRestore.route) }
        SettingsRow("🔒 Security & App Lock") { onNavigate(Screen.SecuritySettings.route) }
        SettingsRow("⭐ Expense Tracker Pro") { onNavigate(Screen.Paywall.route) }
        SettingsRow("📄 Privacy Policy") { onNavigate(Screen.PrivacyPolicy.route) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PlaceholderCard(text: String, padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(text = text, fontSize = 20.sp)
    }
}

@Composable
private fun ShopDashboard(onNavigate: (String) -> Unit, padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.shop.dashboard.ShopDashboardScreen(onNavigate = onNavigate)
    }
}

@Composable
private fun ShopLedger(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.shop.ledger.CashLedgerScreen()
    }
}

@Composable
private fun ShopKhata(onNavigate: (String) -> Unit, padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.shop.khata.KhataScreen(
            onOpenCustomer = { id -> onNavigate("shop/customer/$id") }
        )
    }
}

@Composable
private fun ShopInsights(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.padding(padding)) {
        com.yourname.expensetracker.ui.insights.InsightsScreen()
    }
}

@Composable
private fun ShopMore(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text("Shop Operations", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        SettingsRow("🧾 Billing & Invoicing (POS)") { onNavigate(Screen.Billing.route) }
        SettingsRow("📦 Inventory & Stock Alerts") { onNavigate(Screen.Inventory.route) }
        SettingsRow("📊 GST / Tax Reports") { onNavigate(Screen.GstReport.route) }
        SettingsRow("🔒 Daily Closing Summary") { onNavigate(Screen.DailyClosing.route) }
        SettingsRow("🏬 Branches & Staff Roles") { onNavigate(Screen.Branches.route) }
        SettingsRow("🔔 Notification Center") { onNavigate(Screen.NotificationCenter.route) }
        SettingsRow("📖 Cash Ledger") { onNavigate("shop/ledger") }
        SettingsRow("🚚 Suppliers") { onNavigate(Screen.Suppliers.route) }
        SettingsRow("📈 Profit & Loss (P&L)") { onNavigate(Screen.PL.route) }
        SettingsRow("👥 Staff Advances & Salary") { onNavigate(Screen.Staff.route) }
        SettingsRow("🖨️ Bluetooth Thermal Printer") { onNavigate(Screen.Printer.route) }
        SettingsRow("🌐 Language / भाषा") { onNavigate(Screen.LanguageSettings.route) }
        SettingsRow("💱 Currency & Appearance") { onNavigate(Screen.CurrencySettings.route) }
        SettingsRow("☁️ Cloud Backup & Restore") { onNavigate(Screen.BackupRestore.route) }
        SettingsRow("🔒 Security & App Lock") { onNavigate(Screen.SecuritySettings.route) }
        SettingsRow("⭐ Expense Tracker Pro") { onNavigate(Screen.Paywall.route) }
        SettingsRow("📄 Privacy Policy") { onNavigate(Screen.PrivacyPolicy.route) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)
