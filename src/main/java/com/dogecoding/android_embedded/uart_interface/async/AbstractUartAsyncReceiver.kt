@file:OptIn(ExperimentalUnsignedTypes::class)

package com.dogecoding.android_embedded.uart_interface.async

import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

abstract class AbstractUartAsyncReceiver(
    private val checkPeriodMillis: Long = 2, messageStackSize: Int = 10, maxPayloadSize: Int = 250
) {
    companion object {
        val TAG: String = AbstractUartAsyncReceiver::class.java.name
    }

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var runTask: ScheduledFuture<*>? = null

    private val token = Any()

    private class AsyncMessage(maxPayloadSize: Int) {
        var header: UByte = 0.toUByte()
        val payload: UByteArray = UByteArray(maxPayloadSize) { 0.toUByte() }
        var payloadSize: Int = 0
        var pending: Boolean = false
    }

    private val asyncMessages: ArrayList<AsyncMessage> = ArrayList()
    private val asyncMessage = AsyncMessage(maxPayloadSize)

    abstract fun onAsyncMessageReceived(header: UByte, payload: UByteArray, payloadSize: Int)

    init {
        synchronized(token) {
            for (i in 0 until messageStackSize) {
                asyncMessages.add(AsyncMessage(maxPayloadSize))
            }
        }
    }

    fun stop() {
        runTask?.cancel(false)
    }

    fun receiveAsync(header: UByte, payload: UByteArray?, payloadSize: Int): Boolean {
        var found = false

        if (payload != null && payloadSize != payload.count()) {
            Log.d(TAG, "Wrong size")
            return false
        }

        synchronized(token) {
            for (i in 0 until asyncMessages.count()) {
                val message: AsyncMessage = asyncMessages[i]
                if (!message.pending) {
                    found = true
                    message.pending = true
                    message.header = header
                    if (payload != null) {
                        message.payloadSize = payload.count()
                        for (j in 0 until message.payloadSize) {
                            message.payload[j] = payload[j]
                        }
                    } else {
                        message.payloadSize = 0
                    }
                    break
                }
            }

            runIfNot()
        }

        return found
    }

    private fun runCheck() {
        asyncMessage.pending = false
        synchronized(token) {
            for (i in 0 until asyncMessages.count()) {
                val message: AsyncMessage = asyncMessages[i]

                if (message.pending) {
                    asyncMessages[i].pending = false

                    asyncMessage.pending = true
                    asyncMessage.header = message.header
                    asyncMessage.payloadSize = message.payloadSize
                    for (j in 0 until message.payloadSize) {
                        asyncMessage.payload[j] = message.payload[j]
                    }
                    break
                }
            }
        }

        if (asyncMessage.pending) {
            onAsyncMessageReceived(
                asyncMessage.header, asyncMessage.payload, asyncMessage.payloadSize
            )
        }
    }

    private fun hasPending(): Boolean {
        var found = false
        synchronized(token) {
            for (i in 0 until asyncMessages.count()) {
                if (asyncMessages[i].pending) {
                    found = true
                    break
                }
            }
        }

        return found
    }

    private fun runIfNot() {
        if (runTask == null || runTask?.isCancelled == true) {
            runTask = scheduler.scheduleWithFixedDelay(
                {
                    if (runTask != null && runTask?.isCancelled != true) {
                        runCheck()
                    }
                }, 0, checkPeriodMillis, TimeUnit.MILLISECONDS
            )
        }
    }
}