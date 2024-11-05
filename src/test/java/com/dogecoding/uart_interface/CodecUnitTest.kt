@file:OptIn(ExperimentalUnsignedTypes::class)

package com.dogecoding.uart_interface

import com.dogecoding.embedded.uart_interface.codec.Cobs
import com.dogecoding.embedded.uart_interface.codec.Cobs.Companion.MAX_COBS_MESSAGE_SIZE
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CodecUnitTest {

    @Test
    fun cobsEncodeAndDecodeMatch() {
        assert(!testEncodeDecodeMatch(0))
        assert(testEncodeDecodeMatch(1))
        assert(testEncodeDecodeMatch(2))
        assert(testEncodeDecodeMatch(3))
        assert(testEncodeDecodeMatch(40))
        assert(testEncodeDecodeMatch(100))
        assert(testEncodeDecodeMatch(150))
        assert(testEncodeDecodeMatch(200))
        assert(testEncodeDecodeMatch(MAX_COBS_MESSAGE_SIZE))
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

        val encodedSize = Cobs.encode(
            target = testBuffer, size = size, source = testOutMessage
        )
        val outSize = size + 1

        if (encodedSize != outSize) {
            return false
        }
        val decodedSize = Cobs.decode(
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