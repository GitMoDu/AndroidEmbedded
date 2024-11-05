package com.dogecoding.embedded.uart_interface

import android.util.Log
import com.dogecoding.embedded.uart_interface.UartMessenger.Companion.PacketEndMarker
import com.dogecoding.embedded.uart_interface.model.Message.Companion.TAG

@OptIn(ExperimentalUnsignedTypes::class)
class UartParser {
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
                    if (value == PacketEndMarker) {
                        state = StateEnum.WaitingForData
                        inSize = 0
                    }
                }
            }

            StateEnum.WaitingForData -> {
                if (value == PacketEndMarker) {
                    Log.d(TAG, "Repeated delimiter detected.")
                } else {
                    state = StateEnum.ReadingData
                    inSize = 0
                    inBuffer[inSize++] = value
                }
            }

            StateEnum.ReadingData -> {
                if (value == PacketEndMarker) {
                    state = StateEnum.WaitingForStart
                    return inSize
                } else {
                    inBuffer[inSize++] = value
                    if (inSize >= inBuffer.size) {
                        state = StateEnum.WaitingForStart
                        inSize = 0
                        Log.d(TAG, "Sync error.")
                    }
                }
            }
        }

        return 0
    }
}