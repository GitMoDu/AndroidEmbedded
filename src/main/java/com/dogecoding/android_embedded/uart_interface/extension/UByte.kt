package com.dogecoding.android_embedded.uart_interface.extension


@OptIn(ExperimentalUnsignedTypes::class)
fun IntArray.toUByteArray(): UByteArray {
    val byteArray = UByteArray(size)

    for (i in indices) {
        byteArray[i] = get(i).toUByte()
    }

    return byteArray
}
