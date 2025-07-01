package com.dogecoding.embedded.uart_interface.codec


@OptIn(ExperimentalUnsignedTypes::class)
object CobsFallback {
    fun encodeDstBufMaxLen(srcLen: Int): Int {
        return ((srcLen) + (((srcLen) + 253) / 254))
    }

    fun decodeDstBufMaxLen(srcLen: Int): Int {
        return (if (((srcLen) == 0)) 0 else ((srcLen) - 1))
    }


    fun encode(
        dst_buf_ptr: UByteArray, dst_buf_len: Int, src_ptr: UByteArray, src_len: Int
    ): EncodeResult {
        val result = EncodeResult()
        result.outLen = 0
        result.status = EncodeStatus.OK

        var dst_write_counter = 1
        var dst_code_write_counter = 0
        val dst_buf_end_counter = dst_buf_len

        var src_ptr_counter = 0
        val src_end_counter = src_len

        var search_len = 1

        if (src_len != 0) {/* Iterate over the source bytes */
            while (true) {/* Check for running out of output buffer space */
                if (dst_write_counter > dst_buf_end_counter) {
                    result.status = EncodeStatus.OUT_BUFFER_OVERFLOW
                    break
                }

                val src_byte = src_ptr[src_ptr_counter++].toInt()

                if (src_byte == 0) {/* We found a zero byte */
                    dst_buf_ptr[dst_code_write_counter] = (search_len and 0xFF).toUByte()
                    dst_code_write_counter = dst_write_counter++
                    search_len = 1
                    if (src_ptr_counter >= src_end_counter) {
                        break
                    }
                } else {/* Copy the non-zero byte to the destination buffer */
                    dst_buf_ptr[dst_write_counter++] = (src_byte and 0xFF).toUByte()

                    search_len++
                    if (src_ptr_counter >= src_end_counter) {
                        break
                    }
                    if (search_len == 0xFF) {/* We have a long string of non-zero bytes, so we need
                         * to write out a length code of 0xFF. */
                        dst_buf_ptr[dst_code_write_counter] = (search_len and 0xFF).toUByte()

                        dst_code_write_counter = dst_write_counter++
                        search_len = 1
                    }
                }
            }
        }

        /* We've reached the end of the source data (or possibly run out of output buffer)
         * Finalise the remaining output. In particular, write the code (length) byte.
         * Update the pointer to calculate the final output length.
         */
        if (dst_code_write_counter > dst_buf_end_counter) {/* We've run out of output buffer to write the code byte. */
            result.status = EncodeStatus.OUT_BUFFER_OVERFLOW
            dst_write_counter = dst_buf_end_counter
        } else {/* Write the last code (length) byte. */
            dst_buf_ptr[dst_code_write_counter] = (search_len and 0xFF).toUByte()
        }

        /* Calculate the output length, from the value of dst_code_write_ptr */
        result.outLen = dst_write_counter

        return result
    }


    fun decode(
        dst_buf_ptr: UByteArray, dst_buf_len: Int, src_ptr: UByteArray, src_len: Int
    ): DecodeResult {
        val result = DecodeResult()
        result.outLen = 0
        result.status = DecodeStatus.OK

        var src_ptr_counter = 0
        val src_end_counter = src_len
        val dst_buf_end_counter = dst_buf_len
        var dst_write_counter = 0
        var remaining_bytes: Int
        var src_byte: Int
        var i: Int
        var len_code: Int

        if (src_len != 0) {
            while (true) {
                len_code = src_ptr[src_ptr_counter++].toInt()
                if (len_code == 0) {
                    result.status = DecodeStatus.ZERO_BYTE_IN_INPUT
                    break
                }
                len_code--

                /* Check length code against remaining input bytes */
                remaining_bytes = src_end_counter - src_ptr_counter
                if (len_code > remaining_bytes) {
                    result.status = DecodeStatus.INPUT_TOO_SHORT
                    len_code = remaining_bytes
                }

                /* Check length code against remaining output buffer space */
                remaining_bytes = dst_buf_end_counter - dst_write_counter
                if (len_code > remaining_bytes) {
                    result.status = DecodeStatus.OUT_BUFFER_OVERFLOW
                    len_code = remaining_bytes
                }

                i = len_code
                while (i != 0) {
                    src_byte = (src_ptr[src_ptr_counter++].toInt() and 0xFF).toChar().code
                    if (src_byte == 0) {
                        result.status = DecodeStatus.ZERO_BYTE_IN_INPUT
                    }

                    dst_buf_ptr[dst_write_counter++] = src_byte.toUByte()
                    i--

                    if (src_ptr_counter >= src_end_counter) {
                        break
                    }
                }

                if (src_ptr_counter >= src_end_counter) {
                    break
                }

                /* Add a zero to the end */
                if (len_code != 0xFE) {
                    if (dst_write_counter >= dst_buf_end_counter) {
                        result.status = DecodeStatus.OUT_BUFFER_OVERFLOW
                        break
                    }
                    dst_buf_ptr[dst_write_counter++] = 0u
                }
            }
        }

        result.outLen = dst_write_counter

        return result
    }


    enum class EncodeStatus {
        OK, NULL_POINTER, OUT_BUFFER_OVERFLOW
    }

    class EncodeResult {
        var outLen: Int = 0
        var status: EncodeStatus? = null
    }

    enum class DecodeStatus {
        OK, NULL_POINTER, OUT_BUFFER_OVERFLOW, ZERO_BYTE_IN_INPUT, INPUT_TOO_SHORT
    }

    class DecodeResult {
        var outLen: Int = 0
        var status: DecodeStatus? = null
    }
}
