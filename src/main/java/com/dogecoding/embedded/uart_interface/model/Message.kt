package com.dogecoding.embedded.uart_interface.model

class Message {
    enum class Format(val index: Int) {
        Crc0(0),
        Crc1(1),
        Header(2),
        Payload(3)
    }

    companion object {
        fun getMessageSize(payloadSize: Int): Int {
            return Format.Payload.index + payloadSize
        }

        const val CRC: String = "Fletcher16"
    }
}