package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.ExpenseSplit
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseSplitDao {

    @Query("SELECT * FROM expense_splits WHERE profileId = :profileId ORDER BY date DESC")
    fun getSplitsByProfile(profileId: Long): Flow<List<ExpenseSplit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: ExpenseSplit): Long

    @Update
    suspend fun updateSplit(split: ExpenseSplit)

    @Delete
    suspend fun deleteSplit(split: ExpenseSplit)

    @Query("UPDATE expense_splits SET isSettled = :isSettled WHERE id = :id")
    suspend fun setSettledStatus(id: Long, isSettled: Boolean)
}
