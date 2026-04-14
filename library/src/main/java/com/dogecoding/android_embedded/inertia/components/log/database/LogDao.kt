package com.dogecoding.android_embedded.inertia.components.log.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogDbRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LogDbRecord>)

    @Query("SELECT * FROM logs ORDER BY bootId DESC, recordId DESC")
    fun getFullLogs(): Flow<List<LogDbRecord>>

    @Query("SELECT * FROM logs ORDER BY bootId DESC, recordId DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<LogDbRecord>>

    @Query("DELETE FROM logs")
    suspend fun clearLogs()

    @Query("SELECT MAX(recordId) FROM logs")
    suspend fun getMaxEntryId(): Long?

    @Query("SELECT * FROM logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<LogDbRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSync(sync: LogBootSync)

    @Query("SELECT * FROM log_boot_sync WHERE bootId = :bootId")
    suspend fun getSyncForBoot(bootId: Long): LogBootSync?

    @Query("UPDATE logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)
}
