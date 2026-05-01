package com.dogecoding.android_embedded.uart_interface.model

@OptIn(ExperimentalUnsignedTypes::class)
interface UartMessengerListener {
    fun onMessageReceived(header: UByte, payload: UByteArray?)
    fun onUartStateChange(uartConnected: Boolean)
}
