package com.yourname.expensetracker.ui.personal.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Frequency
import com.yourname.expensetracker.data.local.entity.RecurringTemplate
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.RecurringTemplateRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetectedSubscription(
    val title: String,
    val amount: Double,
    val frequency: String,
    val occurrences: Int
)

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val recurringRepository: RecurringTemplateRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _templates = MutableStateFlow<List<RecurringTemplate>>(emptyList())
    val templates: StateFlow<List<RecurringTemplate>> = _templates.asStateFlow()

    private val _detected = MutableStateFlow<List<DetectedSubscription>>(emptyList())
    val detected: StateFlow<List<DetectedSubscription>> = _detected.asStateFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private var activeProfileId: Long? = null
    private var defaultAccountId: Long = 1L
    private var defaultCategoryId: Long = 1L

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    recurringRepository.getTemplatesByProfile(profileId).collect {
                        _templates.value = it
                    }
                }
            }
        }
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    accountRepository.getAccountsByProfile(profileId).collect {
                        defaultAccountId = it.firstOrNull()?.id ?: 1L
                    }
                }
            }
        }
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    categoryRepository.getCategoriesByProfile(profileId).collect {
                        defaultCategoryId = it.firstOrNull()?.id ?: 1L
                    }
                }
            }
        }
    }

    fun detectSubscriptions() {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            transactionRepository.getTransactionsByProfile(profileId).first().let { txs: List<Transaction> ->
                val knownKeywords = listOf("netflix", "spotify", "prime", "gym", "hotstar", "youtube", "apple", "icloud", "google one", "broadband", "wifi", "rent")
                val found = mutableListOf<DetectedSubscription>()

                val noteGroups = txs.filter { !it.note.isNullOrBlank() }.groupBy { it.note!!.trim().lowercase() }
                for ((note, list) in noteGroups) {
                    val isKeyword = knownKeywords.any { note.contains(it) }
                    if (list.size >= 2 || isKeyword) {
                        found.add(
                            DetectedSubscription(
                                title = note.replaceFirstChar { it.uppercase() },
                                amount = list.first().amount,
                                frequency = "MONTHLY",
                                occurrences = list.size
                            )
                        )
                    }
                }

                _detected.value = found
            }
        }
    }

    fun addSubscription(name: String, amount: Double, frequency: Frequency = Frequency.MONTHLY) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            val template = RecurringTemplate(
                profileId = profileId,
                accountId = defaultAccountId,
                categoryId = defaultCategoryId,
                amount = amount,
                label = name,
                frequency = frequency,
                nextDueDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            )
            recurringRepository.insert(template)
            _detected.update { list -> list.filter { it.title != name } }
        }
    }

    fun deleteSubscription(template: RecurringTemplate) {
        viewModelScope.launch {
            recurringRepository.delete(template)
        }
    }
}
