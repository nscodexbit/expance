package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.TransactionDao
import com.yourname.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getTransactionsByProfile(profileId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByProfile(profileId)

    fun getTransactionsByDateRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(profileId, startDate, endDate)

    fun getTransactionsByCategory(profileId: Long, categoryId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategory(profileId, categoryId)

    fun searchTransactions(profileId: Long, query: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(profileId, query)

    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)

    suspend fun findDuplicate(profileId: Long, accountId: Long, categoryId: Long?, amount: Double, startTime: Long, endTime: Long): Transaction? =
        transactionDao.findDuplicate(profileId, accountId, categoryId, amount, startTime, endTime)

    fun getTotalByTypeAndDateRange(profileId: Long, type: String, startDate: Long, endDate: Long): Flow<Double?> =
        transactionDao.getTotalByTypeAndDateRange(profileId, type, startDate, endDate)

    fun getCategoryTotal(profileId: Long, categoryId: Long, startDate: Long, endDate: Long): Flow<Double?> =
        transactionDao.getCategoryTotal(profileId, categoryId, startDate, endDate)

    suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    suspend fun softDelete(id: Long) = transactionDao.softDelete(id)

    suspend fun undoDelete(id: Long) = transactionDao.undoDelete(id)

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)
}
