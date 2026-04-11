package com.dogecoding.android_embedded.serial.model

import android.content.Context
import com.dogecoding.android_embedded.serial.model.SerialListener

interface SerialInterface {
    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun connect(context: Context, serialListener: SerialListener)
    fun removeListener(serialListener: SerialListener)
    fun disconnect()
    fun serialWrite(value: UByte)
    fun serialWrite(data: UByteArray, size: Int)
}