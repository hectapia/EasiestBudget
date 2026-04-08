package com.easybudget.easiestbudget.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.easybudget.easiestbudget.models.Budget
import com.easybudget.easiestbudget.models.Expense
import com.easybudget.easiestbudget.models.User

/**
 * The main Room database class for the application.
 * Defines the entities included in the database and the DAO used to access them.
 */
@Database(
    entities = [User::class, Budget::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provides access to the Data Access Object (DAO). */
    abstract fun appDao(): AppDao

    companion object {
        // Singleton instance to prevent multiple instances of the database being opened simultaneously
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database.
         * Creates a new instance if one doesn't exist.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easiest_budget_db"
                )
                    // Enables Write-Ahead Logging for better performance and concurrency
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}