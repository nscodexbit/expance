package com.yourname.expensetracker.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.yourname.expensetracker.data.local.ExpenseDatabase
import com.yourname.expensetracker.data.local.dao.BackupLogDao
import com.yourname.expensetracker.data.local.entity.BackupLog
import com.yourname.expensetracker.data.local.entity.BackupStatus
import com.yourname.expensetracker.data.local.entity.BackupType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: ExpenseDatabase,
    private val backupLogDao: BackupLogDao,
    @ApplicationContext private val context: Context
) {

    fun getBackupLogs(): Flow<List<BackupLog>> = backupLogDao.getAllLogs()

    suspend fun createLocalBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 2)
            root.put("appName", "ExpenseTracker")
            root.put("timestamp", System.currentTimeMillis())

            // Export transactions
            val txnsArray = JSONArray()
            val txns = database.transactionDao().getAllActiveTransactions()
            for (t in txns) {
                val obj = JSONObject()
                obj.put("profileId", t.profileId)
                obj.put("accountId", t.accountId)
                obj.put("categoryId", t.categoryId)
                obj.put("type", t.type.name)
                obj.put("amount", t.amount)
                obj.put("date", t.date)
                obj.put("note", t.note ?: "")
                txnsArray.put(obj)
            }
            root.put("transactions", txnsArray)

            // Export customers
            val customersArray = JSONArray()
            val customers = database.customerDao().getAllCustomersDirect()
            for (c in customers) {
                val obj = JSONObject()
                obj.put("profileId", c.profileId)
                obj.put("name", c.name)
                obj.put("phone", c.phone ?: "")
                obj.put("address", c.address ?: "")
                customersArray.put(obj)
            }
            root.put("customers", customersArray)

            // Export suppliers
            val suppliersArray = JSONArray()
            val suppliers = database.supplierDao().getAllSuppliersDirect()
            for (s in suppliers) {
                val obj = JSONObject()
                obj.put("profileId", s.profileId)
                obj.put("name", s.name)
                obj.put("phone", s.phone ?: "")
                suppliersArray.put(obj)
            }
            root.put("suppliers", suppliersArray)

            // Write to file
            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.cacheDir, "expense_tracker_backup_$timestampStr.json")
            file.writeText(root.toString(2))

            backupLogDao.insert(
                BackupLog(
                    timestamp = System.currentTimeMillis(),
                    type = BackupType.LOCAL,
                    status = BackupStatus.SUCCESS
                )
            )
            Result.success(file)
        } catch (e: Exception) {
            backupLogDao.insert(
                BackupLog(
                    timestamp = System.currentTimeMillis(),
                    type = BackupType.LOCAL,
                    status = BackupStatus.FAILED
                )
            )
            Result.failure(e)
        }
    }

    suspend fun restoreFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var count = 0

            // Restore transactions
            val txnsArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txnsArray.length()) {
                val obj = txnsArray.getJSONObject(i)
                val txn = com.yourname.expensetracker.data.local.entity.Transaction(
                    profileId = obj.optLong("profileId", 1L),
                    accountId = obj.optLong("accountId", 1L),
                    categoryId = if (obj.isNull("categoryId")) null else obj.optLong("categoryId"),
                    type = com.yourname.expensetracker.data.local.entity.TransactionType.valueOf(obj.getString("type")),
                    amount = obj.getDouble("amount"),
                    date = obj.getLong("date"),
                    note = obj.optString("note").ifBlank { null }
                )
                database.transactionDao().insert(txn)
                count++
            }

            // Restore customers
            val customersArray = root.optJSONArray("customers")
            if (customersArray != null) {
                for (i in 0 until customersArray.length()) {
                    val obj = customersArray.getJSONObject(i)
                    val customer = com.yourname.expensetracker.data.local.entity.Customer(
                        profileId = obj.optLong("profileId", 1L),
                        name = obj.getString("name"),
                        phone = obj.optString("phone").ifBlank { null },
                        address = obj.optString("address").ifBlank { null }
                    )
                    database.customerDao().insert(customer)
                    count++
                }
            }

            // Restore suppliers
            val suppliersArray = root.optJSONArray("suppliers")
            if (suppliersArray != null) {
                for (i in 0 until suppliersArray.length()) {
                    val obj = suppliersArray.getJSONObject(i)
                    val supplier = com.yourname.expensetracker.data.local.entity.Supplier(
                        profileId = obj.optLong("profileId", 1L),
                        name = obj.getString("name"),
                        phone = obj.optString("phone").ifBlank { null }
                    )
                    database.supplierDao().insert(supplier)
                    count++
                }
            }

            backupLogDao.insert(
                BackupLog(
                    timestamp = System.currentTimeMillis(),
                    type = BackupType.LOCAL,
                    status = BackupStatus.SUCCESS
                )
            )
            Result.success(count)
        } catch (e: Exception) {
            backupLogDao.insert(
                BackupLog(
                    timestamp = System.currentTimeMillis(),
                    type = BackupType.LOCAL,
                    status = BackupStatus.FAILED
                )
            )
            Result.failure(e)
        }
    }
}
