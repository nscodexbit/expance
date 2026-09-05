package com.yourname.expensetracker.ui.personal.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.*
import com.yourname.expensetracker.data.repository.*
import com.yourname.expensetracker.ui.common.Calculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val accounts: List<Account> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val duplicateWarning: Boolean = false,
    val isSaving: Boolean = false,
    val currencySymbol: String = "₹"
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    val calculator = Calculator()

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            val initialProfileId = dataInitializer.ensureInitialized()
            activeProfileId = initialProfileId
            loadData(initialProfileId)

            sessionManager.currencySymbol.collect { symbol ->
                _uiState.update { it.copy(currencySymbol = symbol) }
            }
        }

        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null && profileId != activeProfileId) {
                    activeProfileId = profileId
                    loadData(profileId)
                }
            }
        }
    }

    private fun loadData(profileId: Long) {
        viewModelScope.launch {
            // Ensure default accounts and categories exist
            dataInitializer.ensureDefaultAccounts(profileId, ProfileType.PERSONAL)
            dataInitializer.ensureDefaultCategories(profileId)

            accountRepository.getAccountsByProfile(profileId)
                .onEach { accounts ->
                    _uiState.update { current ->
                        val validAccountId = if (accounts.any { it.id == current.selectedAccountId }) {
                            current.selectedAccountId
                        } else {
                            accounts.firstOrNull()?.id
                        }
                        current.copy(
                            accounts = accounts,
                            selectedAccountId = validAccountId
                        )
                    }
                }
                .launchIn(viewModelScope)

            categoryRepository.getCategoriesByProfileAndKind(profileId, TransactionKind.EXPENSE.name)
                .onEach { cats ->
                    _uiState.update { current ->
                        val validCatId = if (current.type == TransactionType.EXPENSE && current.selectedCategoryId == null) {
                            cats.firstOrNull()?.id
                        } else current.selectedCategoryId
                        current.copy(
                            expenseCategories = cats,
                            selectedCategoryId = validCatId
                        )
                    }
                }
                .launchIn(viewModelScope)

            categoryRepository.getCategoriesByProfileAndKind(profileId, TransactionKind.INCOME.name)
                .onEach { cats ->
                    _uiState.update { current ->
                        current.copy(incomeCategories = cats)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun selectType(type: TransactionType) {
        _uiState.update { current ->
            val newCategories = if (type == TransactionType.EXPENSE || type == TransactionType.CASH_OUT) {
                current.expenseCategories
            } else {
                current.incomeCategories
            }
            val newCatId = newCategories.firstOrNull()?.id ?: current.selectedCategoryId
            current.copy(
                type = type,
                selectedCategoryId = newCatId
            )
        }
    }

    fun selectAccount(id: Long) {
        _uiState.update { it.copy(selectedAccountId = id) }
    }

    fun selectCategory(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun setAmount(amountStr: String) {
        calculator.setDisplay(amountStr)
        checkDuplicate(calculator.evaluate())
    }

    fun parseVoiceInput(text: String) {
        if (text.isBlank()) return
        val regex = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)")
        val matcher = regex.matcher(text)
        var amount = 0.0
        if (matcher.find()) {
            amount = matcher.group(1)?.toDoubleOrNull() ?: 0.0
        }
        val cleanNote = text.replace(regex.toRegex(), "")
            .replace("rupees", "", ignoreCase = true)
            .replace("rs", "", ignoreCase = true)
            .replace("spent on", "", ignoreCase = true)
            .replace("paid for", "", ignoreCase = true)
            .trim()

        if (amount > 0) {
            setAmount(amount.toString())
        }
        if (cleanNote.isNotBlank()) {
            onNoteChange(cleanNote.replaceFirstChar { it.uppercase() })
        }
    }

    fun checkDuplicate(amount: Double) {
        val state = _uiState.value
        val profileId = activeProfileId ?: return
        val accountId = state.selectedAccountId ?: return
        val categoryId = state.selectedCategoryId
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            val duplicate = transactionRepository.findDuplicate(
                profileId = profileId,
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                startTime = now - 60_000,
                endTime = now + 1_000
            )
            _uiState.update { it.copy(duplicateWarning = duplicate != null) }
        }
    }

    fun save(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        val profileId = activeProfileId ?: run {
            onError("No active profile found. Please retry.")
            return
        }

        val amount = calculator.evaluate()
        if (amount <= 0.0) {
            onError("Please enter an amount greater than 0")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                // Ensure valid account ID
                var accountId = state.selectedAccountId
                if (accountId == null) {
                    val accs = accountRepository.getAccountsListByProfile(profileId)
                    if (accs.isNotEmpty()) {
                        accountId = accs.first().id
                    } else {
                        dataInitializer.ensureDefaultAccounts(profileId, ProfileType.PERSONAL)
                        val newAccs = accountRepository.getAccountsListByProfile(profileId)
                        accountId = newAccs.firstOrNull()?.id ?: 1L
                    }
                }

                // Ensure category ID
                var categoryId = state.selectedCategoryId
                if (categoryId == null) {
                    val kind = if (state.type == TransactionType.INCOME || state.type == TransactionType.CASH_IN) {
                        TransactionKind.INCOME.name
                    } else {
                        TransactionKind.EXPENSE.name
                    }
                    val cats = categoryRepository.getCategoriesByProfileAndKind(profileId, kind).first()
                    categoryId = cats.firstOrNull()?.id
                }

                val transaction = Transaction(
                    profileId = profileId,
                    accountId = accountId,
                    categoryId = categoryId,
                    type = state.type,
                    amount = amount,
                    date = state.date,
                    note = state.note.ifBlank { null }
                )

                val newId = transactionRepository.insert(transaction)
                _uiState.update { it.copy(isSaving = false) }
                if (newId > 0) {
                    onSuccess()
                } else {
                    onError("Failed to record entry. Please check values.")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                onError("Error: ${e.localizedMessage ?: "Could not save entry"}")
            }
        }
    }
}
