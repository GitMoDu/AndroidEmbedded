package com.dogecoding.android_embedded.serial

import android.app.Activity

interface SerialInterface {
    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun connect(activity: Activity, serialListener: SerialListener)
    fun disconnect()
    fun serialWrite(value: UByte)
    fun serialWrite(data: UByteArray, size: Int)
}