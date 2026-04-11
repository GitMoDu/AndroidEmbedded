package com.dogecoding.android_embedded.inertia.surface

/*
* Abstract surface with common functionality.
 */
@OptIn(ExperimentalUnsignedTypes::class)
abstract class Surface(private val blockCount: Int) {

    companion object {
        // Bytes in block
        const val BLOCK_SIZE: Int = 8

        fun getPayloadSize(blockCount: Int): Int {
            return blockCount * BLOCK_SIZE
        }
    }

    protected val arrayOut = UByteArray(getPayloadSize())

    abstract fun getHash(): Int

    abstract fun parseFromArray(byteArray: UByteArray)
    abstract fun getAsArray(): UByteArray

    fun clear() {
        val zeroes = UByteArray(getPayloadSize()) { 0u }
        parseFromArray(zeroes)
    }

    fun getPayloadSize(): Int {
        return getPayloadSize(blockCount)
    }
}