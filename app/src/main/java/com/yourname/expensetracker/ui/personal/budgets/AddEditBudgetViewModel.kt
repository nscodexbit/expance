package com.yourname.expensetracker.ui.personal.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Budget
import com.yourname.expensetracker.data.local.entity.BudgetPeriod
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.TransactionKind
import com.yourname.expensetracker.data.repository.BudgetRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetFormState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val amount: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _formState = MutableStateFlow(BudgetFormState())
    val formState: StateFlow<BudgetFormState> = _formState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    categoryRepository.getCategoriesByProfileAndKind(profileId, TransactionKind.EXPENSE.name)
                        .onEach { cats ->
                            _formState.update {
                                it.copy(
                                    categories = cats,
                                    selectedCategoryId = it.selectedCategoryId ?: cats.firstOrNull()?.id
                                )
                            }
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    fun selectCategory(id: Long) {
        _formState.update { it.copy(selectedCategoryId = id) }
    }

    fun onAmountChange(amount: String) {
        _formState.update { it.copy(amount = amount.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun onPeriodChange(period: BudgetPeriod) {
        _formState.update { it.copy(period = period) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _formState.value
        val profileId = activeProfileId ?: return
        val categoryId = state.selectedCategoryId ?: return
        val amount = state.amount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            budgetRepository.insert(
                Budget(
                    profileId = profileId,
                    categoryId = categoryId,
                    amount = amount,
                    period = state.period,
                    startDate = System.currentTimeMillis()
                )
            )
            _formState.update { it.copy(isSaving = false) }
            onSaved()
        }
    }
}
