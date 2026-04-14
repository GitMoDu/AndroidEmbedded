package com.dogecoding.android_embedded.inertia.surface.value_pack.packs

import com.dogecoding.android_embedded.inertia.surface.value_pack.packs.ValueHelpers.Companion.FactorScale32
import com.dogecoding.android_embedded.inertia.surface.value_pack.packs.ValueHelpers.Companion.limitInt
import com.dogecoding.android_embedded.inertia.surface.value_pack.packs.ValueHelpers.Companion.limitUInt
import com.dogecoding.android_embedded.inertia.surface.value_pack.packs.ValueHelpers.Companion.requireValueIndex
import com.dogecoding.android_embedded.inertia.surface.value_pack.block.BlockPack3x21B8

/**
 * Implementations of the different codec variants for 3x21 packing.
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class ValuePack3x21B8 : BlockPack3x21B8() {
    // Limited Variants
    protected fun getUnsignedLimited(valueIndex: Int): UInt =
        limitUInt(getRaw(valueIndex), 0u, RAW_MAX)

    protected fun setUnsignedLimited(valueIndex: Int, value: UInt) =
        setRaw(valueIndex, limitUInt(value, 0u, RAW_MAX))

    protected fun getSignedLimited(valueIndex: Int): Int =
        signExtend21(limitUInt(getRaw(valueIndex), 0u, RAW_MAX))

    protected fun setSignedLimited(valueIndex: Int, value: Int) =
        setRaw(valueIndex, limitInt(value, SIGNED_MIN, SIGNED_MAX).toUInt() and RAW_MAX)

    // Masked Variants
    protected fun getUnsignedMasked(valueIndex: Int): UInt = getRaw(valueIndex)
    protected fun setUnsignedMasked(valueIndex: Int, value: UInt) =
        writeMaskedRaw(valueIndex, value and RAW_MAX)

    protected fun getSignedMasked(valueIndex: Int): Int = signExtend21(getRaw(valueIndex))
    protected fun setSignedMasked(valueIndex: Int, value: Int) =
        writeMaskedRaw(valueIndex, value.toUInt() and RAW_MAX)

    // Scaled Variants
    protected fun getUnsignedScaled(valueIndex: Int, minVal: Int, maxVal: Int): Int {
        require(maxVal > minVal) { "Scaled value packs require MaxValue > MinValue." }
        val valueRange = (maxVal - minVal).toUInt()
        val decompressScale = FactorScale32.getFactor(valueRange, RAW_MAX)
        val limited = limitUInt(getRaw(valueIndex), 0u, RAW_MAX)
        return minVal + FactorScale32.scale(decompressScale, limited).toInt()
    }

    protected fun setUnsignedScaled(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) {
        require(maxVal > minVal) { "Scaled value packs require MaxValue > MinValue." }
        val limited = limitInt(value, minVal, maxVal)
        val valueRange = (maxVal - minVal).toUInt()
        val compressScale = FactorScale32.getFactor(RAW_MAX, valueRange)
        val normalized = (limited - minVal).toUInt()
        setRaw(valueIndex, FactorScale32.scale(compressScale, normalized))
    }

    protected fun getSignedScaled(valueIndex: Int, minVal: Int, maxVal: Int): Int =
        getUnsignedScaled(valueIndex, minVal, maxVal)

    protected fun setSignedScaled(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) =
        setUnsignedScaled(valueIndex, value, minVal, maxVal)

    private fun signExtend21(value: UInt): Int {
        return if ((value and 0x00100000u) != 0u) (value or 0xFFE00000u).toInt() else value.toInt()
    }

    private fun writeMaskedRaw(valueIndex: Int, value: UInt) {
        requireValueIndex(valueIndex, 2)
        val bitOffset = valueIndex * 21
        var packed = getPacked()
        packed = packed and (RAW_MAX.toULong() shl bitOffset).inv()
        packed = packed or (value.toULong() shl bitOffset)
        setPacked(packed)
    }
}
