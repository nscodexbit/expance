package com.yourname.expensetracker.data.local.dao

import androidx.room.*
import com.yourname.expensetracker.data.local.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierPaymentDao {
    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPaymentsBySupplier(supplierId: Long): Flow<List<SupplierPayment>>

    @Query("SELECT * FROM supplier_payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): SupplierPayment?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM supplier_payments WHERE supplierId = :supplierId AND date BETWEEN :startDate AND :endDate")
    fun getTotalPayments(supplierId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(sp.amount), 0.0) FROM supplier_payments sp
        INNER JOIN suppliers s ON sp.supplierId = s.id
        WHERE s.profileId = :profileId AND sp.date BETWEEN :startDate AND :endDate
    """)
    fun getTotalPaymentsByProfile(profileId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Insert
    suspend fun insert(payment: SupplierPayment): Long

    @Update
    suspend fun update(payment: SupplierPayment)

    @Delete
    suspend fun delete(payment: SupplierPayment)

    @Query("DELETE FROM supplier_payments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
