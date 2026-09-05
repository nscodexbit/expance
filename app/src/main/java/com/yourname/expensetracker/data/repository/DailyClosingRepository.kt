package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.DailyClosingDao
import com.yourname.expensetracker.data.local.entity.DailyClosing
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyClosingRepository @Inject constructor(
    private val dailyClosingDao: DailyClosingDao
) {
    fun getClosings(profileId: Long): Flow<List<DailyClosing>> =
        dailyClosingDao.getClosingsByProfile(profileId)

    suspend fun saveClosing(closing: DailyClosing): Long =
        dailyClosingDao.insertClosing(closing)

    suspend fun deleteClosing(closing: DailyClosing) =
        dailyClosingDao.deleteClosing(closing)
}
