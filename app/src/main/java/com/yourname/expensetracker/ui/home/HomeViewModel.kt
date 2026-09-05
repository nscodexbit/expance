package com.yourname.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    init {
        viewModelScope.launch {
            // Guarantee at least one valid profile exists
            val initialProfileId = dataInitializer.ensureInitialized()
            val initialProfile = profileRepository.getProfileById(initialProfileId)
            if (initialProfile != null) {
                _activeProfile.value = initialProfile
            }

            sessionManager.activeProfileId.collect { id ->
                val resolvedId = id ?: dataInitializer.ensureInitialized()
                val profile = profileRepository.getProfileById(resolvedId)
                    ?: profileRepository.getProfileById(dataInitializer.ensureInitialized())
                _activeProfile.value = profile
            }
        }
    }

    fun switchProfileMode(targetType: ProfileType) {
        viewModelScope.launch {
            val allProfiles = profileRepository.getAllProfilesList()
            var matched = allProfiles.firstOrNull { it.type == targetType }
            if (matched == null) {
                val newId = profileRepository.insert(
                    Profile(
                        name = if (targetType == ProfileType.SHOP) "My Shop" else "Personal Wallet",
                        type = targetType,
                        currency = "INR"
                    )
                )
                dataInitializer.ensureDefaultAccounts(newId, targetType)
                dataInitializer.ensureDefaultCategories(newId)
                matched = profileRepository.getProfileById(newId)
            }
            if (matched != null) {
                sessionManager.setActiveProfileId(matched.id)
                _activeProfile.value = matched
            }
        }
    }
}
