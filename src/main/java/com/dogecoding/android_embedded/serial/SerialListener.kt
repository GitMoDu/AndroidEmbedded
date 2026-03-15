package com.dogecoding.android_embedded.serial

interface SerialListener {
    fun onConnected()
    fun onDisconnected()

    fun onNewData(data: ByteArray)
    fun onRunError(e: Exception)
}