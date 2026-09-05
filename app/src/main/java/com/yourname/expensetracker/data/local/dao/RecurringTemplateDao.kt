package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.RecurringTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTemplateDao {
    @Query("SELECT * FROM recurring_templates WHERE profileId = :profileId ORDER BY nextDueDate ASC")
    fun getTemplatesByProfile(profileId: Long): Flow<List<RecurringTemplate>>

    @Query("SELECT * FROM recurring_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): RecurringTemplate?

    @Query("SELECT * FROM recurring_templates WHERE nextDueDate <= :currentDate")
    suspend fun getTemplatesDue(currentDate: Long): List<RecurringTemplate>

    @Insert
    suspend fun insert(template: RecurringTemplate): Long

    @Update
    suspend fun update(template: RecurringTemplate)

    @Delete
    suspend fun delete(template: RecurringTemplate)

    @Query("DELETE FROM recurring_templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
