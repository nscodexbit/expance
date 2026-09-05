package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.CategoryDao
import com.yourname.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getCategoriesByProfileAndKind(profileId: Long, kind: String): Flow<List<Category>> =
        categoryDao.getCategoriesByProfileAndKind(profileId, kind)

    fun getCategoriesByProfile(profileId: Long): Flow<List<Category>> =
        categoryDao.getCategoriesByProfile(profileId)

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun insertAll(categories: List<Category>) = categoryDao.insertAll(categories)

    suspend fun update(category: Category) = categoryDao.update(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)

    suspend fun deleteById(id: Long) = categoryDao.deleteById(id)

    suspend fun getCategoryCount(profileId: Long): Int = categoryDao.getCategoryCount(profileId)
}
