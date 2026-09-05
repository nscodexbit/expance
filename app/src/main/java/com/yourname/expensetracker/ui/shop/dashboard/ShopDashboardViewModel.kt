package com.yourname.expensetracker.ui.shop.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.dao.InvoiceWithItems
import com.yourname.expensetracker.data.local.entity.InventoryItem
import com.yourname.expensetracker.data.repository.CustomerRepository
import com.yourname.expensetracker.data.repository.InventoryRepository
import com.yourname.expensetracker.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ShopDashboardUiState(
    val todaySales: Double = 0.0,
    val monthSales: Double = 0.0,
    val totalGstCollected: Double = 0.0,
    val totalUdharDue: Double = 0.0,
    val lowStockItems: List<InventoryItem> = emptyList(),
    val recentInvoices: List<InvoiceWithItems> = emptyList(),
    val shopName: String = "My Store",
    val isLoading: Boolean = false
)

@HiltViewModel
class ShopDashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopDashboardUiState())
    val uiState: StateFlow<ShopDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                val id = profileId ?: 1L
                loadDashboardData(id)
            }
        }
        viewModelScope.launch {
            sessionManager.shopName.collect { name ->
                _uiState.value = _uiState.value.copy(shopName = name)
            }
        }
    }

    private fun loadDashboardData(profileId: Long) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        val now = System.currentTimeMillis()

        viewModelScope.launch {
            invoiceRepository.getTotalSalesInRange(profileId, startOfToday, now).collect { today ->
                _uiState.value = _uiState.value.copy(todaySales = today ?: 0.0)
            }
        }

        viewModelScope.launch {
            invoiceRepository.getTotalSalesInRange(profileId, startOfMonth, now).collect { month ->
                _uiState.value = _uiState.value.copy(monthSales = month ?: 0.0)
            }
        }

        viewModelScope.launch {
            invoiceRepository.getTotalGstInRange(profileId, startOfMonth, now).collect { gst ->
                _uiState.value = _uiState.value.copy(totalGstCollected = gst ?: 0.0)
            }
        }

        viewModelScope.launch {
            inventoryRepository.getLowStockItems(profileId).collect { lowItems ->
                _uiState.value = _uiState.value.copy(lowStockItems = lowItems)
            }
        }

        viewModelScope.launch {
            invoiceRepository.getInvoicesWithItems(profileId).collect { list ->
                _uiState.value = _uiState.value.copy(recentInvoices = list.take(10))
            }
        }

        viewModelScope.launch {
            customerRepository.getCustomersByProfile(profileId).collect { customers ->
                var totalDue = 0.0
                customers.forEach { c ->
                    val bal = customerRepository.getOutstandingBalance(c.id).firstOrNull() ?: 0.0
                    if (bal > 0) totalDue += bal
                }
                _uiState.value = _uiState.value.copy(totalUdharDue = totalDue)
            }
        }
    }
}
