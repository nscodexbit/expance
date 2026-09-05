package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.InventoryDao
import com.yourname.expensetracker.data.local.entity.InventoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    fun getItems(profileId: Long): Flow<List<InventoryItem>> =
        inventoryDao.getItemsByProfile(profileId)

    fun getLowStockItems(profileId: Long): Flow<List<InventoryItem>> =
        inventoryDao.getLowStockItems(profileId)

    suspend fun getItemById(id: Long): InventoryItem? =
        inventoryDao.getItemById(id)

    suspend fun getItemByBarcode(profileId: Long, barcode: String): InventoryItem? =
        inventoryDao.getItemByBarcode(profileId, barcode)

    suspend fun saveItem(item: InventoryItem): Long =
        inventoryDao.insertItem(item)

    suspend fun updateItem(item: InventoryItem) =
        inventoryDao.updateItem(item)

    suspend fun deleteItem(item: InventoryItem) =
        inventoryDao.deleteItem(item)

    suspend fun adjustStock(id: Long, delta: Double) =
        inventoryDao.adjustStock(id, delta)
}
