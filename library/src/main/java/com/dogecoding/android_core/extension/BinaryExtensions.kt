package com.dogecoding.android_core.extension

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.getUShortLe(offset: Int): UShort {
    return (this[offset].toUInt() or (this[offset + 1].toUInt() shl 8)).toUShort()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.setUShortLe(offset: Int, value: UShort) {
    this[offset] = (value.toUInt() and 0xFFu).toUByte()
    this[offset + 1] = (value.toUInt() shr 8 and 0xFFu).toUByte()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.getUIntLe(offset: Int): UInt {
    return this[offset].toUInt() or
            (this[offset + 1].toUInt() shl 8) or
            (this[offset + 2].toUInt() shl 16) or
            (this[offset + 3].toUInt() shl 24)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.setUIntLe(offset: Int, value: UInt) {
    this[offset] = (value and 0xFFu).toUByte()
    this[offset + 1] = (value shr 8 and 0xFFu).toUByte()
    this[offset + 2] = (value shr 16 and 0xFFu).toUByte()
    this[offset + 3] = (value shr 24 and 0xFFu).toUByte()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.getULongLe(offset: Int): ULong {
    return this[offset].toULong() or
            (this[offset + 1].toULong() shl 8) or
            (this[offset + 2].toULong() shl 16) or
            (this[offset + 3].toULong() shl 24) or
            (this[offset + 4].toULong() shl 32) or
            (this[offset + 5].toULong() shl 40) or
            (this[offset + 6].toULong() shl 48) or
            (this[offset + 7].toULong() shl 56)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.setULongLe(offset: Int, value: ULong) {
    this[offset] = (value and 0xFFu).toUByte()
    this[offset + 1] = (value shr 8 and 0xFFu).toUByte()
    this[offset + 2] = (value shr 16 and 0xFFu).toUByte()
    this[offset + 3] = (value shr 24 and 0xFFu).toUByte()
    this[offset + 4] = (value shr 32 and 0xFFu).toUByte()
    this[offset + 5] = (value shr 40 and 0xFFu).toUByte()
    this[offset + 6] = (value shr 48 and 0xFFu).toUByte()
    this[offset + 7] = (value shr 56 and 0xFFu).toUByte()
}
