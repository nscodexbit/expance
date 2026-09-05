package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.BillReminderDao
import com.yourname.expensetracker.data.local.entity.BillReminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillReminderRepository @Inject constructor(
    private val billReminderDao: BillReminderDao
) {
    fun getBills(profileId: Long): Flow<List<BillReminder>> =
        billReminderDao.getBillsByProfile(profileId)

    fun getPendingBills(profileId: Long): Flow<List<BillReminder>> =
        billReminderDao.getPendingBills(profileId)

    suspend fun saveBill(bill: BillReminder): Long =
        billReminderDao.insertBill(bill)

    suspend fun updateBill(bill: BillReminder) =
        billReminderDao.updateBill(bill)

    suspend fun deleteBill(bill: BillReminder) =
        billReminderDao.deleteBill(bill)

    suspend fun setPaidStatus(id: Long, isPaid: Boolean) =
        billReminderDao.setPaidStatus(id, isPaid)
}
