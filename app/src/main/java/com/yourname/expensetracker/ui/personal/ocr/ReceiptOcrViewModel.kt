package com.yourname.expensetracker.ui.personal.ocr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
import java.util.Locale
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
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    var detectedMerchant = ""
                    var detectedAmount = ""
                    val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text.trim() }.filter { it.isNotBlank() }

                    // Merchant: take top candidate
                    for (line in lines.take(4)) {
                        if (line.length in 3..40 && !line.any { it.isDigit() } && !line.contains("total", ignoreCase = true)) {
                            detectedMerchant = line
                            break
                        }
                    }
                    if (detectedMerchant.isBlank() && lines.isNotEmpty()) {
                        detectedMerchant = lines.first().take(30)
                    }

                    // Amounts: find price patterns (Total, Grand Total, etc.)
                    val priceRegex = Regex("""(?:[$₹€£]|total|amt)?\s*([0-9]+[.,][0-9]{2})""", RegexOption.IGNORE_CASE)
                    var maxAmount = 0.0
                    for (line in lines) {
                        val match = priceRegex.find(line)
                        if (match != null) {
                            val numStr = match.groupValues[1].replace(",", ".")
                            val num = numStr.toDoubleOrNull() ?: 0.0
                            if (num in 0.01..999999.0 && num > maxAmount) {
                                maxAmount = num
                            }
                        }
                    }
                    if (maxAmount > 0) {
                        detectedAmount = String.format(Locale.US, "%.2f", maxAmount)
                    }

                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            merchant = detectedMerchant.ifBlank { "Receipt Store" },
                            amount = if (detectedAmount.isNotBlank()) detectedAmount else (it.amount.ifBlank { "0.00" })
                        )
                    }
                }
                .addOnFailureListener {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            merchant = it.merchant.ifBlank { "Store" },
                            amount = it.amount.ifBlank { "0.00" }
                        )
                    }
                }
        } catch (e: Exception) {
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun onBitmapCaptured(bitmap: android.graphics.Bitmap) {
        _uiState.update { it.copy(isScanning = true) }
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    var detectedMerchant = ""
                    var detectedAmount = ""
                    val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text.trim() }.filter { it.isNotBlank() }

                    for (line in lines.take(4)) {
                        if (line.length in 3..40 && !line.any { it.isDigit() } && !line.contains("total", ignoreCase = true)) {
                            detectedMerchant = line
                            break
                        }
                    }
                    if (detectedMerchant.isBlank() && lines.isNotEmpty()) {
                        detectedMerchant = lines.first().take(30)
                    }

                    val priceRegex = Regex("""(?:[$₹€£]|total|amt)?\s*([0-9]+[.,][0-9]{2})""", RegexOption.IGNORE_CASE)
                    var maxAmount = 0.0
                    for (line in lines) {
                        val match = priceRegex.find(line)
                        if (match != null) {
                            val numStr = match.groupValues[1].replace(",", ".")
                            val num = numStr.toDoubleOrNull() ?: 0.0
                            if (num in 0.01..999999.0 && num > maxAmount) {
                                maxAmount = num
                            }
                        }
                    }
                    if (maxAmount > 0) {
                        detectedAmount = String.format(Locale.US, "%.2f", maxAmount)
                    }

                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            merchant = detectedMerchant.ifBlank { "Receipt Store" },
                            amount = if (detectedAmount.isNotBlank()) detectedAmount else (it.amount.ifBlank { "0.00" })
                        )
                    }
                }
                .addOnFailureListener {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            merchant = it.merchant.ifBlank { "Store" },
                            amount = it.amount.ifBlank { "0.00" }
                        )
                    }
                }
        } catch (e: Exception) {
            _uiState.update { it.copy(isScanning = false) }
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
