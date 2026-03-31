@file:OptIn(ExperimentalUnsignedTypes::class)

package com.dogecoding.android_embedded.uart_interface.async

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A thread-safe asynchronous message receiver that handles UART messages in a prioritized FIFO queue.
 * Uses a fixed-size pool of message containers to avoid GC pressure.
 *
 * @param checkPeriodMillis The delay between processing cycles.
 * @param maxPayloadSize Maximum expected payload size per message.
 * @param messageStackSize Capacity of the message buffer (Pool size).
 */
abstract class AbstractUartAsyncReceiver(
    private val checkPeriodMillis: Long = 50,
    private val maxPayloadSize: Int = 250,
    messageStackSize: Int = 10
) {
    companion object {
        val TAG: String = AbstractUartAsyncReceiver::class.java.name
    }

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var runTask: ScheduledFuture<*>? = null
    private val isRunning = AtomicBoolean(false)

    private class AsyncMessage(maxPayloadSize: Int) {
        var header: UByte = 0.toUByte()
        val payload: UByteArray = UByteArray(maxPayloadSize)
        var payloadSize: Int = 0
    }

    // Fixed pool to avoid allocations at runtime
    private val freePool = ArrayBlockingQueue<AsyncMessage>(messageStackSize)
    // Proper FIFO queue for pending messages
    private val pendingQueue = LinkedBlockingQueue<AsyncMessage>()

    abstract fun onAsyncMessageReceived(header: UByte, payload: UByteArray, payloadSize: Int)

    init {
        for (i in 0 until messageStackSize) {
            freePool.add(AsyncMessage(maxPayloadSize))
        }
    }

    /**
     * Stops the processing task.
     */
    fun stop() {
        synchronized(this) {
            runTask?.cancel(false)
            runTask = null
            isRunning.set(false)
        }
    }

    /**
     * Shuts down the receiver and its internal executor.
     */
    fun release() {
        stop()
        scheduler.shutdown()
    }

    /**
     * Adds a message to the queue to be processed asynchronously.
     * Returns false if the buffer is full.
     */
    fun receiveAsync(header: UByte, payload: UByteArray?, payloadSize: Int): Boolean {
        if (payload != null && payloadSize > maxPayloadSize) {
            Log.e(TAG, "Payload size $payloadSize exceeds maximum $maxPayloadSize")
            return false
        }

        // Get a container from the pool
        val message = freePool.poll() ?: run {
            Log.w(TAG, "Async buffer overflow! Dropping message $header. Increase messageStackSize.")
            return false
        }

        message.header = header
        message.payloadSize = payloadSize
        payload?.let {
            for (i in 0 until payloadSize) {
                message.payload[i] = it[i]
            }
        }

        pendingQueue.offer(message)
        startProcessing()
        return true
    }

    private fun startProcessing() {
        if (isRunning.compareAndSet(false, true)) {
            synchronized(this) {
                if (runTask == null || runTask?.isCancelled == true) {
                    runTask = scheduler.scheduleWithFixedDelay(
                        ::runCheck, 0, checkPeriodMillis, TimeUnit.MILLISECONDS
                    )
                }
            }
        }
    }

    private fun runCheck() {
        val messagesToProcess = mutableListOf<AsyncMessage>()
        pendingQueue.drainTo(messagesToProcess)

        if (messagesToProcess.isEmpty()) {
            stopTask()
            return
        }

        for (message in messagesToProcess) {
            try {
                onAsyncMessageReceived(message.header, message.payload, message.payloadSize)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onAsyncMessageReceived", e)
            } finally {
                // Return container to pool for reuse
                freePool.offer(message)
            }
        }

        // If nothing was added while we were processing, stop the task
        if (pendingQueue.isEmpty()) {
            stopTask()
        }
    }

    private fun stopTask() {
        synchronized(this) {
            runTask?.cancel(false)
            runTask = null
            isRunning.set(false)
        }
        
        // Final safety check for race conditions
        if (!pendingQueue.isEmpty()) {
            startProcessing()
        }
    }
}
