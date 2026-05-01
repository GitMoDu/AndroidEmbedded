package com.dogecoding.android_embedded.serial.model

import android.content.Context
import com.dogecoding.android_embedded.serial.model.SerialListener

@OptIn(ExperimentalUnsignedTypes::class)
interface SerialInterface {
    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun connect(context: Context, serialListener: SerialListener)
    fun removeListener(serialListener: SerialListener)
    fun disconnect()
    fun serialWrite(value: UByte)
    fun serialWrite(data: UByteArray, size: Int)
}
