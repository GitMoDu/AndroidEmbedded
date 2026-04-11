package com.dogecoding.android_embedded.inertia.log.model

import com.dogecoding.android_core.extension.getUIntLe
import com.dogecoding.android_core.extension.getUShortLe
import com.dogecoding.android_embedded.uart_interface.codec.Fletcher16

@OptIn(ExperimentalUnsignedTypes::class)
data class LogRecord(
    val entry: LogEntry,
    val recordId: UInt,
    val bootId: UInt,
    val timestampMillis: UInt,
    val timestampOverflows: UShort,
    val crc: UShort
) {
    /**
     * Returns the full 64-bit timestamp in milliseconds.
     */
    val fullTimestamp: ULong
        get() = (timestampOverflows.toULong() * 0x100000000uL) + timestampMillis.toULong()

    companion object {
        const val SIZE = 24

        fun fromBinary(bytes: UByteArray): LogRecord {
            if (bytes.size < SIZE) throw IllegalArgumentException("Buffer too small (expected $SIZE, got ${bytes.size})")
            
            val record = LogRecord(
                entry = LogEntry.fromBinary(bytes, 0),
                recordId = bytes.getUIntLe(8),
                bootId = bytes.getUIntLe(12),
                timestampMillis = bytes.getUIntLe(16),
                timestampOverflows = bytes.getUShortLe(20),
                crc = bytes.getUShortLe(22)
            )

            return record
        }

        fun verifyCrc(bytes: UByteArray): Boolean {
            if (bytes.size < SIZE) return false
            val fletcher = Fletcher16()
            fletcher.begin()
            // Calculate over the first 22 bytes (everything except the CRC field itself)
            fletcher.add(bytes, 22, 0)
            val calculated = fletcher.getFletcher()
            val expected = bytes.getUShortLe(22)
            return calculated == expected
        }
    }
}
