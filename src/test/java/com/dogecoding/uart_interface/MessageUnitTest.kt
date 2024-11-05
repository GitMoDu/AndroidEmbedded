package com.dogecoding.uart_interface

import com.dogecoding.embedded.uart_interface.codec.Cobs
import com.dogecoding.embedded.uart_interface.codec.Cobs.Companion.MAX_COBS_MESSAGE_SIZE
import com.dogecoding.embedded.uart_interface.codec.UartCrc
import com.dogecoding.embedded.uart_interface.extension.toUByteArray
import com.dogecoding.embedded.uart_interface.model.Format
import com.dogecoding.embedded.uart_interface.model.Message
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MessageUnitTest {
    @Test
    fun messageEncodeDecodeMatch() {
        assert(messageEncodeDecode(0u, 0))
        assert(messageEncodeDecode(3u, 0))
        assert(messageEncodeDecode(1u, 40))
        assert(messageEncodeDecode(2u, 150))
        assert(messageEncodeDecode(3u, MAX_COBS_MESSAGE_SIZE - Message.getMessageSize(0)))
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun messageEncodeDecode(header: UByte, payloadSize: Int): Boolean {
        val key: UByteArray = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8).toUByteArray()
        val uartCrc = UartCrc(key)

        val maxSize = 256

        val testBuffer = UByteArray(maxSize)
        val testOutMessage = UByteArray(maxSize)
        val testInMessage = UByteArray(maxSize)
        for (i in 0 until maxSize) {
            testOutMessage[i] = i.toUByte()
            testInMessage[i] = 0u
        }

        testOutMessage[Format.Header.index] = header

        val size = Message.getMessageSize(payloadSize)
        val outCrc = uartCrc.crc(
            testOutMessage, size - Format.Header.index, Format.Header.index
        )

        testOutMessage[Format.Crc0.index] = (outCrc and 255u).toUByte()
        testOutMessage[Format.Crc1.index] = ((outCrc.toInt() shr 8) and 255).toUByte()

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

        val inCrc = uartCrc.crc(
            bytes = testInMessage,
            size = inSize - Format.Header.index,
            offset = Format.Header.index
        )

        if (outCrc != inCrc) {
            return false
        }

        return true
    }
}