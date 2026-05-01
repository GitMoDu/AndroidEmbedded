package com.dogecoding.android_embedded.inertia.components.core.model

/*
 * Angle type definition (angle_t = uint16_t).
 * Maps 360º to an modular integer 16 bit unsigned value.
 */
class Angle {
    companion object {
        const val PRECISION: Float = 360f / 65536f
        const val MAX_VALUE: Int = 65535
        const val MIN_VALUE: Int = 0

        fun degreesFromAngle(angle: Int): Float {
            return angle * PRECISION
        }

        fun angleFromDegrees(degrees: Float): Int {
            return (degrees / PRECISION).toInt()
        }
    }
}
