package com.dogecoding.android_embedded.inertia.drivers.hardware_interface.serial

@OptIn(ExperimentalUnsignedTypes::class)
interface SerialListener {
    fun onConnected()
    fun onDisconnected()
    fun onNewData(data: UByteArray)
    fun onRunError(e: Exception)
}
