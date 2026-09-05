package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.BudgetDao
import com.yourname.expensetracker.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getBudgetsByProfile(profileId: Long): Flow<List<Budget>> =
        budgetDao.getBudgetsByProfile(profileId)

    suspend fun getBudgetById(id: Long): Budget? = budgetDao.getBudgetById(id)

    suspend fun getBudgetForCategory(profileId: Long, categoryId: Long): Budget? =
        budgetDao.getBudgetForCategory(profileId, categoryId)

    suspend fun insert(budget: Budget): Long = budgetDao.insert(budget)

    suspend fun update(budget: Budget) = budgetDao.update(budget)

    suspend fun delete(budget: Budget) = budgetDao.delete(budget)

    suspend fun deleteById(id: Long) = budgetDao.deleteById(id)
}
