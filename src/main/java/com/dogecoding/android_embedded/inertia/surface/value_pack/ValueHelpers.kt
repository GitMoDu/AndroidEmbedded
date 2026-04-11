package com.dogecoding.android_embedded.inertia.surface.value_pack

class ValueHelpers {
    companion object {
        fun requireValueIndex(valueIndex: Int, maxValueIndex: Int) {
            require(valueIndex in 0..maxValueIndex) { "ValueIndex must be between 0 and $maxValueIndex." }
        }

        fun limitUInt(value: UInt, minValue: UInt, maxValue: UInt): UInt =
            when {
                value < minValue -> minValue
                value > maxValue -> maxValue
                else -> value
            }

        fun limitUShort(value: UShort, minValue: UShort, maxValue: UShort): UShort =
            limitUInt(value.toUInt(), minValue.toUInt(), maxValue.toUInt()).toUShort()

        fun limitInt(value: Int, minValue: Int, maxValue: Int): Int =
            when {
                value < minValue -> minValue
                value > maxValue -> maxValue
                else -> value
            }

        fun limitShort(value: Short, minValue: Short, maxValue: Short): Short =
            limitInt(value.toInt(), minValue.toInt(), maxValue.toInt()).toShort()

        object FactorScale32 {
            private const val BIT_SHIFTS = 16
            private const val SCALE_UNIT = 0x1_0000u

            fun getFactor(numerator: UInt, denominator: UInt): UInt =
                if (denominator == 0u) {
                    SCALE_UNIT
                } else {
                    ((numerator.toULong() shl BIT_SHIFTS) / denominator.toULong()).toUInt()
                }

            fun scale(factor: UInt, value: UInt): UInt =
                ((value.toULong() * factor.toULong()) shr BIT_SHIFTS).toUInt()
        }

    }
}