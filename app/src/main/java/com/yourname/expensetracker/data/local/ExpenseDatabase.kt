package com.yourname.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourname.expensetracker.data.local.dao.*
import com.yourname.expensetracker.data.local.entity.*

@Database(
    entities = [
        Profile::class,
        Account::class,
        Category::class,
        Transaction::class,
        RecurringTemplate::class,
        Budget::class,
        SavingsGoal::class,
        Tag::class,
        TransactionTag::class,
        SplitParticipant::class,
        Customer::class,
        CreditEntry::class,
        Supplier::class,
        SupplierPayment::class,
        Staff::class,
        BackupLog::class,
        Invoice::class,
        InvoiceItem::class,
        InventoryItem::class,
        BillReminder::class,
        ExpenseSplit::class,
        DailyClosing::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagDao(): TransactionTagDao
    abstract fun splitParticipantDao(): SplitParticipantDao
    abstract fun customerDao(): CustomerDao
    abstract fun creditEntryDao(): CreditEntryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierPaymentDao(): SupplierPaymentDao
    abstract fun staffDao(): StaffDao
    abstract fun backupLogDao(): BackupLogDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
    abstract fun dailyClosingDao(): DailyClosingDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
    }
}
