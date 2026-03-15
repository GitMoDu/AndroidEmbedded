package com.dogecoding.android_embedded.usb_serial

interface UsbSerialListener {
    fun onConnected()
    fun onDisconnected()

    fun onNewData(data: ByteArray)
    fun onRunError(e: Exception)
}