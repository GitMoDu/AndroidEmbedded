package com.dogecoding.android_embedded.uart_interface.model

enum class Format(val index: Int) {
    Crc0(0),
    Crc1(1),
    Header(2),
    Payload(3)
}