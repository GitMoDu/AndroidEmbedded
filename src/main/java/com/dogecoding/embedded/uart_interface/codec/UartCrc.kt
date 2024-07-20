package com.dogecoding.embedded.uart_interface.codec

@OptIn(ExperimentalUnsignedTypes::class)
class UartCrc(private val key: UByteArray) {

    private val fletcher = Fletcher16()

    fun crc(bytes: UByteArray, size: Int, offset: Int): Int {
        fletcher.begin()

        fletcher.add(value = size.toUByte())
        fletcher.add(bytes = key, size = key.count(), offset = 0)
        fletcher.add(bytes = bytes, size = size, offset = offset)

        return fletcher.getFletcher()
    }
}