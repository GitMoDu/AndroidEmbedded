package com.dogecoding.android_embedded.lola.surface.model


// Generic surface interface.
@OptIn(ExperimentalUnsignedTypes::class)
abstract class Surface(private val blockCount: Int) {

    companion object {
        // Bytes in block
        const val BLOCK_SIZE: Int = 8
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
        return blockCount * BLOCK_SIZE
    }
}