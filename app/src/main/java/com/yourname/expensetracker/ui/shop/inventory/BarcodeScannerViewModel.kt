package com.yourname.expensetracker.ui.shop.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.InventoryItem
import com.yourname.expensetracker.data.remote.ProductLookupService
import com.yourname.expensetracker.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeScannerUiState(
    val activeProduct: InventoryItem? = null,
    val isExistingProduct: Boolean = false,
    val isLoadingOnlineInfo: Boolean = false,
    val productName: String = "",
    val sellingPrice: String = "",
    val stockQuantity: String = "1",
    val barcode: String = ""
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productLookupService: ProductLookupService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    private var lastScannedBarcode: String? = null
    private var lastScanTime: Long = 0

    fun onBarcodeScanned(barcode: String) {
        val now = System.currentTimeMillis()
        if (barcode == lastScannedBarcode && now - lastScanTime < 3000) return
        lastScannedBarcode = barcode
        lastScanTime = now

        viewModelScope.launch {
            val profileId = sessionManager.activeProfileId.firstOrNull() ?: 1L
            val existing = inventoryRepository.getItemByBarcode(profileId, barcode)
            if (existing != null) {
                _uiState.value = _uiState.value.copy(
                    activeProduct = existing,
                    isExistingProduct = true,
                    productName = existing.name,
                    sellingPrice = if (existing.sellingPrice > 0) existing.sellingPrice.toString() else "",
                    stockQuantity = existing.currentStock.toString(),
                    barcode = barcode,
                    isLoadingOnlineInfo = false
                )
            } else {
                // Not in DB, draft new item
                val draft = InventoryItem(
                    profileId = profileId,
                    name = "Scanned Item ($barcode)",
                    barcode = barcode,
                    costPrice = 0.0,
                    sellingPrice = 0.0,
                    currentStock = 1.0
                )
                _uiState.value = _uiState.value.copy(
                    activeProduct = draft,
                    isExistingProduct = false,
                    productName = draft.name,
                    sellingPrice = "",
                    stockQuantity = "10",
                    barcode = barcode,
                    isLoadingOnlineInfo = true
                )

                // Query public Open Food Facts API
                val onlineInfo = productLookupService.lookupProduct(barcode)
                if (onlineInfo != null) {
                    _uiState.value = _uiState.value.copy(
                        productName = onlineInfo.name,
                        isLoadingOnlineInfo = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingOnlineInfo = false)
                }
            }
        }
    }

    fun openManualEntryDialog() {
        val draft = InventoryItem(
            profileId = 1L,
            name = "",
            barcode = "",
            costPrice = 0.0,
            sellingPrice = 0.0,
            currentStock = 1.0
        )
        _uiState.value = _uiState.value.copy(
            activeProduct = draft,
            isExistingProduct = false,
            productName = "",
            sellingPrice = "",
            stockQuantity = "1",
            barcode = ""
        )
    }

    fun setProductName(name: String) {
        _uiState.value = _uiState.value.copy(productName = name)
    }

    fun setSellingPrice(price: String) {
        _uiState.value = _uiState.value.copy(sellingPrice = price)
    }

    fun setStockQuantity(qty: String) {
        _uiState.value = _uiState.value.copy(stockQuantity = qty)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(activeProduct = null)
        lastScannedBarcode = null
    }

    fun saveAndConfirm(onConfirmed: (InventoryItem) -> Unit) {
        viewModelScope.launch {
            val profileId = sessionManager.activeProfileId.firstOrNull() ?: 1L
            val price = _uiState.value.sellingPrice.toDoubleOrNull() ?: 0.0
            val stock = _uiState.value.stockQuantity.toDoubleOrNull() ?: 1.0
            val name = _uiState.value.productName.ifBlank { "Item ${_uiState.value.barcode}" }

            val itemToSave = _uiState.value.activeProduct?.copy(
                profileId = profileId,
                name = name,
                sellingPrice = price,
                currentStock = stock
            ) ?: InventoryItem(
                profileId = profileId,
                name = name,
                barcode = _uiState.value.barcode.ifBlank { null },
                sellingPrice = price,
                currentStock = stock
            )

            val savedId = if (itemToSave.id == 0L) {
                inventoryRepository.saveItem(itemToSave)
            } else {
                inventoryRepository.updateItem(itemToSave)
                itemToSave.id
            }

            val finalItem = itemToSave.copy(id = savedId)
            dismissDialog()
            onConfirmed(finalItem)
        }
    }
}
