package com.dogecoding.android_embedded.inertia.surface.value_pack

import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.FactorScale32
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.limitInt
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.limitShort
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.limitUShort
import com.dogecoding.android_embedded.inertia.surface.value_pack.ValueHelpers.Companion.requireValueIndex
import com.dogecoding.android_embedded.inertia.surface.value_pack.block.BlockPack4x12B6


/**
 * Implementations of the different codec variants for 4x12 packing.
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class ValuePack4x12B6 : BlockPack4x12B6() {
    // Limited Variants
    protected fun getUnsignedLimited(valueIndex: Int): UShort =
        limitUShort(getRaw(valueIndex), 0u, RAW_MAX.toUShort())

    protected fun setUnsignedLimited(valueIndex: Int, value: UShort) =
        setRaw(valueIndex, limitUShort(value, 0u, RAW_MAX.toUShort()))

    protected fun getSignedLimited(valueIndex: Int): Short =
        signExtend12(limitUShort(getRaw(valueIndex), 0u, RAW_MAX.toUShort()))

    protected fun setSignedLimited(valueIndex: Int, value: Short) =
        setRaw(
            valueIndex,
            (limitShort(
                value,
                SIGNED_MIN.toShort(),
                SIGNED_MAX.toShort()
            ).toInt() and RAW_MAX.toInt()).toUShort(),
        )

    // Masked Variants
    protected fun getUnsignedMasked(valueIndex: Int): UShort = getRaw(valueIndex)
    protected fun setUnsignedMasked(valueIndex: Int, value: UShort) =
        writeMaskedRaw(valueIndex, value.toUInt().and(RAW_MAX).toUShort())

    protected fun getSignedMasked(valueIndex: Int): Short = signExtend12(getRaw(valueIndex))
    protected fun setSignedMasked(valueIndex: Int, value: Short) =
        writeMaskedRaw(valueIndex, (value.toInt() and RAW_MAX.toInt()).toUShort())

    // Scaled Variants
    protected fun getUnsignedScaled(valueIndex: Int, minVal: Int, maxVal: Int): Int {
        require(maxVal > minVal) { "Scaled value packs require MaxValue > MinValue." }
        val valueRange = (maxVal - minVal).toUInt()
        val decompressScale = FactorScale32.getFactor(valueRange, RAW_MAX)
        val limited = limitUShort(getRaw(valueIndex), 0u, RAW_MAX.toUShort())
        return minVal + FactorScale32.scale(decompressScale, limited.toUInt()).toInt()
    }

    protected fun setUnsignedScaled(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) {
        require(maxVal > minVal) { "Scaled value packs require MaxValue > MinValue." }
        val limited = limitInt(value, minVal, maxVal)
        val valueRange = (maxVal - minVal).toUInt()
        val compressScale = FactorScale32.getFactor(RAW_MAX, valueRange)
        val normalized = (limited - minVal).toUInt()
        setRaw(valueIndex, FactorScale32.scale(compressScale, normalized).toUShort())
    }

    protected fun getSignedScaled(valueIndex: Int, minVal: Int, maxVal: Int): Int =
        getUnsignedScaled(valueIndex, minVal, maxVal)

    protected fun setSignedScaled(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) =
        setUnsignedScaled(valueIndex, value, minVal, maxVal)

    private fun signExtend12(value: UShort): Short {
        val raw = value.toInt()
        return if ((raw and 0x0800) != 0) (raw or 0xF000.toInt()).toShort() else raw.toShort()
    }

    private fun writeMaskedRaw(valueIndex: Int, value: UShort) {
        val byteIndex = (valueIndex * 3) / 2
        val packed = value.toUInt()
        requireValueIndex(valueIndex, 3)
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