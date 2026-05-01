package com.dogecoding.android_embedded.inertia.surface.model

import com.dogecoding.android_embedded.inertia.components.surface.value_pack.packs.ValuePack3x21B8
import com.dogecoding.android_embedded.inertia.components.surface.value_pack.packs.ValuePack4x12B6
import com.dogecoding.android_embedded.inertia.components.surface.value_pack.block.BlockPack3x21B8
import com.dogecoding.android_embedded.inertia.components.surface.value_pack.block.BlockPack4x12B6
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalUnsignedTypes::class)
class PackHelpersUnitTest {

    @Test
    fun blockPack4x12RawSetClampsToRawMax() {
        val pack = TestValuePack4x12B6()

        pack.setRawValue(0, 5000u.toUShort())

        assertEquals(BlockPack4x12B6.RAW_MAX.toLong(), pack.raw(0).toLong())
    }

    @Test
    fun blockPack4x12SignedSetClampsToSignedRange() {
        val pack = TestValuePack4x12B6()

        pack.setSignedValue(0, 3000.toShort())
        pack.setSignedValue(1, (-3000).toShort())

        assertEquals(BlockPack4x12B6.SIGNED_MAX.toLong(), pack.signed(0).toLong())
        assertEquals(BlockPack4x12B6.SIGNED_MIN.toLong(), pack.signed(1).toLong())
    }

    @Test
    fun valuePack4x12UnsignedLimitedClamps() {
        val pack = TestValuePack4x12B6()
        pack.setUnsignedLimitedValue(0, 5000u.toUShort())
        assertEquals(BlockPack4x12B6.RAW_MAX.toInt(), pack.getUnsignedLimitedValue(0).toInt())
    }

    @Test
    fun valuePack4x12SignedLimitedClamps() {
        val pack = TestValuePack4x12B6()
        pack.setSignedLimitedValue(0, 3000.toShort())
        pack.setSignedLimitedValue(1, (-3000).toShort())
        assertEquals(BlockPack4x12B6.SIGNED_MAX.toInt(), pack.getSignedLimitedValue(0).toInt())
        assertEquals(BlockPack4x12B6.SIGNED_MIN.toInt(), pack.getSignedLimitedValue(1).toInt())
    }

    @Test
    fun valuePack4x12UnsignedMaskedWraps() {
        val pack = TestValuePack4x12B6()
        // 4096 (0x1000) should mask to 0
        pack.setUnsignedMaskedValue(0, 4096u.toUShort())
        assertEquals(0, pack.getUnsignedMaskedValue(0).toInt())
    }

    @Test
    fun valuePack4x12SignedMaskedUsesMaskedEncoding() {
        val pack = TestValuePack4x12B6()

        pack.setSignedMaskedValue(0, 3000.toShort())

        assertEquals(3000L, pack.raw(0).toLong())
        assertEquals((-1096).toLong(), pack.signedMasked(0).toLong())
    }

    @Test
    fun valuePack4x12Scaled() {
        val pack = TestValuePack4x12B6()
        val min = -100
        val max = 100

        // Exact middle
        pack.setUnsignedScaledValue(0, 0, min, max)
        // (0 - (-100)) = 100. 100/200 = 0.5. 0.5 * 4095 = 2047.5
        val raw = pack.raw(0).toInt()
        assertTrue("Raw value $raw should be near 2047 or 2048", raw in 2047..2048)
        val recovered = pack.getUnsignedScaledValue(0, min, max)
        assertTrue("Recovered value $recovered should be near 0", recovered in -1..1)

        pack.setUnsignedScaledValue(1, max, min, max)
        val rawMax = pack.raw(1).toInt()
        assertTrue(
            "Raw max $rawMax should be near RAW_MAX",
            rawMax >= BlockPack4x12B6.RAW_MAX.toInt() - 2
        )
        val recoveredMax = pack.getUnsignedScaledValue(1, min, max)
        assertTrue(
            "Recovered max value $recoveredMax should be near $max",
            recoveredMax in (max - 1)..max
        )

        pack.setUnsignedScaledValue(2, min, min, max)
        assertEquals(0, pack.raw(2).toInt())
        assertEquals(min, pack.getUnsignedScaledValue(2, min, max))
    }

    @Test
    fun blockPack3x21RawSetClampsToRawMax() {
        val pack = TestValuePack3x21B8()

        pack.setRawValue(0, 3_000_000u)

        assertEquals(BlockPack3x21B8.RAW_MAX.toLong(), pack.raw(0).toLong())
    }

    @Test
    fun blockPack3x21SignedSetClampsToSignedRange() {
        val pack = TestValuePack3x21B8()

        pack.setSignedValue(0, 2_000_000)
        pack.setSignedValue(1, -2_000_000)

        assertEquals(BlockPack3x21B8.SIGNED_MAX.toLong(), pack.signed(0).toLong())
        assertEquals(BlockPack3x21B8.SIGNED_MIN.toLong(), pack.signed(1).toLong())
    }

    @Test
    fun valuePack3x21UnsignedLimitedClamps() {
        val pack = TestValuePack3x21B8()
        pack.setUnsignedLimitedValue(0, 3_000_000u)
        assertEquals(BlockPack3x21B8.RAW_MAX.toInt(), pack.getUnsignedLimitedValue(0).toInt())
    }

    @Test
    fun valuePack3x21SignedLimitedClamps() {
        val pack = TestValuePack3x21B8()
        pack.setSignedLimitedValue(0, 2_000_000)
        pack.setSignedLimitedValue(1, -2_000_000)
        assertEquals(BlockPack3x21B8.SIGNED_MAX, pack.getSignedLimitedValue(0))
        assertEquals(BlockPack3x21B8.SIGNED_MIN, pack.getSignedLimitedValue(1))
    }

    @Test
    fun valuePack3x21UnsignedMaskedWraps() {
        val pack = TestValuePack3x21B8()
        // RAW_MAX + 1 should mask to 0
        pack.setUnsignedMaskedValue(0, BlockPack3x21B8.RAW_MAX + 1u)
        assertEquals(0u, pack.getUnsignedMaskedValue(0))
    }

    @Test
    fun valuePack3x21SignedMaskedUsesMaskedEncoding() {
        val pack = TestValuePack3x21B8()

        pack.setSignedMaskedValue(0, 1_500_000)

        assertEquals(1_500_000L, pack.raw(0).toLong())
        assertEquals((-597_152).toLong(), pack.signedMasked(0).toLong())
    }

    @Test
    fun valuePack3x21Scaled() {
        val pack = TestValuePack3x21B8()
        val min = 0
        val max = 10000

        // Exact middle
        pack.setUnsignedScaledValue(0, 5000, min, max)
        // 5000/10000 = 0.5. 0.5 * 2097151 = 1048575.5
        val raw = pack.raw(0).toInt()
        assertTrue("Raw value $raw should be near 1048575 or 1048576", raw in 1048575..1048576)

        // The current implementation might have some precision loss due to BIT_SHIFTS = 16
        val recovered = pack.getUnsignedScaledValue(0, min, max)
        assertTrue("Recovered value $recovered should be near 5000", recovered in 4990..5010)

        pack.setUnsignedScaledValue(1, max, min, max)
        val rawMax = pack.raw(1).toLong()
        assertTrue(
            "Raw max $rawMax should be near RAW_MAX",
            rawMax >= BlockPack3x21B8.RAW_MAX.toLong() - 5
        )
        val recoveredMax = pack.getUnsignedScaledValue(1, min, max)
        assertTrue("Recovered max $recoveredMax should be near $max", recoveredMax >= max - 20)
    }

    @Test
    fun scaledHelpersRejectInvalidRange() {
        val pack4 = TestValuePack4x12B6()
        val pack3 = TestValuePack3x21B8()

        assertThrows(IllegalArgumentException::class.java) {
            pack4.setUnsignedScaledValue(0, 10, 100, 100)
        }
        assertThrows(IllegalArgumentException::class.java) {
            pack3.getUnsignedScaledValue(0, 25, 25)
        }
    }

    private class TestValuePack4x12B6 : ValuePack4x12B6() {
        fun raw(valueIndex: Int): UShort = getRaw(valueIndex)
        fun setRawValue(valueIndex: Int, value: UShort) = setRaw(valueIndex, value)
        fun signed(valueIndex: Int): Short = getSigned(valueIndex)
        fun setSignedValue(valueIndex: Int, value: Short) = setSigned(valueIndex, value)

        fun getUnsignedLimitedValue(valueIndex: Int) = getUnsignedLimited(valueIndex)
        fun setUnsignedLimitedValue(valueIndex: Int, value: UShort) =
            setUnsignedLimited(valueIndex, value)

        fun getSignedLimitedValue(valueIndex: Int) = getSignedLimited(valueIndex)
        fun setSignedLimitedValue(valueIndex: Int, value: Short) =
            setSignedLimited(valueIndex, value)

        fun getUnsignedMaskedValue(valueIndex: Int) = getUnsignedMasked(valueIndex)
        fun setUnsignedMaskedValue(valueIndex: Int, value: UShort) =
            setUnsignedMasked(valueIndex, value)

        fun signedMasked(valueIndex: Int): Short = getSignedMasked(valueIndex)
        fun setSignedMaskedValue(valueIndex: Int, value: Short) = setSignedMasked(valueIndex, value)

        fun getUnsignedScaledValue(valueIndex: Int, minVal: Int, maxVal: Int) =
            getUnsignedScaled(valueIndex, minVal, maxVal)

        fun setUnsignedScaledValue(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) =
            setUnsignedScaled(valueIndex, value, minVal, maxVal)
    }

    private class TestValuePack3x21B8 : ValuePack3x21B8() {
        fun raw(valueIndex: Int): UInt = getRaw(valueIndex)
        fun setRawValue(valueIndex: Int, value: UInt) = setRaw(valueIndex, value)
        fun signed(valueIndex: Int): Int = getSigned(valueIndex)
        fun setSignedValue(valueIndex: Int, value: Int) = setSigned(valueIndex, value)

        fun getUnsignedLimitedValue(valueIndex: Int) = getUnsignedLimited(valueIndex)
        fun setUnsignedLimitedValue(valueIndex: Int, value: UInt) =
            setUnsignedLimited(valueIndex, value)

        fun getSignedLimitedValue(valueIndex: Int) = getSignedLimited(valueIndex)
        fun setSignedLimitedValue(valueIndex: Int, value: Int) = setSignedLimited(valueIndex, value)

        fun getUnsignedMaskedValue(valueIndex: Int) = getUnsignedMasked(valueIndex)
        fun setUnsignedMaskedValue(valueIndex: Int, value: UInt) =
            setUnsignedMasked(valueIndex, value)

        fun signedMasked(valueIndex: Int): Int = getSignedMasked(valueIndex)
        fun setSignedMaskedValue(valueIndex: Int, value: Int) = setSignedMasked(valueIndex, value)

        fun getUnsignedScaledValue(valueIndex: Int, minVal: Int, maxVal: Int): Int =
            getUnsignedScaled(valueIndex, minVal, maxVal)

        fun setUnsignedScaledValue(valueIndex: Int, value: Int, minVal: Int, maxVal: Int) =
            setUnsignedScaled(valueIndex, value, minVal, maxVal)
    }
}
