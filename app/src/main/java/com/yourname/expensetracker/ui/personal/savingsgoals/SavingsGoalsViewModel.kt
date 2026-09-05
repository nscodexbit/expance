package com.yourname.expensetracker.ui.personal.savingsgoals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.SavingsGoal
import com.yourname.expensetracker.data.repository.SavingsGoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavingsGoalsViewModel @Inject constructor(
    private val goalRepository: SavingsGoalRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _goals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val goals: StateFlow<List<SavingsGoal>> = _goals.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    goalRepository.getGoalsByProfile(profileId)
                        .onEach { _goals.value = it }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    fun addGoal(name: String, targetAmount: Double, targetDate: Long? = null) {
        val profileId = activeProfileId ?: return
        viewModelScope.launch {
            goalRepository.insert(
                SavingsGoal(
                    profileId = profileId,
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    targetDate = targetDate
                )
            )
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            goalRepository.delete(goal)
        }
    }

    fun addDeposit(goal: SavingsGoal, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            goalRepository.update(goal.copy(currentAmount = goal.currentAmount + amount))
        }
    }
}
