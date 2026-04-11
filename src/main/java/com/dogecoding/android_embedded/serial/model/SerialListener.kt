package com.dogecoding.android_embedded.serial.model

interface SerialListener {
    fun onConnected()
    fun onDisconnected()

    fun onNewData(data: ByteArray)
    fun onRunError(e: Exception)
}