package com.dogecoding.android_embedded.inertia.components.log.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Maps a specific boot session to a known UTC sync point.
 */
@Entity(tableName = "log_boot_sync")
data class LogBootSync(
    @PrimaryKey val bootId: Long,
    val syncUtc: Long,      // Client UTC at sync
    val syncUptime: Long    // Device uptime at sync
)
