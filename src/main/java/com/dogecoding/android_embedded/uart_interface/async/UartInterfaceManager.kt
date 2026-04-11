package com.dogecoding.android_embedded.uart_interface.async

import android.util.Log
import com.dogecoding.android_embedded.serial.model.SerialInterface
import com.dogecoding.android_embedded.uart_interface.UartMessenger
import com.dogecoding.android_embedded.uart_interface.model.UartMessengerListener

@OptIn(ExperimentalUnsignedTypes::class)
open class UartInterfaceManager(
    private val uartMessengerListener: UartMessengerListener,
    requestCheckPeriodMillis: Long,
    minSendPeriod: Long,
    maxConcurrentRequests: Int,
    serialInterface: SerialInterface,
    key: UByteArray,
    uartCheckPeriodMillis: Long = 2,
    maxPayloadSize: Int = 250,
    messageStackSize: Int = 10
) : UartMessengerListener {

    companion object {
        val TAG: String = UartInterfaceManager::class.java.name
        private const val INTERFACE_STATE_UPDATE_PERIOD: Long = 100
    }

    enum class State {
        NoLifeCycle, NoSerial, NoDevice, RuntimeUpdate
    }

    private val messenger = UartMessenger(
        serialInterface = serialInterface,
        key = key,
        checkPeriodMillis = uartCheckPeriodMillis,
        maxPayloadSize = maxPayloadSize,
        messageStackSize = messageStackSize
    )

    private val requestsHandler by lazy {
        UartMultiRequestHandler(
            receiveListener = this,
            uartMessenger = messenger,
            checkPeriodMillis = requestCheckPeriodMillis,
            minSendPeriod = minSendPeriod,
            maxConcurrentRequests = maxConcurrentRequests
        )
    }

    private val token = Any()
    private var state: State = State.NoLifeCycle

    fun canRequest(): Boolean {
        return getState() == State.RuntimeUpdate && requestsHandler.canRequest()
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
        return requestsHandler.request(
            requestHeader = requestHeader.toUByte(),
            replyHeader = replyHeader?.toUByte(),
            payload = payload,
            timeout = timeout,
            onSend = onSend,
            onReply = onReply,
            onFail = onFail
        )
    }

    private fun getState(): State {
        synchronized(token) {
            return state
        }
    }

    private fun setState(value: State) {
        synchronized(token) {
            state = value
        }
    }

    override fun onMessageReceived(header: UByte, payload: UByteArray?) {
        when (getState()) {
            State.RuntimeUpdate -> {
                uartMessengerListener.onMessageReceived(header, payload)
            }

            else -> {
                Log.e(TAG, "Received UART message out of lifecycle")
                onStop()
            }
        }
    }

//    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//        Log.d(TAG, "Lifecycle ${event.name}")
//        when (event) {
//            Lifecycle.Event.ON_CREATE -> {}
//            Lifecycle.Event.ON_START -> {}
//            Lifecycle.Event.ON_RESUME -> {
//                val resumeState: State = getState()
//                when (resumeState) {
//                    State.NoLifeCycle -> {
//                        setState(State.NoSerial)
//                    }
//
//                    else -> {}
//                }
//            }
//
//            Lifecycle.Event.ON_PAUSE -> {
//
//
//            }
//
//            Lifecycle.Event.ON_STOP -> {
//                when (getState()) {
//                    State.NoLifeCycle -> {
//                    }
//
//                    else -> {
//                        setState(State.NoLifeCycle)
//                        onStop()
//                    }
//                }
//            }
//
//            Lifecycle.Event.ON_DESTROY -> {}
//            Lifecycle.Event.ON_ANY -> {}
//        }
//    }

    fun onStop() {
        requestsHandler.stop()
    }

    override fun onUartStateChange(uartConnected: Boolean) {
        Log.d(TAG, "onUartStateChange $uartConnected")
        if (uartConnected) {
            when (getState()) {
                State.NoSerial -> {
                    setState(State.NoDevice)
                }

                State.NoLifeCycle -> {
                    return
                }

                else -> {}
            }

        } else {
            requestsHandler.stop()
            when (getState()) {
                State.NoDevice -> {
                    setState(State.NoSerial)
                }

                State.NoLifeCycle, State.NoSerial -> {}
                else -> {
                    setState(State.NoSerial)
                }
            }
            uartMessengerListener.onUartStateChange(false)
        }
    }
}
