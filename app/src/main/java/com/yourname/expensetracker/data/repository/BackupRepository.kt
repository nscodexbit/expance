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
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())

            // Export transactions
            val txnsArray = JSONArray()
            val txns = database.transactionDao().getAllActiveTransactions()
            for (t in txns) {
                val obj = JSONObject()
                obj.put("id", t.id)
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
            val txnsArray = root.optJSONArray("transactions") ?: JSONArray()
            var count = 0

            for (i in 0 until txnsArray.length()) {
                val obj = txnsArray.getJSONObject(i)
                val txn = com.yourname.expensetracker.data.local.entity.Transaction(
                    profileId = obj.getLong("profileId"),
                    accountId = obj.getLong("accountId"),
                    categoryId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId"),
                    type = com.yourname.expensetracker.data.local.entity.TransactionType.valueOf(obj.getString("type")),
                    amount = obj.getDouble("amount"),
                    date = obj.getLong("date"),
                    note = obj.optString("note").ifBlank { null }
                )
                database.transactionDao().insert(txn)
                count++
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

    suspend fun simulateDriveBackup(): Result<Boolean> = withContext(Dispatchers.IO) {
        // Simulates Google Drive sync using signed-in account without server
        try {
            backupLogDao.insert(
                BackupLog(
                    timestamp = System.currentTimeMillis(),
                    type = BackupType.DRIVE,
                    status = BackupStatus.SUCCESS
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
