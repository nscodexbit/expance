package com.yourname.expensetracker.ui.personal.networth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Account
import com.yourname.expensetracker.data.local.entity.SavingsGoal
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.SavingsGoalRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountWithBalance(
    val account: Account,
    val currentBalance: Double
)

data class NetWorthState(
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netWorth: Double = 0.0,
    val accounts: List<AccountWithBalance> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList()
)

@HiltViewModel
class NetWorthViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(NetWorthState())
    val state: StateFlow<NetWorthState> = _state.asStateFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    combine(
                        accountRepository.getAccountsByProfile(profileId),
                        transactionRepository.getTransactionsByProfile(profileId),
                        savingsGoalRepository.getGoalsByProfile(profileId)
                    ) { accounts, txns, goals ->
                        var assets = 0.0
                        var liabilities = 0.0

                        val accountsWithBalance = accounts.map { acc ->
                            val accTxns = txns.filter { it.accountId == acc.id }
                            var bal = acc.startingBalance
                            for (t in accTxns) {
                                when (t.type) {
                                    TransactionType.INCOME, TransactionType.CASH_IN -> bal += t.amount
                                    TransactionType.EXPENSE, TransactionType.CASH_OUT -> bal -= t.amount
                                }
                            }
                            if (bal >= 0) {
                                assets += bal
                            } else {
                                liabilities += -bal
                            }
                            AccountWithBalance(account = acc, currentBalance = bal)
                        }

                        val savingsTotal = goals.sumOf { it.currentAmount }
                        assets += savingsTotal

                        NetWorthState(
                            totalAssets = assets,
                            totalLiabilities = liabilities,
                            netWorth = assets - liabilities,
                            accounts = accountsWithBalance,
                            savingsGoals = goals
                        )
                    }.collect {
                        _state.value = it
                    }
                }
            }
        }
    }
}
