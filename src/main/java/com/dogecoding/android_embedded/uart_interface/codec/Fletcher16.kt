package com.dogecoding.android_embedded.uart_interface.codec

@OptIn(ExperimentalUnsignedTypes::class)
class Fletcher16 {

    companion object {
        private val MODULUS: UShort = UByte.MAX_VALUE.toUShort()
    }

    private var sum1: UShort = 0u
    private var sum2: UShort = 0u

    fun begin(s1: UShort = 0u, s2: UShort = 0u) {
        sum1 = s1
        sum2 = s2
    }

    fun add(value: UByte) {
        sum1 = (sum1 + value.toUShort()).toUShort()
        if (sum1 >= MODULUS) {
            sum1 = (sum1 - MODULUS).toUShort()
        }
        sum2 = (sum2 + sum1).toUShort()
        if (sum2 >= MODULUS) {
            sum2 = (sum2 - MODULUS).toUShort()
        }
    }

    fun add(bytes: UByteArray, size: Int, offset: Int) {
        for (i in 0 until size) {
            add(bytes[i + offset])
        }
    }

    fun getFletcher(): UShort {
        return sum1.or(sum2.toInt().shl(8).toUShort())
    }
}