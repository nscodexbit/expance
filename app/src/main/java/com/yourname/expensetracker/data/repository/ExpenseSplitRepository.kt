package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.ExpenseSplitDao
import com.yourname.expensetracker.data.local.entity.ExpenseSplit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseSplitRepository @Inject constructor(
    private val expenseSplitDao: ExpenseSplitDao
) {
    fun getSplits(profileId: Long): Flow<List<ExpenseSplit>> =
        expenseSplitDao.getSplitsByProfile(profileId)

    suspend fun saveSplit(split: ExpenseSplit): Long =
        expenseSplitDao.insertSplit(split)

    suspend fun updateSplit(split: ExpenseSplit) =
        expenseSplitDao.updateSplit(split)

    suspend fun deleteSplit(split: ExpenseSplit) =
        expenseSplitDao.deleteSplit(split)

    suspend fun setSettledStatus(id: Long, isSettled: Boolean) =
        expenseSplitDao.setSettledStatus(id, isSettled)
}
