package com.yourname.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.expensetracker.ui.onboarding.OnboardingScreen
import com.yourname.expensetracker.ui.personal.addtransaction.AddTransactionScreen
import com.yourname.expensetracker.ui.personal.budgets.AddEditBudgetScreen
import com.yourname.expensetracker.ui.personal.budgets.BudgetsScreen
import com.yourname.expensetracker.ui.personal.categories.CategoryManagementScreen
import com.yourname.expensetracker.ui.personal.recurringtemplates.RecurringTemplatesScreen
import com.yourname.expensetracker.ui.personal.savingsgoals.SavingsGoalsScreen
import com.yourname.expensetracker.ui.personal.transactions.TransactionListScreen
import com.yourname.expensetracker.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object AppLock : Screen("app_lock")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object AddTransaction : Screen("add_transaction")
    data object TransactionList : Screen("transaction_list")
    data object Categories : Screen("categories")
    data object Budgets : Screen("budgets")
    data object AddEditBudget : Screen("add_edit_budget")
    data object SavingsGoals : Screen("savings_goals")
    data object RecurringTemplates : Screen("recurring_templates")
    data object Insights : Screen("insights")
    data object ReceiptOcr : Screen("receipt_ocr")

    // Shop mode routes
    data object CustomerDetail : Screen("shop/customer/{customerId}")
    data object Suppliers : Screen("shop/suppliers")
    data object SupplierDetail : Screen("shop/supplier/{supplierId}")
    data object Staff : Screen("shop/staff")
    data object PL : Screen("shop/pl")
    data object Printer : Screen("shop/printer")
    data object Ledger : Screen("shop/ledger")
    data object Billing : Screen("shop/billing")
    data object Inventory : Screen("shop/inventory")
    data object GstReport : Screen("shop/gst_report")
    data object DailyClosing : Screen("shop/daily_closing")
    data object Branches : Screen("shop/branches")

    // Personal mode routes
    data object BillReminders : Screen("personal/bill_reminders")
    data object NetWorth : Screen("personal/net_worth")
    data object Subscriptions : Screen("personal/subscriptions")
    data object SmsImport : Screen("personal/sms_import")
    data object ExpenseSplit : Screen("personal/expense_split")

    // Shared / Settings
    data object NotificationCenter : Screen("notifications")
    data object LanguageSettings : Screen("settings/language")
    data object BackupRestore : Screen("settings/backup")
    data object SecuritySettings : Screen("settings/security")
    data object CurrencySettings : Screen("settings/currency")
    data object Paywall : Screen("settings/paywall")
    data object PrivacyPolicy : Screen("settings/privacy")
}

@Composable
fun ExpenseTrackerNavHost(
    sessionManager: com.yourname.expensetracker.data.local.SessionManager,
    backupRepository: com.yourname.expensetracker.data.repository.BackupRepository,
    navController: NavHostController = rememberNavController()
) {
    val pinHash by sessionManager.pinHash.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onFinished = { isOnboarded ->
                if (!pinHash.isNullOrBlank()) {
                    navController.navigate(Screen.AppLock.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else if (isOnboarded) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            })
        }

        composable(Screen.AppLock.route) {
            com.yourname.expensetracker.ui.security.AppLockScreen(
                sessionManager = sessionManager,
                onUnlocked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.AppLock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            com.yourname.expensetracker.ui.home.HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onDone = { navController.popBackStack() },
                onScanReceipt = { navController.navigate(Screen.ReceiptOcr.route) }
            )
        }

        composable(Screen.TransactionList.route) {
            TransactionListScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Categories.route) {
            CategoryManagementScreen()
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen(
                onAddBudget = { navController.navigate(Screen.AddEditBudget.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddEditBudget.route) {
            AddEditBudgetScreen(onDone = { navController.popBackStack() })
        }

        composable(Screen.SavingsGoals.route) {
            SavingsGoalsScreen(
                onBack = { navController.popBackStack() },
                onAddGoal = { }
            )
        }

        composable(Screen.RecurringTemplates.route) {
            RecurringTemplatesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) {
            val customerId = it.arguments?.getLong("customerId") ?: 0L
            com.yourname.expensetracker.ui.shop.khata.CustomerDetailScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Suppliers.route) {
            com.yourname.expensetracker.ui.shop.supplier.SupplierScreen(
                onOpenSupplier = { id ->
                    navController.navigate("shop/supplier/$id")
                }
            )
        }

        composable(
            route = Screen.SupplierDetail.route,
            arguments = listOf(navArgument("supplierId") { type = NavType.LongType })
        ) {
            val supplierId = it.arguments?.getLong("supplierId") ?: 0L
            com.yourname.expensetracker.ui.shop.supplier.SupplierDetailScreen(
                supplierId = supplierId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Staff.route) {
            com.yourname.expensetracker.ui.shop.staff.StaffManagementScreen()
        }

        composable(Screen.PL.route) {
            com.yourname.expensetracker.ui.shop.pl.PLScreen()
        }

        composable(Screen.Printer.route) {
            com.yourname.expensetracker.ui.shop.printer.PrinterSetupScreen(onBack = {
                navController.popBackStack()
            })
        }

        composable(Screen.Ledger.route) {
            com.yourname.expensetracker.ui.shop.ledger.CashLedgerScreen()
        }

        composable(Screen.Insights.route) {
            com.yourname.expensetracker.ui.insights.InsightsScreen()
        }

        composable(Screen.BackupRestore.route) {
            com.yourname.expensetracker.ui.settings.BackupRestoreScreen(
                backupRepository = backupRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SecuritySettings.route) {
            com.yourname.expensetracker.ui.settings.SecuritySettingsScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CurrencySettings.route) {
            com.yourname.expensetracker.ui.settings.CurrencyThemeSettingsScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Paywall.route) {
            com.yourname.expensetracker.ui.settings.PaywallScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            com.yourname.expensetracker.ui.settings.PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReceiptOcr.route) {
            com.yourname.expensetracker.ui.personal.ocr.ReceiptOcrScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Shop mode destinations
        composable(Screen.Billing.route) {
            com.yourname.expensetracker.ui.shop.billing.BillingScreen(
                onBack = { navController.popBackStack() },
                onOpenPrinter = { navController.navigate(Screen.Printer.route) }
            )
        }

        composable(Screen.Inventory.route) {
            com.yourname.expensetracker.ui.shop.inventory.InventoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GstReport.route) {
            com.yourname.expensetracker.ui.shop.reports.GstReportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DailyClosing.route) {
            com.yourname.expensetracker.ui.shop.closing.DailyClosingScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Branches.route) {
            com.yourname.expensetracker.ui.shop.branch.BranchManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Personal mode destinations
        composable(Screen.BillReminders.route) {
            com.yourname.expensetracker.ui.personal.bills.BillRemindersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NetWorth.route) {
            com.yourname.expensetracker.ui.personal.networth.NetWorthScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Subscriptions.route) {
            com.yourname.expensetracker.ui.personal.subscriptions.SubscriptionsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SmsImport.route) {
            com.yourname.expensetracker.ui.personal.sms.SmsImportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExpenseSplit.route) {
            com.yourname.expensetracker.ui.personal.split.ExpenseSplitScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Common destinations
        composable(Screen.NotificationCenter.route) {
            com.yourname.expensetracker.ui.common.NotificationCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LanguageSettings.route) {
            com.yourname.expensetracker.ui.settings.LanguageSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
