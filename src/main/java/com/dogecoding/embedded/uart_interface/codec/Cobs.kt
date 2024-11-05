package com.dogecoding.embedded.uart_interface.codec

import com.dogecoding.embedded.uart_interface.codec.CobsFallback.EncodeStatus


// Credit to https://github.com/themarpe/cobs-java and https://github.com/cmcqueen/cobs-c
@OptIn(ExperimentalUnsignedTypes::class)
class Cobs {

    companion object {
        val MAX_COBS_MESSAGE_SIZE: Int = (UByte.MAX_VALUE - 2u).toInt()

        fun encode(target: UByteArray, size: Int, source: UByteArray): Int {
            var targetWriteCounter = 1
            var targetCodeWriteCounter = 0

            var sourceReadCounter = 0
            var searchLength = 1

            if (size != 0) {
                // Iterate over the source bytes
                while (true) {
                    // Check for running out of output buffer space.
                    if (targetWriteCounter > size) {
                        return targetWriteCounter + 1
                    }

                    val sourceByte = source[sourceReadCounter++].toInt()

                    if (sourceByte == 0) {
                        // We found a zero byte.
                        target[targetCodeWriteCounter] = (searchLength and 0xFF).toUByte()
                        targetCodeWriteCounter = targetWriteCounter++
                        searchLength = 1
                        if (sourceReadCounter >= size) {
                            break
                        }
                    } else {
                        // Copy the non-zero byte to the target buffer.
                        target[targetWriteCounter++] = (sourceByte and 0xFF).toUByte()

                        searchLength++
                        if (sourceReadCounter >= size) {
                            break
                        }
                        if (searchLength == 0xFF) {
                            // We have a long string of non-zero bytes, so we need to write out a length code of 0xFF.
                            target[targetCodeWriteCounter] = (searchLength and 0xFF).toUByte()

                            targetCodeWriteCounter = targetWriteCounter++
                            searchLength = 1
                        }
                    }
                }
            }

            // We've reached the end of the source data (or possibly run out of output buffer).
            // Finalise the remaining output. In particular, write the code (length) byte.
            // Update the pointer to calculate the final output length.
            if (targetCodeWriteCounter > size) {
                // We've run out of output buffer to write the code byte.
                return targetWriteCounter + 1
            } else {
                // Write the last code (length) byte.
                target[targetCodeWriteCounter] = (searchLength and 0xFF).toUByte()
            }

            // Calculate the output length, from the value of dst_code_write_ptr.
            return targetWriteCounter
        }

        fun decode(
            target: UByteArray, size: Int, source: UByteArray
        ): Int {
            var sourceReadCounter = 0
            var targetWriteCounter = 0
            var remaining: Int
            var sourceByte: Int
            var i: Int
            var lengthCode: Int

            if (size != 0) {
                while (true) {
                    lengthCode = source[sourceReadCounter++].toInt()
                    if (lengthCode == 0) {
                        return sourceReadCounter + 1
                    }
                    lengthCode--

                    /* Check length code against remaining input bytes */
                    remaining = size - sourceReadCounter
                    if (lengthCode > remaining) {
                        return lengthCode
                    }

                    /* Check length code against remaining output buffer space */
                    remaining = size - targetWriteCounter
                    if (lengthCode > remaining) {
                        return lengthCode
                    }

                    i = lengthCode
                    while (i != 0) {
                        sourceByte = (source[sourceReadCounter++].toInt() and 0xFF).toChar().code
                        if (sourceByte == 0) {
                            return 0
                        }

                        target[targetWriteCounter++] = sourceByte.toUByte()
                        i--

                        if (sourceReadCounter >= size) {
                            break
                        }
                    }

                    if (sourceReadCounter >= size) {
                        break
                    }

                    // Add a zero to the end.
                    if (lengthCode != 0xFE) {
                        if (targetWriteCounter > size) {
                            return 0
                        }
                        target[targetWriteCounter++] = 0u
                    }
                }
            }

            return targetWriteCounter
        }
    }
}
