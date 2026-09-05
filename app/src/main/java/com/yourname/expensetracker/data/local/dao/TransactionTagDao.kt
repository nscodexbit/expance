package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.TransactionTag

@Dao
interface TransactionTagDao {
    @Query("SELECT * FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun getTagsForTransaction(transactionId: Long): List<TransactionTag>

    @Insert
    suspend fun insert(transactionTag: TransactionTag)

    @Insert
    suspend fun insertAll(transactionTags: List<TransactionTag>)

    @Delete
    suspend fun delete(transactionTag: TransactionTag)

    @Query("DELETE FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun deleteAllForTransaction(transactionId: Long)

    @Query("SELECT * FROM transaction_tags WHERE tagId = :tagId")
    suspend fun getTransactionsWithTag(tagId: Long): List<TransactionTag>
}
