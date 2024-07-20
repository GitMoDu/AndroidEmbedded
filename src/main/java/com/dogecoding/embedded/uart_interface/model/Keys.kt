@file:OptIn(ExperimentalUnsignedTypes::class)

package com.dogecoding.embedded.uart_interface.model

class Keys {
    companion object {
        fun getUByteArray(intArray: IntArray): UByteArray {
            val byteArray = UByteArray(intArray.size)

            for (i in intArray.indices) {
                byteArray[i] = intArray[i].toUByte()
            }

            return byteArray
        }
    }
}