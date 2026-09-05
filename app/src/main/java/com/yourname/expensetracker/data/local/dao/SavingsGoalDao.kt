package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE profileId = :profileId ORDER BY id DESC")
    fun getGoalsByProfile(profileId: Long): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoal?

    @Insert
    suspend fun insert(goal: SavingsGoal): Long

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
