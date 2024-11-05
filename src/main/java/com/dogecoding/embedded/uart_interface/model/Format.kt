package com.dogecoding.embedded.uart_interface.model

enum class Format(val index: Int) {
    Crc0(0),
    Crc1(1),
    Header(2),
    Payload(3)
}