package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.DailyClosing
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyClosingDao {

    @Query("SELECT * FROM daily_closings WHERE profileId = :profileId ORDER BY date DESC")
    fun getClosingsByProfile(profileId: Long): Flow<List<DailyClosing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosing(closing: DailyClosing): Long

    @Delete
    suspend fun deleteClosing(closing: DailyClosing)
}
