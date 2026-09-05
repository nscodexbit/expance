package com.yourname.expensetracker.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yourname.expensetracker.data.repository.RecurringTransactionWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    fun scheduleRecurringTransactions(workManager: WorkManager) {
        val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "recurring_transactions",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
