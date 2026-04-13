package com.dogecoding.android_embedded.uart_interface

import com.dogecoding.android_embedded.uart_interface.codec.Cobs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for COBS (Consistent Overhead Byte Stuffing) codec.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class CodecUnitTest {

    @Test
    fun cobsEncodeAndDecodeMatch() {
        assert(testEncodeDecodeMatch(1))
        assert(testEncodeDecodeMatch(2))
        assert(testEncodeDecodeMatch(3))
        assert(testEncodeDecodeMatch(40))
        assert(testEncodeDecodeMatch(100))
        assert(testEncodeDecodeMatch(150))
        assert(testEncodeDecodeMatch(200))
        assert(testEncodeDecodeMatch(Cobs.MAX_COBS_MESSAGE_SIZE))
    }

    @Test
    fun cobsKnownVector1() {
        // Data: [0x00]
        // Encoded: [0x01, 0x01]
        val source = ubyteArrayOf(0x00u)
        val target = UByteArray(2)
        val encodedSize = Cobs.encode(target, 1, source)
        
        assertEquals(2, encodedSize)
        assertArrayEquals(ubyteArrayOf(0x01u, 0x01u).toByteArray(), target.toByteArray())
    }

    @Test
    fun cobsKnownVector2() {
        // Data: [0x01, 0x02, 0x03]
        // Encoded: [0x04, 0x01, 0x02, 0x03]
        val source = ubyteArrayOf(0x01u, 0x02u, 0x03u)
        val target = UByteArray(4)
        val encodedSize = Cobs.encode(target, 3, source)
        
        assertEquals(4, encodedSize)
        assertArrayEquals(ubyteArrayOf(0x04u, 0x01u, 0x02u, 0x03u).toByteArray(), target.toByteArray())
    }

    @Test
    fun cobsKnownVector3() {
        // Data: [0x01, 0x00, 0x02]
        // Encoded: [0x02, 0x01, 0x02, 0x02]
        val source = ubyteArrayOf(0x01u, 0x00u, 0x02u)
        val target = UByteArray(4)
        val encodedSize = Cobs.encode(target, 3, source)
        
        assertEquals(4, encodedSize)
        assertArrayEquals(ubyteArrayOf(0x02u, 0x01u, 0x02u, 0x02u).toByteArray(), target.toByteArray())
    }

    @Test
    fun cobsLongNonZeroSequence() {
        // Data: 254 non-zero bytes [0x01, 0x01, ...]
        // Encoded: [0xFF, 0x01, 0x01, ..., 0x01] (total 256 bytes)
        val source = UByteArray(254) { 0x01u }
        val target = UByteArray(256)
        val encodedSize = Cobs.encode(target, 254, source)
        
        assertEquals(255, encodedSize)
        assertEquals(0xFFu.toUByte(), target[0])
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun testEncodeDecodeMatch(size: Int): Boolean {
        if (size == 0) {
            return false
        }

        val maxSize = 256
        val testBuffer = UByteArray(maxSize)
        val testOutMessage = UByteArray(maxSize)
        val testInMessage = UByteArray(maxSize)

        for (i in 0 until maxSize) {
            testOutMessage[i] = i.toUByte()
            testInMessage[i] = 0u
        }

        val encodedSize = Cobs.Companion.encode(
            target = testBuffer, size = size, source = testOutMessage
        )
        val outSize = size + 1

        if (encodedSize != outSize) {
            return false
        }
        val decodedSize = Cobs.Companion.decode(
            target = testInMessage, size = outSize, source = testBuffer
        )
        val inSize = outSize - 1

        if (size != inSize) {
            return false
        }

        if (decodedSize != inSize) {
            return false
        }

        for (i in 0 until size) {
            if (testInMessage[i] != testOutMessage[i]) {
                return false
            }
        }

        return true
    }
}