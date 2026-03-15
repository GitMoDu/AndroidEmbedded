package com.dogecoding.android_embedded.uart_interface.async

import android.util.Log
import com.dogecoding.android_embedded.common.TimestampSource
import com.dogecoding.android_embedded.uart_interface.UartMessenger
import com.dogecoding.android_embedded.uart_interface.model.UartMessengerListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalUnsignedTypes::class)
class UartMultiRequestHandler(
    private val receiveListener: UartMessengerListener,
    private val uartMessenger: UartMessenger,
    private val checkPeriodMillis: Long,
    private val minSendPeriod: Long,
    maxConcurrentRequests: Int
) : UartMessengerListener {

    companion object {
        val TAG: String = UartMultiRequestHandler::class.java.name
    }

    private val scheduler: ScheduledExecutorService =
        Executors.newScheduledThreadPool(maxConcurrentRequests)

    private val token = Any()

    private val requests: ArrayList<UartRequest> = ArrayList(maxConcurrentRequests)

    private var replyHandler: UartRequest? = null

    init {
        uartMessenger.receiveListener = this

        for (i in 0 until maxConcurrentRequests) {
            requests.add(UartRequest())
        }
    }

    fun canRequest(): Boolean {
        synchronized(token) {
            requests.forEach {
                if (it.getState() == UartRequest.State.Done) {
                    return true
                }
            }
        }

        return false
    }

    fun isActive(): Boolean {
        synchronized(token) {
            requests.forEach {
                if (it.getState() != UartRequest.State.Done) {
                    return true
                }
            }
        }

        return false
    }

    private fun elapsedMillisSinceLastSent(): Long {
        val timestamp = TimestampSource.getMillis()
        var elapsed: Long? = null
        synchronized(token) {
            requests.forEach {
                if (elapsed == null || it.elapsed(timestamp) < elapsed!!) {
                    elapsed = it.elapsed(timestamp)
                }
            }
        }

        if (elapsed == null) {
            elapsed = 0
        }

        return elapsed!!
    }

    fun request(
        requestHeader: Int,
        replyHeader: Int?,
        payload: UByteArray?,
        timeout: Long,
        onSend: () -> Unit,
        onReply: (payload: UByteArray?) -> Unit,
        onFail: () -> Unit
    ): Boolean {
        return request(
            requestHeader = requestHeader.toUByte(),
            replyHeader = replyHeader?.toUByte(),
            payload = payload,
            timeout = timeout,
            onSend = onSend,
            onReply = onReply,
            onFail = onFail
        )
    }

    fun request(
        requestHeader: Int, onSend: () -> Unit
    ): Boolean {
        val timestamp = TimestampSource.getMillis()

        if (!canRequest()) {
            return false
        }

        synchronized(token) {
            val handler = getNextFreeHandler()

            if (handler != null) {
                handler.onRequested(
                    timestamp = timestamp,
                    requestHeader = requestHeader.toUByte(),
                    replyHeader = null,
                    payload = null,
                    timeout = 0,
                    onSend = onSend,
                    onReply = { payload: UByteArray? -> },
                    onFail = {})

                startTaskCheck(handler)

                return true
            }
        }

        return false
    }

    fun request(
        requestHeader: UByte,
        replyHeader: UByte?,
        payload: UByteArray?,
        timeout: Long,
        onSend: () -> Unit,
        onReply: (payload: UByteArray?) -> Unit,
        onFail: () -> Unit
    ): Boolean {
        val timestamp = TimestampSource.getMillis()

        if (!canRequest()) {
            return false
        }

        synchronized(token) {
            val handler = getNextFreeHandler()

            if (handler != null) {
                handler.onRequested(
                    timestamp = timestamp,
                    requestHeader = requestHeader,
                    replyHeader = replyHeader,
                    payload = payload,
                    timeout = timeout,
                    onSend = onSend,
                    onReply = onReply,
                    onFail = onFail
                )

                startTaskCheck(handler)

                return true
            }
        }

        return false
    }

    private fun getReplyHandler(header: UByte): UartRequest? {
        for (i in 0 until requests.count()) {
            if (requests[i].getState() == UartRequest.State.WaitingForReply && requests[i].replyHeader == header) {
                return requests[i]
            }
        }

        return null
    }

    override fun onMessageReceived(header: UByte, payload: UByteArray?) {
        val timestamp = TimestampSource.getMillis()
        synchronized(token) {
            replyHandler = getReplyHandler(header)

            if (replyHandler != null) {
                try {
                    replyHandler!!.onReply(payload)
                } catch (ex: Exception) {
                    Log.e(
                        TAG, "Request onReply exception: ${ex.message}"
                    )
                }
                replyHandler!!.clear()
                Log.i(
                    TAG, "($header) Reply to took ${replyHandler!!.elapsedSinceSend(timestamp)} ms"
                )

            } else {
                Log.d(TAG, "Unexpected message")
                try {
                    receiveListener.onMessageReceived(header, payload)
                } catch (ex: Exception) {
                    Log.e(
                        TAG, "Request Receive exception: ${ex.message}"
                    )
                }
            }
        }
    }

    override fun onUartStateChange(uartConnected: Boolean) {
        receiveListener.onUartStateChange(uartConnected)
    }

    private fun runCheck(request: UartRequest, timestamp: Long) {
        when (request.getState()) {
            UartRequest.State.Done -> {
                request.runTask?.cancel(false)
            }

            UartRequest.State.WaitingToSend -> {
                if (request.timedOut(timestamp)) {
                    request.onFail()
                    request.setState(UartRequest.State.Done)
                } else if (uartMessenger.isConnected() && elapsedMillisSinceLastSent() >= minSendPeriod && uartMessenger.sendMessage(
                        request.requestHeader, request.payload
                    )
                ) {
                    request.onSend(timestamp)
                    if (request.replyHeader != null) {
                        request.setState(UartRequest.State.WaitingForReply)
                    } else {
                        request.runTask?.cancel(false)
                        request.setState(UartRequest.State.Done)
                    }
                } else {
                    // Skipped try.
                }
            }

            UartRequest.State.WaitingForReply -> {
                if (request.timedOut(timestamp)) {
                    request.onFail()
                    request.setState(UartRequest.State.Done)
                }
            }
        }
    }

    fun stop() {
        synchronized(token) {
            requests.forEach {
                it.runTask?.cancel(false)
                it.runTask = null
            }
        }
        uartMessenger.disconnect()
    }

    private fun getNextFreeHandler(): UartRequest? {
        requests.forEach {
            if (it.getState() == UartRequest.State.Done) {
                return it
            }
        }

        return null
    }

    private fun startTaskCheck(request: UartRequest) {
        if (request.runTask == null || request.runTask?.isCancelled == true) {
            request.runTask = scheduler.scheduleWithFixedDelay(
                {
                    runCheck(request, TimestampSource.getMillis())
                }, checkPeriodMillis, checkPeriodMillis, TimeUnit.MILLISECONDS
            )
        }
    }
}