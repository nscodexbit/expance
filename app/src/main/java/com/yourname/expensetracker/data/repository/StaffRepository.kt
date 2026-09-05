package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.StaffDao
import com.yourname.expensetracker.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val staffDao: StaffDao
) {
    fun getStaffByProfile(profileId: Long): Flow<List<Staff>> =
        staffDao.getStaffByProfile(profileId)

    suspend fun getStaffById(id: Long): Staff? = staffDao.getStaffById(id)

    suspend fun authenticate(profileId: Long, pinHash: String): Staff? =
        staffDao.authenticateStaff(profileId, pinHash)

    suspend fun insert(staff: Staff): Long = staffDao.insert(staff)

    suspend fun update(staff: Staff) = staffDao.update(staff)

    suspend fun delete(staff: Staff) = staffDao.delete(staff)

    suspend fun deleteById(id: Long) = staffDao.deleteById(id)
}
