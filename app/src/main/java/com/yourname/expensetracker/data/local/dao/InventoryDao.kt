package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items WHERE profileId = :profileId ORDER BY name ASC")
    fun getItemsByProfile(profileId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE profileId = :profileId AND currentStock <= minStockAlert")
    fun getLowStockItems(profileId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE profileId = :profileId AND barcode = :barcode LIMIT 1")
    suspend fun getItemByBarcode(profileId: Long, barcode: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET currentStock = currentStock + :delta, updatedAt = :timestamp WHERE id = :id")
    suspend fun adjustStock(id: Long, delta: Double, timestamp: Long = System.currentTimeMillis())
}
