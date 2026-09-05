package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE profileId = :profileId ORDER BY categoryId ASC")
    fun getBudgetsByProfile(profileId: Long): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?

    @Query("SELECT * FROM budgets WHERE profileId = :profileId AND categoryId = :categoryId")
    suspend fun getBudgetForCategory(profileId: Long, categoryId: Long): Budget?

    @Insert
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
