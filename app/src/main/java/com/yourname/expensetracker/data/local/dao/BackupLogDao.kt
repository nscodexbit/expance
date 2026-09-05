package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.BackupLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupLogDao {
    @Query("SELECT * FROM backup_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BackupLog>>

    @Query("SELECT * FROM backup_logs WHERE id = :id")
    suspend fun getLogById(id: Long): BackupLog?

    @Insert
    suspend fun insert(log: BackupLog): Long

    @Delete
    suspend fun delete(log: BackupLog)

    @Query("DELETE FROM backup_logs")
    suspend fun deleteAll()
}
