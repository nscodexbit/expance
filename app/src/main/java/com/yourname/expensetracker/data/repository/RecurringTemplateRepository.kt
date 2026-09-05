package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.RecurringTemplateDao
import com.yourname.expensetracker.data.local.entity.RecurringTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTemplateRepository @Inject constructor(
    private val templateDao: RecurringTemplateDao
) {
    fun getTemplatesByProfile(profileId: Long): Flow<List<RecurringTemplate>> =
        templateDao.getTemplatesByProfile(profileId)

    suspend fun getTemplateById(id: Long): RecurringTemplate? = templateDao.getTemplateById(id)

    suspend fun getTemplatesDue(currentDate: Long): List<RecurringTemplate> =
        templateDao.getTemplatesDue(currentDate)

    suspend fun insert(template: RecurringTemplate): Long = templateDao.insert(template)

    suspend fun update(template: RecurringTemplate) = templateDao.update(template)

    suspend fun delete(template: RecurringTemplate) = templateDao.delete(template)

    suspend fun deleteById(id: Long) = templateDao.deleteById(id)
}
