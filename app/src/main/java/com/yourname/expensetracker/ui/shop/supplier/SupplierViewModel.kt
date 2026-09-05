package com.yourname.expensetracker.ui.shop.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Supplier
import com.yourname.expensetracker.data.local.entity.SupplierPayment
import com.yourname.expensetracker.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupplierDetailState(
    val supplier: Supplier? = null,
    val payments: List<SupplierPayment> = emptyList(),
    val totalBills: Double = 0.0,
    val totalPaid: Double = 0.0,
    val payableBalance: Double = 0.0
)

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _detailState = MutableStateFlow(SupplierDetailState())
    val detailState: StateFlow<SupplierDetailState> = _detailState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            val initialProfileId = dataInitializer.ensureInitialized()
            activeProfileId = initialProfileId
            supplierRepository.getSuppliersByProfile(initialProfileId)
                .onEach { _suppliers.value = it }
                .launchIn(viewModelScope)

            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null && profileId != activeProfileId) {
                    activeProfileId = profileId
                    supplierRepository.getSuppliersByProfile(profileId)
                        .onEach { _suppliers.value = it }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    fun loadSupplierDetail(supplierId: Long) {
        viewModelScope.launch {
            val supplier = supplierRepository.getSupplierById(supplierId)
            _detailState.value = _detailState.value.copy(supplier = supplier)
        }
        supplierRepository.getPaymentsBySupplier(supplierId)
            .onEach { payments ->
                val bills = payments.filter { it.note?.startsWith("[BILL]") == true }.sumOf { it.amount }
                val paid = payments.filter { it.note?.startsWith("[BILL]") != true }.sumOf { it.amount }
                _detailState.value = _detailState.value.copy(
                    payments = payments,
                    totalBills = bills,
                    totalPaid = paid,
                    payableBalance = bills - paid
                )
            }
            .launchIn(viewModelScope)
    }

    fun addSupplier(name: String, phone: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val profileId = activeProfileId ?: dataInitializer.ensureInitialized()
            supplierRepository.insert(
                Supplier(
                    profileId = profileId,
                    name = name.trim(),
                    phone = phone?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            supplierRepository.delete(supplier)
        }
    }

    fun addBill(supplierId: Long, amount: Double, note: String? = null) {
        if (amount <= 0) return
        viewModelScope.launch {
            supplierRepository.insertPayment(
                SupplierPayment(
                    supplierId = supplierId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = "[BILL] ${note?.trim() ?: "Goods / Purchase on Credit"}"
                )
            )
        }
    }

    fun addPayment(supplierId: Long, amount: Double, note: String? = null) {
        if (amount <= 0) return
        viewModelScope.launch {
            supplierRepository.insertPayment(
                SupplierPayment(
                    supplierId = supplierId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = "[PAYMENT] ${note?.trim() ?: "Settlement Payment"}"
                )
            )
        }
    }

    fun deletePayment(payment: SupplierPayment) {
        viewModelScope.launch {
            supplierRepository.deletePayment(payment)
        }
    }

    fun clearDetail() {
        _detailState.value = SupplierDetailState()
    }
}
