package com.dogecoding.android_embedded.uart_interface.async

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalUnsignedTypes::class)
class UartRequest {
    enum class State {
        Done, WaitingToSend, WaitingForReply,
    }

    private val token = Any()

    var startTimestamp: Long = 0
    var sendTimestamp: Long = 0
    var timeout: Long = 100
    var requestHeader: UByte = 0u
    var replyHeader: UByte? = null
    var payload: UByteArray? = null

    private var state = State.Done

    private var sendListener = AtomicReference<(() -> Unit)?>(null)
    private var replyListener = AtomicReference<((payload: UByteArray?) -> Unit)?>(null)
    private var failListener = AtomicReference<(() -> Unit)?>(null)

    var runTask: ScheduledFuture<*>? = null


    fun clear() {
        setState(State.Done)
    }

    fun onRequested(
        timestamp: Long,
        requestHeader: UByte,
        replyHeader: UByte?,
        payload: UByteArray?,
        timeout: Long,
        onSend: () -> Unit,
        onReply: (payload: UByteArray?) -> Unit,
        onFail: () -> Unit
    ) {
        synchronized(token) {
            this.state = State.WaitingToSend
            this.requestHeader = requestHeader
            this.replyHeader = replyHeader
            this.payload = payload
            this.timeout = timeout
            this.startTimestamp = timestamp

            this.sendListener.set { onSend() }
            this.replyListener.set { onReply(it) }
            this.failListener.set { onFail() }
        }
    }

    fun isActive(): Boolean {
        synchronized(token) {
            return state != State.Done
        }
    }

    fun getState(): State {
        synchronized(token) {
            return state
        }
    }

    fun setState(state: State) {
        synchronized(token) {
            this.state = state
        }
    }

    fun timedOut(timestamp: Long): Boolean {
        return elapsed(timestamp) > timeout
    }

    fun elapsed(timestamp: Long): Long {
        return timestamp - startTimestamp
    }

    fun elapsedSinceSend(timestamp: Long): Long {
        return timestamp - sendTimestamp
    }

    fun onSend(timestamp: Long) {
        sendTimestamp = timestamp
        sendListener.get()?.invoke()
    }

    fun onReply(payload: UByteArray?) {
        replyListener.get()?.invoke(payload)
    }

    fun onFail() {
        failListener.get()?.invoke()
    }

}