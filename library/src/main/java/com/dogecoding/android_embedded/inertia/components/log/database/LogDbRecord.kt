package com.dogecoding.android_embedded.inertia.components.log.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs",
    indices = [Index(value = ["bootId", "recordId"], unique = true)]
)
data class LogDbRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Core identifiers (uint32_t -> Long)
    val bootId: Long,
    val recordId: Long,
    val crc: Int, // Fletcher-16 checksum (uint16_t)

    // Timing Puzzle Pieces
    val dumpUtc: Long,      // Client RTC when this log batch was received
    val dumpUptime: Long,   // Device uptime when this log batch was sent ("paired boot up time")

    // Raw record uptime (from millis_timestamp_t)
    val uptimeMillis: Long, // Full 64-bit uptime in ms
    val overflows: Int,     // Legacy/Raw field (kept for schema compatibility if needed, but we'll use full uptime)

    // Log Data (LogEntryStruct)
    val tag: Long,          // uint32_t
    val instance: Int,      // uint8_t
    val type: Int,          // LogTypeEnum (uint8_t)
    val code: Int,          // uint8_t
    val value: Int,         // uint8_t

    val isSynced: Boolean = false
) {
    /**
     * Reconstructs the full uptime in milliseconds as defined by the firmware:
     * (overflows * UINT32_MAX) + timestamp
     */
    fun getFullUptime(): Long {
        return (overflows.toLong() * 0x100000000L) + uptimeMillis
    }

    /**
     * Infers the UTC time of this specific record using the dump sync point.
     * Record UTC = dumpUtc - (dumpUptime - recordUptime)
     */
    fun getInferredUtc(): Long {
        val recordUptime = uptimeMillis
        val diff = dumpUptime - recordUptime
        val inferred = dumpUtc - diff

        // Debug logging to catch the 19-hour jump
        // Log.d("LogDbRecord", "Inferred UTC: $inferred (DumpUTC: $dumpUtc, DumpUptime: $dumpUptime, RecordUptime: $recordUptime, Diff: $diff)")

        return inferred
    }
}
