package com.yourname.expensetracker.ui.personal.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Account
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.BudgetRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.ProfileRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import com.yourname.expensetracker.ui.personal.budgets.BudgetWithProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardRecentItem(
    val transaction: Transaction,
    val category: Category?,
    val account: Account?
)

data class PersonalDashboardUiState(
    val profile: Profile? = null,
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val recentTransactions: List<DashboardRecentItem> = emptyList(),
    val topBudgets: List<BudgetWithProgress> = emptyList(),
    val currencySymbol: String = "₹"
)

@HiltViewModel
class PersonalDashboardViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalDashboardUiState())
    val uiState: StateFlow<PersonalDashboardUiState> = _uiState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            val initialProfileId = dataInitializer.ensureInitialized()
            activeProfileId = initialProfileId
            loadDashboard(initialProfileId)

            sessionManager.currencySymbol.collect { symbol ->
                _uiState.update { it.copy(currencySymbol = symbol) }
            }
        }

        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null && profileId != activeProfileId) {
                    activeProfileId = profileId
                    loadDashboard(profileId)
                }
            }
        }
    }

    private fun loadDashboard(profileId: Long) {
        viewModelScope.launch {
            dataInitializer.ensureDefaultAccounts(profileId, ProfileType.PERSONAL)
            dataInitializer.ensureDefaultCategories(profileId)

            val profile = profileRepository.getProfileById(profileId)
            _uiState.update { it.copy(profile = profile) }
        }

        // Continually observe accounts and transactions to calculate current balance
        viewModelScope.launch {
            combine(
                accountRepository.getAccountsByProfile(profileId),
                transactionRepository.getTransactionsByProfile(profileId)
            ) { accounts, txns ->
                val startingTotal = accounts.sumOf { it.startingBalance }
                var netTxns = 0.0
                txns.forEach { t ->
                    when (t.type) {
                        TransactionType.INCOME, TransactionType.CASH_IN -> netTxns += t.amount
                        TransactionType.EXPENSE, TransactionType.CASH_OUT -> netTxns -= t.amount
                    }
                }
                Pair(accounts, startingTotal + netTxns)
            }.collect { (accounts, balance) ->
                _uiState.update { it.copy(accounts = accounts, totalBalance = balance) }
            }
        }

        // Monthly income and expense
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            combine(
                transactionRepository.getTotalByTypeAndDateRange(profileId, TransactionType.INCOME.name, monthStart, monthEnd),
                transactionRepository.getTotalByTypeAndDateRange(profileId, TransactionType.EXPENSE.name, monthStart, monthEnd)
            ) { income, expense ->
                _uiState.update {
                    it.copy(
                        monthIncome = income ?: 0.0,
                        monthExpense = expense ?: 0.0
                    )
                }
            }.collect()
        }

        // Observe recent transactions
        viewModelScope.launch {
            combine(
                transactionRepository.getTransactionsByProfile(profileId),
                categoryRepository.getCategoriesByProfile(profileId),
                accountRepository.getAccountsByProfile(profileId)
            ) { txns, categories, accounts ->
                val catMap = categories.associateBy { it.id }
                val accMap = accounts.associateBy { it.id }
                txns.take(5).map { txn ->
                    DashboardRecentItem(
                        transaction = txn,
                        category = txn.categoryId?.let { catMap[it] },
                        account = accMap[txn.accountId]
                    )
                }
            }.collect { recent ->
                _uiState.update { it.copy(recentTransactions = recent) }
            }
        }

        // Observe top budgets
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis

            budgetRepository.getBudgetsByProfile(profileId)
                .combine(categoryRepository.getCategoriesByProfile(profileId)) { budgets, categories ->
                    val catMap = categories.associateBy { it.id }
                    budgets.take(3).map { b ->
                        val spent = transactionRepository.getCategoryTotal(profileId, b.categoryId, start, end).first() ?: 0.0
                        val progress = if (b.amount > 0) (spent / b.amount).toFloat().coerceIn(0f, 1.5f) else 0f
                        BudgetWithProgress(b, catMap[b.categoryId], spent, progress)
                    }
                }.collect { topBudgets ->
                    _uiState.update { it.copy(topBudgets = topBudgets) }
                }
        }
    }

    fun deleteTransaction(id: Long, onUndoRequested: (() -> Unit) -> Unit) {
        viewModelScope.launch {
            transactionRepository.softDelete(id)
            onUndoRequested {
                viewModelScope.launch {
                    transactionRepository.undoDelete(id)
                }
            }
        }
    }
}
