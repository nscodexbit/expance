package com.yourname.expensetracker.ui.personal.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Budget
import com.yourname.expensetracker.data.local.entity.BudgetPeriod
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.TransactionKind
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.BudgetRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val progress: Float
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _budgets = MutableStateFlow<List<BudgetWithProgress>>(emptyList())
    val budgets: StateFlow<List<BudgetWithProgress>> = _budgets.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    observeBudgets(profileId)
                }
            }
        }
    }

    private fun observeBudgets(profileId: Long) {
        budgetRepository.getBudgetsByProfile(profileId)
            .combine(categoryRepository.getCategoriesByProfile(profileId)) { budgets, categories ->
                val categoryMap = categories.associateBy { it.id }
                budgets.map { budget ->
                    val period = budget.period
                    val (start, end) = periodRange(budget.startDate, period)
                    val spent = transactionRepository.getCategoryTotal(
                        profileId, budget.categoryId, start, end
                    ).first() ?: 0.0
                    BudgetWithProgress(
                        budget = budget,
                        category = categoryMap[budget.categoryId],
                        spent = spent,
                        progress = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
                    )
                }
            }
            .onEach { _budgets.value = it }
            .launchIn(viewModelScope)
    }

    private fun periodRange(startDate: Long, period: BudgetPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = startDate }
        return when (period) {
            BudgetPeriod.WEEKLY -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
                startDate to cal.timeInMillis
            }
            BudgetPeriod.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
                startDate to cal.timeInMillis
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
        }
    }
}
