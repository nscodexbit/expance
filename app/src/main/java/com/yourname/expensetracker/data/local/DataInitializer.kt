package com.yourname.expensetracker.data.local

import com.yourname.expensetracker.data.local.entity.Account
import com.yourname.expensetracker.data.local.entity.AccountType
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.ProfileType
import com.yourname.expensetracker.data.local.entity.TransactionKind
import com.yourname.expensetracker.data.repository.AccountRepository
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.ProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataInitializer @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) {

    /**
     * Guarantees that at least one profile exists, and that default accounts and
     * categories are seeded. Returns the active profile ID.
     */
    suspend fun ensureInitialized(): Long {
        var activeId = sessionManager.activeProfileId.firstOrNull()
        var profile = if (activeId != null) profileRepository.getProfileById(activeId) else null

        if (profile == null) {
            val allProfiles = profileRepository.getAllProfilesList()
            if (allProfiles.isNotEmpty()) {
                profile = allProfiles.first()
                activeId = profile.id
                sessionManager.setActiveProfileId(profile.id)
            } else {
                val newProfileId = profileRepository.insert(
                    Profile(
                        name = "Personal",
                        type = ProfileType.PERSONAL,
                        currency = "INR"
                    )
                )
                activeId = newProfileId
                sessionManager.setActiveProfileId(newProfileId)
                profile = profileRepository.getProfileById(newProfileId)
            }
        }

        val profileId = activeId ?: profile?.id ?: 1L
        sessionManager.setOnboardingComplete(true)

        // Ensure default accounts and categories for this profile
        ensureDefaultAccounts(profileId, profile?.type ?: ProfileType.PERSONAL)
        ensureDefaultCategories(profileId)

        return profileId
    }

    suspend fun ensureDefaultAccounts(profileId: Long, type: ProfileType) {
        val count = accountRepository.getAccountCount(profileId)
        if (count > 0) return

        if (type == ProfileType.SHOP) {
            accountRepository.insert(
                Account(
                    profileId = profileId,
                    name = "Cash Drawer",
                    type = AccountType.CASH,
                    startingBalance = 0.0
                )
            )
            accountRepository.insert(
                Account(
                    profileId = profileId,
                    name = "Shop Bank",
                    type = AccountType.BANK,
                    startingBalance = 0.0
                )
            )
        } else {
            accountRepository.insert(
                Account(
                    profileId = profileId,
                    name = "Cash",
                    type = AccountType.CASH,
                    startingBalance = 0.0
                )
            )
            accountRepository.insert(
                Account(
                    profileId = profileId,
                    name = "Bank Account",
                    type = AccountType.BANK,
                    startingBalance = 0.0
                )
            )
            accountRepository.insert(
                Account(
                    profileId = profileId,
                    name = "UPI / Wallet",
                    type = AccountType.WALLET,
                    startingBalance = 0.0
                )
            )
        }
    }

    suspend fun ensureDefaultCategories(profileId: Long) {
        val count = categoryRepository.getCategoryCount(profileId)
        if (count > 0) return

        val expenseCategories = listOf(
            Category(profileId = profileId, name = "Food & Dining", icon = "restaurant", colorHex = "#F44336", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Groceries", icon = "local_grocery_store", colorHex = "#8BC34A", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Transport", icon = "directions_car", colorHex = "#2196F3", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Shopping", icon = "shopping_cart", colorHex = "#9C27B0", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Rent & Home", icon = "home", colorHex = "#795548", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Utilities", icon = "bolt", colorHex = "#FF9800", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Healthcare", icon = "medical_services", colorHex = "#4CAF50", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Entertainment", icon = "movie", colorHex = "#E91E63", kind = TransactionKind.EXPENSE),
            Category(profileId = profileId, name = "Education", icon = "school", colorHex = "#00BCD4", kind = TransactionKind.EXPENSE)
        )

        val incomeCategories = listOf(
            Category(profileId = profileId, name = "Salary", icon = "payments", colorHex = "#4CAF50", kind = TransactionKind.INCOME),
            Category(profileId = profileId, name = "Business", icon = "store", colorHex = "#2196F3", kind = TransactionKind.INCOME),
            Category(profileId = profileId, name = "Freelance", icon = "work", colorHex = "#00BCD4", kind = TransactionKind.INCOME),
            Category(profileId = profileId, name = "Investments", icon = "trending_up", colorHex = "#FF9800", kind = TransactionKind.INCOME),
            Category(profileId = profileId, name = "Gifts", icon = "card_giftcard", colorHex = "#E91E63", kind = TransactionKind.INCOME),
            Category(profileId = profileId, name = "Other Income", icon = "attach_money", colorHex = "#8BC34A", kind = TransactionKind.INCOME)
        )

        categoryRepository.insertAll(expenseCategories + incomeCategories)
    }
}
