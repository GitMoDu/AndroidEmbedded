package com.dogecoding.android_embedded.inertia.components.surface.value_pack.block

import com.dogecoding.android_embedded.inertia.components.surface.value_pack.packs.ValueHelpers.Companion.limitInt
import com.dogecoding.android_embedded.inertia.components.surface.value_pack.packs.ValueHelpers.Companion.limitUInt
import com.dogecoding.android_embedded.inertia.components.surface.value_pack.packs.ValueHelpers.Companion.requireValueIndex

/**
 * Packs 3 21-bit values into 8 bytes.
 * Unsigned: 0 to 2097151
 * Signed: -1048576 to 1048575
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class BlockPack3x21B8 {
    val data = UByteArray(8)

    companion object {
        const val RAW_MAX = 0x1FFFFFu
        const val SIGNED_MIN = -1048576
        const val SIGNED_MAX = 1048575
    }

    protected fun getRaw(valueIndex: Int): UInt {
        requireValueIndex(valueIndex, 2)
        val bitOffset = valueIndex * 21
        return ((getPacked() shr bitOffset) and RAW_MAX.toULong()).toUInt()
    }

    protected fun setRaw(valueIndex: Int, value: UInt) {
        writeRaw(valueIndex, limitUInt(value, 0u, RAW_MAX))
    }

    protected fun getUnsigned(valueIndex: Int): UInt = getRaw(valueIndex)
    protected fun setUnsigned(valueIndex: Int, value: UInt) = setRaw(valueIndex, value)

    protected fun getSigned(valueIndex: Int): Int {
        val raw = getRaw(valueIndex)
        return if ((raw and 0x00100000u) != 0u) (raw or 0xFFE00000u).toInt() else raw.toInt()
    }

    protected fun setSigned(valueIndex: Int, value: Int) {
        val limited = limitInt(value, SIGNED_MIN, SIGNED_MAX)
        setUnsigned(valueIndex, limited.toUInt() and RAW_MAX)
    }

    private fun writeRaw(valueIndex: Int, value: UInt) {
        requireValueIndex(valueIndex, 2)
        val bitOffset = valueIndex * 21
        var packed = getPacked()
        packed = packed and (RAW_MAX.toULong() shl bitOffset).inv()
        packed = packed or (value.toULong() shl bitOffset)
        setPacked(packed)
    }

    protected fun getPacked(): ULong {
        var packed = 0uL
        for (i in 0 until 8) packed = packed or (data[i].toULong() shl (i * 8))
        return packed
    }

    protected fun setPacked(packed: ULong) {
        for (i in 0 until 8) data[i] = ((packed shr (i * 8)) and 0xFFuL).toUByte()
        data[7] = (data[7].toUInt() and 0x7Fu).toUByte()
    }
}
