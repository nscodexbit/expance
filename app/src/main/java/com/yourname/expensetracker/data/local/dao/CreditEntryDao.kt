package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.CreditEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditEntryDao {
    @Query("SELECT * FROM credit_entries WHERE customerId = :customerId ORDER BY date DESC")
    fun getEntriesByCustomer(customerId: Long): Flow<List<CreditEntry>>

    @Query("SELECT * FROM credit_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): CreditEntry?

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT_GIVEN' THEN amount ELSE 0 END), 0) 
        - COALESCE(SUM(CASE WHEN type = 'PAYMENT_RECEIVED' THEN amount ELSE 0 END), 0) 
        FROM credit_entries WHERE customerId = :customerId
    """)
    fun getOutstandingBalance(customerId: Long): Flow<Double>

    @Insert
    suspend fun insert(entry: CreditEntry): Long

    @Update
    suspend fun update(entry: CreditEntry)

    @Delete
    suspend fun delete(entry: CreditEntry)

    @Query("DELETE FROM credit_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
