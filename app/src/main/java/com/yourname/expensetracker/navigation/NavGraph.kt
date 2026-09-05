package com.yourname.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.repository.BackupRepository
import com.yourname.expensetracker.ui.home.HomeScreen
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

    // Personal mode routes
    data object BillReminders : Screen("personal/bill_reminders")
    data object NetWorth : Screen("personal/net_worth")
    data object Subscriptions : Screen("personal/subscriptions")
    data object SmsImport : Screen("personal/sms_import")
    data object ExpenseSplit : Screen("personal/expense_split")

    // Settings & Utilities
    data object NotificationCenter : Screen("notifications")
    data object LanguageSettings : Screen("settings/language")
    data object BackupRestore : Screen("settings/backup")
    data object SecuritySettings : Screen("settings/security")
    data object CurrencySettings : Screen("settings/currency")
    data object Paywall : Screen("settings/paywall")
    data object PrivacyPolicy : Screen("settings/privacy")

    // Optional Khata & Supplier
    data object Suppliers : Screen("shop/suppliers")
    data object Khata : Screen("shop/khata")
    data object CustomerDetail : Screen("shop/customer/{customerId}")
    data object SupplierDetail : Screen("shop/supplier/{supplierId}")
    data object BarcodeScanner : Screen("shop/barcode_scanner")
    data object Billing : Screen("shop/billing")
    data object ShopBranding : Screen("settings/shop_branding")
}

@Composable
fun ExpenseTrackerNavHost(
    sessionManager: SessionManager,
    backupRepository: BackupRepository,
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
            HomeScreen(
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
            TransactionListScreen(onNavigateBack = { navController.popBackStack() })
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

        composable(Screen.Insights.route) {
            com.yourname.expensetracker.ui.insights.InsightsScreen()
        }

        composable(Screen.ReceiptOcr.route) {
            com.yourname.expensetracker.ui.personal.ocr.ReceiptOcrScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Financial features
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

        // Common settings
        composable(Screen.NotificationCenter.route) {
            com.yourname.expensetracker.ui.common.NotificationCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LanguageSettings.route) {
            com.yourname.expensetracker.ui.settings.LanguageSettingsScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
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

        composable(Screen.Suppliers.route) {
            com.yourname.expensetracker.ui.shop.supplier.SupplierScreen(
                onOpenSupplier = { id ->
                    navController.navigate("shop/supplier/$id")
                },
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

        composable(Screen.BarcodeScanner.route) {
            com.yourname.expensetracker.ui.shop.inventory.BarcodeScannerScreen(
                onProductAdded = { _ -> },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Billing.route) {
            com.yourname.expensetracker.ui.shop.billing.BillingScreen(
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShopBranding.route) {
            com.yourname.expensetracker.ui.settings.ShopBrandingSettingsScreen(
                sessionManager = sessionManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Khata.route) {
            com.yourname.expensetracker.ui.shop.khata.KhataScreen(
                onOpenCustomer = { customerId ->
                    navController.navigate("shop/customer/$customerId")
                }
            )
        }
    }
}
