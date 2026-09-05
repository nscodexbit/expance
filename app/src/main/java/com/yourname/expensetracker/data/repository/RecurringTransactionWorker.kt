package com.yourname.expensetracker.data.repository

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourname.expensetracker.data.local.dao.RecurringTemplateDao
import com.yourname.expensetracker.data.local.dao.TransactionDao
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val templateDao: RecurringTemplateDao,
    private val transactionDao: TransactionDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val dueTemplates = templateDao.getTemplatesDue(currentTime)

            for (template in dueTemplates) {
                val transaction = Transaction(
                    profileId = template.profileId,
                    accountId = template.accountId,
                    categoryId = template.categoryId,
                    type = TransactionType.EXPENSE,
                    amount = template.amount,
                    date = currentTime,
                    note = template.label,
                    recurringTemplateId = template.id
                )
                transactionDao.insert(transaction)

                val nextDueDate = when (template.frequency) {
                    com.yourname.expensetracker.data.local.entity.Frequency.DAILY ->
                        currentTime + 86_400_000L
                    com.yourname.expensetracker.data.local.entity.Frequency.WEEKLY ->
                        currentTime + 604_800_000L
                    com.yourname.expensetracker.data.local.entity.Frequency.MONTHLY ->
                        currentTime + 2_592_000_000L
                }
                templateDao.update(template.copy(nextDueDate = nextDueDate))
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
