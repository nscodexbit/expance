package com.yourname.expensetracker.ui.home

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.data.local.export.CsvExporter
import com.yourname.expensetracker.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val isShopMode = activeProfile?.type == ProfileType.SHOP

    val navItems = if (isShopMode) {
        listOf(
            BottomNavItem("Shop", Icons.Filled.Store),
            BottomNavItem("Billing", Icons.Filled.ReceiptLong),
            BottomNavItem("Khata", Icons.Filled.Group),
            BottomNavItem("Suppliers", Icons.Filled.LocalShipping),
            BottomNavItem("Settings", Icons.Filled.Tune)
        )
    } else {
        listOf(
            BottomNavItem("Overview", Icons.Filled.Dashboard),
            BottomNavItem("Transactions", Icons.Filled.ReceiptLong),
            BottomNavItem("Budgets", Icons.Filled.PieChart),
            BottomNavItem("Insights", Icons.Filled.TrendingUp),
            BottomNavItem("Settings", Icons.Filled.Tune)
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    // Reset selectedIndex if mode switch changes tabs count
    LaunchedEffect(isShopMode) {
        if (selectedIndex >= navItems.size) {
            selectedIndex = 0
        }
    }

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
            if (!isShopMode && (selectedIndex == 0 || selectedIndex == 1)) {
                FloatingActionButton(
                    onClick = { onNavigate(Screen.AddTransaction.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
                }
            } else if (isShopMode && selectedIndex == 0) {
                FloatingActionButton(
                    onClick = { selectedIndex = 1 }, // Jump directly to POS Billing
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = "New POS Bill", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { padding ->
        if (isShopMode) {
            when (selectedIndex) {
                0 -> com.yourname.expensetracker.ui.shop.dashboard.ShopDashboardScreen(
                    onNavigate = onNavigate,
                    onOpenBilling = { selectedIndex = 1 },
                    onOpenScanner = { onNavigate(Screen.BarcodeScanner.route) },
                    onOpenKhata = { selectedIndex = 2 },
                    onOpenSuppliers = { selectedIndex = 3 }
                )
                1 -> Box(modifier = Modifier.padding(padding)) {
                    com.yourname.expensetracker.ui.shop.billing.BillingScreen(
                        onNavigateToScanner = { onNavigate(Screen.BarcodeScanner.route) },
                        onBack = { selectedIndex = 0 }
                    )
                }
                2 -> Box(modifier = Modifier.padding(padding)) {
                    com.yourname.expensetracker.ui.shop.khata.KhataScreen(
                        onOpenCustomer = { customerId ->
                            onNavigate("shop/customer/$customerId")
                        }
                    )
                }
                3 -> Box(modifier = Modifier.padding(padding)) {
                    com.yourname.expensetracker.ui.shop.supplier.SupplierScreen(
                        onOpenSupplier = { supplierId ->
                            onNavigate("shop/supplier/$supplierId")
                        },
                        onBack = { selectedIndex = 0 }
                    )
                }
                else -> Box(modifier = Modifier.padding(padding)) {
                    CleanSettingsScreen(
                        profileName = activeProfile?.name ?: "Shop Profile",
                        isShopMode = true,
                        onSwitchMode = {
                            viewModel.switchProfileMode(ProfileType.PERSONAL)
                        },
                        onNavigate = onNavigate
                    )
                }
            }
        } else {
            // Personal Mode
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
                        isShopMode = false,
                        onSwitchMode = {
                            viewModel.switchProfileMode(ProfileType.SHOP)
                        },
                        onNavigate = onNavigate
                    )
                }
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
    isShopMode: Boolean,
    onSwitchMode: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

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
                            if (isShopMode) Icons.Default.Store else Icons.Default.AccountCircle,
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
                        text = if (isShopMode) "Shop & Retail Workspace" else "Personal Financial Workspace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mode Switcher Card (Goal 6: Personal ⇄ Shop Mode Switch)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isShopMode) "Shop Mode Active" else "Personal Mode Active",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isShopMode) "Switch to Personal Wallet & Expenses" else "Switch to Shop, POS & Khata Ledgers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onSwitchMode,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isShopMode) "To Personal" else "To Shop", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: Mode Specific Tools
        if (isShopMode) {
            SettingsSection(title = "Shop Management") {
                SettingsActionTile(
                    icon = Icons.Default.Store,
                    title = "Shop & Invoice Branding",
                    subtitle = "Store name, GSTIN, header, address & contact",
                    onClick = { onNavigate(Screen.ShopBranding.route) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                SettingsActionTile(
                    icon = Icons.Default.QrCodeScanner,
                    title = "Barcode Scanner",
                    subtitle = "Quick scan to look up or add products",
                    onClick = { onNavigate(Screen.BarcodeScanner.route) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                SettingsActionTile(
                    icon = Icons.Default.LocalShipping,
                    title = "Suppliers / Vendor Ledger",
                    subtitle = "Track supplier purchases and payments",
                    onClick = { onNavigate(Screen.Suppliers.route) }
                )
            }
        } else {
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
                subtitle = "Biometric fingerprint & 4-digit PIN protection",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
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
            modifier = Modifier.size(16.dp)
        )
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)
