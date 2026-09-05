package com.yourname.expensetracker.ui.personal.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.TransactionKind
import com.yourname.expensetracker.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    categoryRepository.getCategoriesByProfile(profileId)
                        .onEach { _categories.value = it }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    fun addCategory(name: String, kind: TransactionKind) {
        val profileId = activeProfileId ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.insert(
                Category(
                    profileId = profileId,
                    name = name.trim(),
                    kind = kind,
                    isCustom = true
                )
            )
        }
    }

    fun deleteCategory(category: Category) {
        if (!category.isCustom) return
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }
}
