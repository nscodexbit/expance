package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.SupplierDao
import com.yourname.expensetracker.data.local.dao.SupplierPaymentDao
import com.yourname.expensetracker.data.local.entity.Supplier
import com.yourname.expensetracker.data.local.entity.SupplierPayment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao,
    private val supplierPaymentDao: SupplierPaymentDao
) {
    fun getSuppliersByProfile(profileId: Long): Flow<List<Supplier>> =
        supplierDao.getSuppliersByProfile(profileId)

    suspend fun getSupplierById(id: Long): Supplier? = supplierDao.getSupplierById(id)

    suspend fun insert(supplier: Supplier): Long = supplierDao.insert(supplier)

    suspend fun update(supplier: Supplier) = supplierDao.update(supplier)

    suspend fun delete(supplier: Supplier) = supplierDao.delete(supplier)

    fun getPaymentsBySupplier(supplierId: Long): Flow<List<SupplierPayment>> =
        supplierPaymentDao.getPaymentsBySupplier(supplierId)

    fun getTotalPaymentsByProfile(profileId: Long, startDate: Long, endDate: Long): Flow<Double> =
        supplierPaymentDao.getTotalPaymentsByProfile(profileId, startDate, endDate)

    suspend fun insertPayment(payment: SupplierPayment): Long = supplierPaymentDao.insert(payment)

    suspend fun updatePayment(payment: SupplierPayment) = supplierPaymentDao.update(payment)

    suspend fun deletePayment(payment: SupplierPayment) = supplierPaymentDao.delete(payment)
}
