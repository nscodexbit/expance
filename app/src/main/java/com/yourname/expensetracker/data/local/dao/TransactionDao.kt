package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getTransactionsByProfile(profileId: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 AND categoryId = :categoryId 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(profileId: Long, categoryId: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 AND type = :type 
        ORDER BY date DESC
    """)
    fun getTransactionsByType(profileId: Long, type: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 
        AND (note LIKE '%' || :query || '%' OR amount LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun searchTransactions(profileId: Long, query: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE isDeleted = 0")
    suspend fun getAllActiveTransactions(): List<Transaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE profileId = :profileId AND isDeleted = 0 
        AND accountId = :accountId AND categoryId = :categoryId AND amount = :amount
        AND date BETWEEN :startTime AND :endTime
        LIMIT 1
    """)
    suspend fun findDuplicate(
        profileId: Long,
        accountId: Long,
        categoryId: Long?,
        amount: Double,
        startTime: Long,
        endTime: Long
    ): Transaction?

    @Query("SELECT SUM(amount) FROM transactions WHERE profileId = :profileId AND isDeleted = 0 AND type = :type AND date BETWEEN :startDate AND :endDate")
    fun getTotalByTypeAndDateRange(profileId: Long, type: String, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE profileId = :profileId AND isDeleted = 0 AND categoryId = :categoryId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getCategoryTotal(profileId: Long, categoryId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("UPDATE transactions SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun undoDelete(id: Long)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
