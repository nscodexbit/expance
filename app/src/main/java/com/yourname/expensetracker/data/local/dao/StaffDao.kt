package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE profileId = :profileId ORDER BY name ASC")
    fun getStaffByProfile(profileId: Long): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: Long): Staff?

    @Query("SELECT * FROM staff WHERE profileId = :profileId AND pinHash = :pinHash LIMIT 1")
    suspend fun authenticateStaff(profileId: Long, pinHash: String): Staff?

    @Insert
    suspend fun insert(staff: Staff): Long

    @Update
    suspend fun update(staff: Staff)

    @Delete
    suspend fun delete(staff: Staff)

    @Query("DELETE FROM staff WHERE id = :id")
    suspend fun deleteById(id: Long)
}
