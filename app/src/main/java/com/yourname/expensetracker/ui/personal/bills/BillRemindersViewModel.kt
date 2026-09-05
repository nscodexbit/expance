package com.yourname.expensetracker.ui.personal.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.BillReminder
import com.yourname.expensetracker.data.repository.BillReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillRemindersViewModel @Inject constructor(
    private val billReminderRepository: BillReminderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _bills = MutableStateFlow<List<BillReminder>>(emptyList())
    val bills: StateFlow<List<BillReminder>> = _bills.asStateFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    billReminderRepository.getBills(profileId).collect {
                        _bills.value = it
                    }
                }
            }
        }
    }

    fun addBill(title: String, amount: Double, dueDate: Long, isRecurring: Boolean, category: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            val bill = BillReminder(
                profileId = profileId,
                title = title,
                amount = amount,
                dueDate = dueDate,
                isPaid = false,
                isRecurring = isRecurring,
                category = category
            )
            billReminderRepository.saveBill(bill)
        }
    }

    fun togglePaid(bill: BillReminder) {
        viewModelScope.launch {
            billReminderRepository.setPaidStatus(bill.id, !bill.isPaid)
        }
    }

    fun deleteBill(bill: BillReminder) {
        viewModelScope.launch {
            billReminderRepository.deleteBill(bill)
        }
    }
}
