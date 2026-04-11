package com.dogecoding.android_embedded.inertia.log.model


import com.dogecoding.android_core.extension.getUIntLe

@OptIn(ExperimentalUnsignedTypes::class)
data class LogEntry(
    val tag: UInt,
    val instance: UByte,
    val type: LogType,
    val code: UByte,
    val value: UByte
) {
    companion object {
        const val SIZE = 8
        fun fromBinary(bytes: UByteArray, offset: Int = 0): LogEntry {
            return LogEntry(
                tag = bytes.getUIntLe(offset),
                instance = bytes[offset + 4],
                type = LogType.fromUByte(bytes[offset + 5]),
                code = bytes[offset + 6],
                value = bytes[offset + 7]
            )
        }
    }
}

