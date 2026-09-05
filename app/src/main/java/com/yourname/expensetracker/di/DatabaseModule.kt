package com.yourname.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.yourname.expensetracker.data.local.ExpenseDatabase
import com.yourname.expensetracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            ExpenseDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideProfileDao(db: ExpenseDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideAccountDao(db: ExpenseDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: ExpenseDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: ExpenseDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideRecurringTemplateDao(db: ExpenseDatabase): RecurringTemplateDao = db.recurringTemplateDao()

    @Provides
    fun provideBudgetDao(db: ExpenseDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideSavingsGoalDao(db: ExpenseDatabase): SavingsGoalDao = db.savingsGoalDao()

    @Provides
    fun provideTagDao(db: ExpenseDatabase): TagDao = db.tagDao()

    @Provides
    fun provideTransactionTagDao(db: ExpenseDatabase): TransactionTagDao = db.transactionTagDao()

    @Provides
    fun provideSplitParticipantDao(db: ExpenseDatabase): SplitParticipantDao = db.splitParticipantDao()

    @Provides
    fun provideCustomerDao(db: ExpenseDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideCreditEntryDao(db: ExpenseDatabase): CreditEntryDao = db.creditEntryDao()

    @Provides
    fun provideSupplierDao(db: ExpenseDatabase): SupplierDao = db.supplierDao()

    @Provides
    fun provideSupplierPaymentDao(db: ExpenseDatabase): SupplierPaymentDao = db.supplierPaymentDao()

    @Provides
    fun provideStaffDao(db: ExpenseDatabase): StaffDao = db.staffDao()

    @Provides
    fun provideBackupLogDao(db: ExpenseDatabase): BackupLogDao = db.backupLogDao()

    @Provides
    fun provideInvoiceDao(db: ExpenseDatabase): InvoiceDao = db.invoiceDao()

    @Provides
    fun provideInventoryDao(db: ExpenseDatabase): InventoryDao = db.inventoryDao()

    @Provides
    fun provideBillReminderDao(db: ExpenseDatabase): BillReminderDao = db.billReminderDao()

    @Provides
    fun provideExpenseSplitDao(db: ExpenseDatabase): ExpenseSplitDao = db.expenseSplitDao()

    @Provides
    fun provideDailyClosingDao(db: ExpenseDatabase): DailyClosingDao = db.dailyClosingDao()
}
