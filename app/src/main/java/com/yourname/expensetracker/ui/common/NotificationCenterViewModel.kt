package com.yourname.expensetracker.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.BillReminder
import com.yourname.expensetracker.data.local.entity.InventoryItem
import com.yourname.expensetracker.data.repository.BillReminderRepository
import com.yourname.expensetracker.data.repository.BudgetRepository
import com.yourname.expensetracker.data.repository.CustomerRepository
import com.yourname.expensetracker.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    LOW_STOCK,
    CREDIT_DUE,
    BILL_OVERDUE,
    BUDGET_WARNING
}

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val customerRepository: CustomerRepository,
    private val billReminderRepository: BillReminderRepository,
    private val budgetRepository: BudgetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    loadAlerts(profileId)
                }
            }
        }
    }

    private fun loadAlerts(profileId: Long) {
        viewModelScope.launch {
            combine(
                inventoryRepository.getLowStockItems(profileId),
                billReminderRepository.getPendingBills(profileId)
            ) { lowStock, pendingBills ->
                val list = mutableListOf<AppNotification>()

                // Low stock alerts
                lowStock.forEach { item ->
                    list.add(
                        AppNotification(
                            id = "stock_${item.id}",
                            title = "Low Stock: ${item.name}",
                            description = "Only ${item.currentStock} ${item.unit} remaining (alert threshold: ${item.minStockAlert})",
                            type = NotificationType.LOW_STOCK
                        )
                    )
                }

                // Bill reminders
                val now = System.currentTimeMillis()
                pendingBills.forEach { bill ->
                    val diffDays = ((bill.dueDate - now) / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays <= 3) {
                        val status = if (diffDays < 0) "Overdue by ${-diffDays} days!" else "Due in $diffDays days"
                        list.add(
                            AppNotification(
                                id = "bill_${bill.id}",
                                title = "Bill Due: ${bill.title}",
                                description = "$status • Amount: ₹ ${bill.amount}",
                                type = NotificationType.BILL_OVERDUE
                            )
                        )
                    }
                }

                list
            }.collect {
                _notifications.value = it
            }
        }
    }

    fun dismissNotification(id: String) {
        _notifications.update { it.filter { n -> n.id != id } }
    }
}
