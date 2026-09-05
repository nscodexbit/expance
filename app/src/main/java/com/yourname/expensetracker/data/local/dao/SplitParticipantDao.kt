package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.SplitParticipant
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitParticipantDao {
    @Query("SELECT * FROM split_participants WHERE transactionId = :transactionId")
    fun getParticipantsByTransaction(transactionId: Long): Flow<List<SplitParticipant>>

    @Query("SELECT * FROM split_participants WHERE id = :id")
    suspend fun getParticipantById(id: Long): SplitParticipant?

    @Insert
    suspend fun insert(participant: SplitParticipant): Long

    @Insert
    suspend fun insertAll(participants: List<SplitParticipant>)

    @Update
    suspend fun update(participant: SplitParticipant)

    @Delete
    suspend fun delete(participant: SplitParticipant)

    @Query("DELETE FROM split_participants WHERE transactionId = :transactionId")
    suspend fun deleteAllForTransaction(transactionId: Long)
}
