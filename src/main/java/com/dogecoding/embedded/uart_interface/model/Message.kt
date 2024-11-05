package com.dogecoding.embedded.uart_interface.model

class Message {
    companion object {
        val TAG: String = Message::class.java.name

        fun getMessageSize(payloadSize: Int): Int {
            return Format.Payload.index + payloadSize
        }
    }
}