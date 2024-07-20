package com.dogecoding.embedded.uart_interface.codec

@OptIn(ExperimentalUnsignedTypes::class)
class Fletcher16 {

    companion object {
        private val MODULUS: Short = UShort.MAX_VALUE.toShort()

        fun fletcher16(data: ByteArray): Int {
            var sum1 = 0
            var sum2 = 0

            for (d in data) {
                sum1 = (sum1 + d) % MODULUS
                sum2 = (sum2 + sum1) % MODULUS
            }

            return sum2.shl(8).or(sum1)
        }
    }

    private var sum1: Int = 0
    private var sum2: Int = 0

    fun begin(s1: Int = 0, s2: Int = 0) {
        sum1 = s1
        sum2 = s2
    }

    fun add(value: UByte) {
        sum1 = (sum1 + value.toInt()) % MODULUS
        sum2 = (sum2 + sum1) % MODULUS
    }

    fun add(bytes: UByteArray, size: Int, offset: Int) {
        for (i in 0 until size) {
            add(bytes[i + offset])
        }
    }

    fun getFletcher(): Int {
        return sum2.shl(8).or(sum1)
    }
}