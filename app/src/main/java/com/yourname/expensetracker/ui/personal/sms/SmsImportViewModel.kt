package com.yourname.expensetracker.ui.personal.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

data class ParsedSmsResult(
    val rawText: String,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val suggestedCategory: String
)

@HiltViewModel
class SmsImportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _parsedResults = MutableStateFlow<List<ParsedSmsResult>>(emptyList())
    val parsedResults: StateFlow<List<ParsedSmsResult>> = _parsedResults.asStateFlow()

    private val _importedSuccess = MutableSharedFlow<String>()
    val importedSuccess = _importedSuccess.asSharedFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private var activeProfileId: Long? = null
    private var defaultAccountId: Long = 1L
    private var categories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                activeProfileId = profileId
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
                        categories = it
                    }
                }
            }
        }
        loadSampleSms()
    }

    private fun loadSampleSms() {
        val samples = listOf(
            "Rs 450.00 debited from A/c XX8901 on 04-Sep-26 at SWIGGY via UPI. Ref 987123.",
            "INR 1,250.00 debited from A/c XX1234 for UBER RIDE.",
            "A/c *4567 credited by Rs 45,000.00 on 01-Sep-26 by SALARY transfer.",
            "Paid Rs 2,100.00 at SHELL PETROL PUMP via UPI."
        )
        samples.forEach { parseSms(it) }
    }

    fun parseSms(text: String) {
        if (text.isBlank()) return

        // Extract amount: e.g. Rs 450.00 or INR 1,200.00
        val amountRegex = Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)")
        val matcher = amountRegex.matcher(text)
        var amount = 0.0
        if (matcher.find()) {
            val amtStr = matcher.group(1)?.replace(",", "")
            amount = amtStr?.toDoubleOrNull() ?: 0.0
        }

        // Detect type: credited vs debited/paid
        val isCredit = text.contains("credited", ignoreCase = true) || text.contains("received", ignoreCase = true)
        val type = if (isCredit) TransactionType.INCOME else TransactionType.EXPENSE

        // Detect merchant
        val merchant = when {
            text.contains("swiggy", ignoreCase = true) -> "Swiggy"
            text.contains("zomato", ignoreCase = true) -> "Zomato"
            text.contains("uber", ignoreCase = true) -> "Uber"
            text.contains("ola", ignoreCase = true) -> "Ola"
            text.contains("petrol", ignoreCase = true) -> "Petrol Pump"
            text.contains("salary", ignoreCase = true) -> "Salary"
            text.contains("amazon", ignoreCase = true) -> "Amazon"
            text.contains("flipkart", ignoreCase = true) -> "Flipkart"
            text.contains("netflix", ignoreCase = true) -> "Netflix"
            text.contains("starbucks", ignoreCase = true) -> "Starbucks"
            else -> {
                val atMatcher = Pattern.compile("(?i)(?:at|for|to)\\s+([A-Za-z0-9 ]{3,20})")
                val m = atMatcher.matcher(text)
                if (m.find()) m.group(1)?.trim() ?: "Bank Transaction" else "Bank Transaction"
            }
        }

        // Suggested category
        val category = when {
            merchant in listOf("Swiggy", "Zomato", "Starbucks") -> "Food & Dining"
            merchant in listOf("Uber", "Ola", "Petrol Pump") -> "Transportation"
            merchant in listOf("Amazon", "Flipkart") -> "Shopping"
            merchant == "Salary" -> "Salary"
            merchant == "Netflix" -> "Entertainment"
            else -> if (isCredit) "Income" else "General"
        }

        if (amount > 0) {
            val result = ParsedSmsResult(
                rawText = text,
                amount = amount,
                type = type,
                merchant = merchant,
                suggestedCategory = category
            )
            _parsedResults.update { listOf(result) + it }
        }
    }

    fun importParsed(result: ParsedSmsResult) {
        val profileId = activeProfileId ?: return
        val cat = categories.find { it.name.equals(result.suggestedCategory, ignoreCase = true) }

        viewModelScope.launch {
            val tx = Transaction(
                profileId = profileId,
                accountId = defaultAccountId,
                categoryId = cat?.id,
                type = result.type,
                amount = result.amount,
                date = System.currentTimeMillis(),
                note = "${result.merchant} (SMS Import)"
            )
            transactionRepository.insert(tx)
            _parsedResults.update { list -> list.filter { it != result } }
            _importedSuccess.emit("Imported ${result.merchant} successfully!")
        }
    }
}
