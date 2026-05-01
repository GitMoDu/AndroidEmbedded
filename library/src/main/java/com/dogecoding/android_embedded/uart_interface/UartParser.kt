package com.dogecoding.android_embedded.uart_interface

import android.util.Log
import com.dogecoding.android_embedded.uart_interface.UartMessenger.Companion.PacketEndMarker

@OptIn(ExperimentalUnsignedTypes::class)
class UartParser {
    companion object {
        private val TAG: String = UartParser::class.java.name
    }

    enum class StateEnum {
        WaitingForStart,
        WaitingForData,
        ReadingData
    }

    private var state: StateEnum = StateEnum.WaitingForStart
    private var inSize: Int = 0

    fun clear() {
        state = StateEnum.WaitingForStart
        inSize = 0
    }

    fun parseIn(inBuffer: UByteArray, value: UByte): Int {
        when (state) {
            StateEnum.WaitingForStart -> {
                if (value == PacketEndMarker) {
                    state = StateEnum.WaitingForData
                    inSize = 0
                }
            }

            StateEnum.WaitingForData -> {
                if (value == PacketEndMarker) {
                    // Skip repeated delimiters silently
                } else {
                    state = StateEnum.ReadingData
                    inSize = 0
                    inBuffer[inSize++] = value
                }
            }

            StateEnum.ReadingData -> {
                if (value == PacketEndMarker) {
                    val finalSize = inSize
                    state = StateEnum.WaitingForStart
                    inSize = 0
                    return finalSize
                } else {
                    if (inSize < inBuffer.size) {
                        inBuffer[inSize++] = value
                    } else {
                        Log.w(TAG, "Buffer overflow - packet too large, resetting.")
                        state = StateEnum.WaitingForStart
                        inSize = 0
                    }
                }
            }
        }

        return 0
    }
}
