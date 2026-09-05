package com.yourname.expensetracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun createProfile(name: String, type: ProfileType) {
        viewModelScope.launch {
            val profileId = profileRepository.insert(
                Profile(name = name, type = type)
            )
            dataInitializer.ensureDefaultAccounts(profileId, type)
            dataInitializer.ensureDefaultCategories(profileId)
            sessionManager.setActiveProfileId(profileId)
            sessionManager.setOnboardingComplete(true)
        }
    }
}
