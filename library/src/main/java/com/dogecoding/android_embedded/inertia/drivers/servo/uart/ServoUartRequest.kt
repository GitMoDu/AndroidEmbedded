package com.dogecoding.android_embedded.inertia.drivers.servo.uart

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ServoUartRequest(
    val index: Byte,
    val enabled: Boolean,
    val pulseNs: Int
) {
    companion object {
        const val MAGIC_BYTE: Byte = 'S'.code.toByte()
        const val PACKET_SIZE = 7

        @OptIn(ExperimentalUnsignedTypes::class)
        fun fromByteArray(data: ByteArray): ServoUartRequest? {
            if (data.size < PACKET_SIZE) return null
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val magic = buffer.get()
            if (magic != MAGIC_BYTE) return null

            val index = buffer.get()
            val enabled = buffer.get() != 0.toByte()
            val pulseNs = buffer.getInt()

            return ServoUartRequest(index, enabled, pulseNs)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(PACKET_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(MAGIC_BYTE)
            put(index)
            put(if (enabled) 1.toByte() else 0.toByte())
            putInt(pulseNs)
        }.array()
    }
}
