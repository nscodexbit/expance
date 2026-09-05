package com.yourname.expensetracker.ui.personal.recurringtemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Frequency
import com.yourname.expensetracker.data.local.entity.RecurringTemplate
import com.yourname.expensetracker.data.repository.RecurringTemplateRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplateItem(
    val template: RecurringTemplate,
    val category: Category?
)

@HiltViewModel
class RecurringTemplatesViewModel @Inject constructor(
    private val templateRepository: RecurringTemplateRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: com.yourname.expensetracker.data.repository.AccountRepository,
    private val transactionRepository: TransactionRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _templates = MutableStateFlow<List<TemplateItem>>(emptyList())
    val templates: StateFlow<List<TemplateItem>> = _templates.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<com.yourname.expensetracker.data.local.entity.Account>>(emptyList())
    val accounts: StateFlow<List<com.yourname.expensetracker.data.local.entity.Account>> = _accounts.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    categoryRepository.getCategoriesByProfile(profileId)
                        .onEach { _categories.value = it }
                        .launchIn(viewModelScope)

                    accountRepository.getAccountsByProfile(profileId)
                        .onEach { _accounts.value = it }
                        .launchIn(viewModelScope)

                    templateRepository.getTemplatesByProfile(profileId)
                        .combine(categoryRepository.getCategoriesByProfile(profileId)) { templates, categories ->
                            val catMap = categories.associateBy { it.id }
                            templates.map { TemplateItem(it, catMap[it.categoryId]) }
                        }
                        .onEach { _templates.value = it }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    fun addTemplate(
        label: String,
        amount: Double,
        frequency: Frequency,
        accountId: Long,
        categoryId: Long
    ) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            templateRepository.insert(
                RecurringTemplate(
                    profileId = profileId,
                    accountId = accountId,
                    categoryId = categoryId,
                    amount = amount,
                    label = label,
                    frequency = frequency,
                    nextDueDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTemplate(template: RecurringTemplate) {
        viewModelScope.launch {
            templateRepository.delete(template)
        }
    }

    fun executeTemplateNow(template: RecurringTemplate) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val transaction = Transaction(
                profileId = template.profileId,
                accountId = template.accountId,
                categoryId = template.categoryId,
                type = TransactionType.EXPENSE,
                amount = template.amount,
                date = currentTime,
                note = "${template.label} (Recurring entry)",
                recurringTemplateId = template.id
            )
            transactionRepository.insert(transaction)

            val nextDueDate = when (template.frequency) {
                Frequency.DAILY -> currentTime + 86_400_000L
                Frequency.WEEKLY -> currentTime + 604_800_000L
                Frequency.MONTHLY -> currentTime + 2_592_000_000L
            }
            templateRepository.update(template.copy(nextDueDate = nextDueDate))
        }
    }
}
