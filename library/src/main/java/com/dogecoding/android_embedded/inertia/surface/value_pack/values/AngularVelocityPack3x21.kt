package com.dogecoding.android_embedded.inertia.surface.value_pack.values

import com.dogecoding.android_embedded.inertia.surface.value_pack.packs.ValuePack3x21B8

/**
 * Angular velocity in euler angle units per second (0.0054931640625 degrees/s per precision).
 */
@OptIn(ExperimentalUnsignedTypes::class)
open class AngularVelocityPack3x21 : ValuePack3x21B8() {
    companion object {
        const val PRECISION: Float = 360f / 65536f
    }

    var x: Int
        get() = getSignedLimited(0)
        set(value) = setSignedLimited(0, value)

    var y: Int
        get() = getSignedLimited(1)
        set(value) = setSignedLimited(1, value)

    var z: Int
        get() = getSignedLimited(2)
        set(value) = setSignedLimited(2, value)

    var xDegrees: Float
        get() = x * PRECISION
        set(value) {
            x = (value / PRECISION).toInt()
        }

    var yDegrees: Float
        get() = y * PRECISION
        set(value) {
            y = (value / PRECISION).toInt()
        }

    var zDegrees: Float
        get() = z * PRECISION
        set(value) {
            z = (value / PRECISION).toInt()
        }
}