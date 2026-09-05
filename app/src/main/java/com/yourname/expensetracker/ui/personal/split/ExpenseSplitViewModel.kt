package com.yourname.expensetracker.ui.personal.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.ExpenseSplit
import com.yourname.expensetracker.data.repository.ExpenseSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseSplitViewModel @Inject constructor(
    private val splitRepository: ExpenseSplitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _splits = MutableStateFlow<List<ExpenseSplit>>(emptyList())
    val splits: StateFlow<List<ExpenseSplit>> = _splits.asStateFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                activeProfileId = profileId
                if (profileId != null) {
                    splitRepository.getSplits(profileId).collect {
                        _splits.value = it
                    }
                }
            }
        }
    }

    fun addSplit(title: String, totalAmount: Double, paidBy: String, participants: String) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            val split = ExpenseSplit(
                profileId = profileId,
                title = title,
                totalAmount = totalAmount,
                paidBy = paidBy,
                participants = participants,
                date = System.currentTimeMillis(),
                isSettled = false
            )
            splitRepository.saveSplit(split)
        }
    }

    fun toggleSettled(split: ExpenseSplit) {
        viewModelScope.launch {
            splitRepository.setSettledStatus(split.id, !split.isSettled)
        }
    }

    fun deleteSplit(split: ExpenseSplit) {
        viewModelScope.launch {
            splitRepository.deleteSplit(split)
        }
    }
}
