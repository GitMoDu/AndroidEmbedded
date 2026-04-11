package com.dogecoding.android_embedded.inertia.components.log.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LogDbRecord::class, LogBootSync::class],
    version = 1,
    exportSchema = false
)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        private const val DATABASE_NAME = "inertia_log_database"

        @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getDatabase(context: Context): LogDatabase {
            // Using double-checked locking for better performance and thread safety
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    val db = buildDatabase(context)
                    // Trigger a connection to check schema/version integrity immediately.
                    // This ensures the catch block handles schema mismatches even if the version hasn't been bumped.
                    db.openHelper.writableDatabase
                    db
                } catch (e: Exception) {
                    // In case of schema mismatch or catastrophic failure (e.g. file corruption),
                    // delete the database and attempt to reconstruct it.
                    context.deleteDatabase(DATABASE_NAME)
                    buildDatabase(context)
                }.also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(context: Context): LogDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LogDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
