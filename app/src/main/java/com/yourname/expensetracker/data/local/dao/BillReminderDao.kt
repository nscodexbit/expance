package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.BillReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {

    @Query("SELECT * FROM bill_reminders WHERE profileId = :profileId ORDER BY dueDate ASC")
    fun getBillsByProfile(profileId: Long): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE profileId = :profileId AND isPaid = 0 ORDER BY dueDate ASC")
    fun getPendingBills(profileId: Long): Flow<List<BillReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillReminder): Long

    @Update
    suspend fun updateBill(bill: BillReminder)

    @Delete
    suspend fun deleteBill(bill: BillReminder)

    @Query("UPDATE bill_reminders SET isPaid = :isPaid WHERE id = :id")
    suspend fun setPaidStatus(id: Long, isPaid: Boolean)
}
