package com.dogecoding.android_embedded.uart_interface.server_client

import android.content.Context
import android.util.Log
import com.dogecoding.android_embedded.serial.SerialInterface
import com.dogecoding.android_embedded.uart_interface.UartMessenger
import com.dogecoding.android_embedded.uart_interface.model.UartMessengerListener

@OptIn(ExperimentalUnsignedTypes::class)
abstract class AbstractUartInterface(
    key: UByteArray,
    maxPayloadSize: Int = 250,
    messageStackSize: Int = 10
) : UartMessengerListener {

    companion object {
        private val TAG: String = AbstractUartInterface::class.java.name
    }

    enum class InterfaceState {
        Disabled, NoDevice, UartConnected
    }

    private val token = Any()
    private var state: InterfaceState = InterfaceState.Disabled

    private var uartMessenger: UartMessenger? = null

    private val messengerKey = key
    private val messengerCheckPeriodMillis: Long = 2
    private val messengerMessageStackSize = messageStackSize
    private val messengerMaxPayloadSize = maxPayloadSize

    abstract fun onInterfaceStart()
    abstract fun onInterfaceStop()

    protected fun getState(): InterfaceState {
        synchronized(token) {
            return state
        }
    }

    private fun setState(interfaceState: InterfaceState) {
        synchronized(token) {
            this.state = interfaceState
        }
    }

    final override fun onUartStateChange(uartConnected: Boolean) {
        Log.d(TAG, "State: $uartConnected")
        when (getState()) {
            InterfaceState.Disabled -> {
            }

            InterfaceState.NoDevice -> {
                if (uartConnected) {
                    setState(InterfaceState.UartConnected)
                    onInterfaceStart()
                }
            }

            InterfaceState.UartConnected -> {
                if (!uartConnected) {
                    setState(InterfaceState.NoDevice)
                    onInterfaceStop()
                }
            }
        }
    }

    override fun onMessageReceived(header: UByte, payload: UByteArray?) {
//        Log.d(TAG, "Message received")
    }

    protected fun sendMessage(header: Int, payload: UByteArray? = null): Boolean {
        return sendMessage(header.toUByte(), payload)
    }

    protected fun sendMessage(header: UByte, payload: UByteArray? = null): Boolean {
        return uartMessenger?.sendMessage(header = header, payload = payload) ?: false
    }

    fun start(context: Context, serialInterface: SerialInterface) {
        Log.d(TAG, "Start UART Interface")

        // Stop any existing updates first to ensure clean state
        stop()

        // Create new messenger with the provided serial interface.
        uartMessenger = UartMessenger(
            serialInterface = serialInterface,
            key = messengerKey,
            checkPeriodMillis = messengerCheckPeriodMillis,
            messageStackSize = messengerMessageStackSize,
            maxPayloadSize = messengerMaxPayloadSize
        )
        uartMessenger!!.receiveListener = this
        setState(InterfaceState.NoDevice)

        uartMessenger!!.connect(context)
    }

    fun stop() {
        Log.d(TAG, "Stop UART Interface")
        if (getState() == InterfaceState.UartConnected) {
            onInterfaceStop()
        }

        uartMessenger?.receiveListener = null
        uartMessenger?.disconnect()
        uartMessenger = null

        setState(InterfaceState.Disabled)
    }
}
