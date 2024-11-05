package com.dogecoding.embedded.uart_interface.codec

@OptIn(ExperimentalUnsignedTypes::class)
class UartCrc(private val key: UByteArray) {

    companion object {
        const val CRC: String = "Fletcher16"
    }

    private val fletcher = Fletcher16()


    fun crc(bytes: UByteArray, size: Int, offset: Int): UShort {
        fletcher.begin()
        fletcher.add(bytes = key, size = key.count(), offset = 0)
        fletcher.add(bytes = bytes, size = size, offset = offset)

        return fletcher.getFletcher()
    }
}