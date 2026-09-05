package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.SavingsGoalDao
import com.yourname.expensetracker.data.local.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsGoalRepository @Inject constructor(
    private val goalDao: SavingsGoalDao
) {
    fun getGoalsByProfile(profileId: Long): Flow<List<SavingsGoal>> =
        goalDao.getGoalsByProfile(profileId)

    suspend fun getGoalById(id: Long): SavingsGoal? = goalDao.getGoalById(id)

    suspend fun insert(goal: SavingsGoal): Long = goalDao.insert(goal)

    suspend fun update(goal: SavingsGoal) = goalDao.update(goal)

    suspend fun delete(goal: SavingsGoal) = goalDao.delete(goal)

    suspend fun deleteById(id: Long) = goalDao.deleteById(id)
}
