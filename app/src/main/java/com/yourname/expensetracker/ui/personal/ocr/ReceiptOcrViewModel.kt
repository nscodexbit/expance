package com.yourname.expensetracker.ui.personal.ocr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Account
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

data class ReceiptOcrUiState(
    val imageUri: Uri? = null,
    val merchant: String = "",
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isScanning: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class ReceiptOcrViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptOcrUiState())
    val uiState: StateFlow<ReceiptOcrUiState> = _uiState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    categoryRepository.getCategoriesByProfile(profileId).onEach { cats ->
                        _uiState.update {
                            it.copy(
                                categories = cats,
                                selectedCategoryId = it.selectedCategoryId ?: cats.firstOrNull()?.id
                            )
                        }
                    }.launchIn(viewModelScope)

                    accountRepository.getAccountsByProfile(profileId).onEach { accs ->
                        _uiState.update {
                            it.copy(
                                accounts = accs,
                                selectedAccountId = it.selectedAccountId ?: accs.firstOrNull()?.id
                            )
                        }
                    }.launchIn(viewModelScope)
                }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, isScanning = true) }
        // On-device receipt text parsing simulation
        viewModelScope.launch {
            kotlinx.coroutines.delay(800) // Fast on-device parsing simulation
            val sampleMerchants = listOf("Walmart Supercenter", "Trader Joe's", "Shell Gas Station", "Starbucks Coffee", "Target Express")
            val sampleAmounts = listOf("42.50", "18.75", "65.20", "8.40", "112.00")
            val idx = (System.currentTimeMillis() % sampleMerchants.size).toInt()

            _uiState.update {
                it.copy(
                    isScanning = false,
                    merchant = sampleMerchants[idx],
                    amount = sampleAmounts[idx]
                )
            }
        }
    }

    fun updateMerchant(name: String) {
        _uiState.update { it.copy(merchant = name) }
    }

    fun updateAmount(amt: String) {
        _uiState.update { it.copy(amount = amt) }
    }

    fun updateCategory(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun updateAccount(id: Long) {
        _uiState.update { it.copy(selectedAccountId = id) }
    }

    fun saveTransaction(onDone: () -> Unit) {
        val state = _uiState.value
        val profileId = activeProfileId ?: return
        val accId = state.selectedAccountId ?: return
        val amt = state.amount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            transactionRepository.insert(
                Transaction(
                    profileId = profileId,
                    accountId = accId,
                    categoryId = state.selectedCategoryId,
                    type = TransactionType.EXPENSE,
                    amount = amt,
                    date = state.date,
                    note = "Receipt: ${state.merchant.ifBlank { "Store" }}"
                )
            )
            _uiState.update { it.copy(isSaved = true) }
            onDone()
        }
    }
}
