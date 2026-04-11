package com.dogecoding.android_embedded.inertia.surface.value_pack.block

import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.limitShort
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.limitUShort
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.requireValueIndex

/**
 * Packs 4 12-bit values into 6 bytes.
 * Unsigned: 0 to 4095
 * Signed: -2048 to 2047 (Two's complement)
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class BlockPack4x12B6 {
    val data = UByteArray(6)

    companion object {
        const val RAW_MAX = 0x0FFFu
        const val SIGNED_MIN = -2048
        const val SIGNED_MAX = 2047
    }

    protected fun getRaw(valueIndex: Int): UShort {
        requireValueIndex(valueIndex, 3)
        val byteIndex = (valueIndex * 3) / 2
        return if (valueIndex % 2 == 0) {
            ((data[byteIndex].toUInt() shl 4) or (data[byteIndex + 1].toUInt() shr 4)).toUShort()
        } else {
            (((data[byteIndex].toUInt() and 0x0Fu) shl 8) or data[byteIndex + 1].toUInt()).toUShort()
        }
    }

    protected fun setRaw(valueIndex: Int, value: UShort) {
        writeRaw(valueIndex, limitUShort(value, 0u, RAW_MAX.toUShort()))
    }

    protected fun getUnsigned(valueIndex: Int): UShort = getRaw(valueIndex)
    protected fun setUnsigned(valueIndex: Int, value: UShort) = setRaw(valueIndex, value)

    protected fun getSigned(valueIndex: Int): Short {
        val raw = getRaw(valueIndex).toInt()
        return if ((raw and 0x0800) != 0) (raw or 0xF000.toInt()).toShort() else raw.toShort()
    }

    protected fun setSigned(valueIndex: Int, value: Short) {
        val limited = limitShort(value, SIGNED_MIN.toShort(), SIGNED_MAX.toShort())
        setUnsigned(valueIndex, (limited.toInt() and RAW_MAX.toInt()).toUShort())
    }

    private fun writeRaw(valueIndex: Int, value: UShort) {
        requireValueIndex(valueIndex, 3)
        val byteIndex = (valueIndex * 3) / 2
        val packed = value.toUInt()
        if (valueIndex % 2 == 0) {
            data[byteIndex] = (packed shr 4).toUByte()
            data[byteIndex + 1] =
                ((data[byteIndex + 1].toUInt() and 0x0Fu) or ((packed and 0x000Fu) shl 4)).toUByte()
        } else {
            data[byteIndex] =
                ((data[byteIndex].toUInt() and 0xF0u) or ((packed shr 8) and 0x0Fu)).toUByte()
            data[byteIndex + 1] = (packed and 0x00FFu).toUByte()
        }
    }
}