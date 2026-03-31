package com.dogecoding.android_embedded.uart_interface

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.dogecoding.android_embedded.serial.SerialInterface
import com.dogecoding.android_embedded.uart_interface.model.UartMessengerListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalUnsignedTypes::class)
abstract class AbstractUartInterfaceViewModel(
    key: UByteArray,
    checkPeriodMillis: Long = 2,
    messageStackSize: Int = 10,
    maxPayloadSize: Int = 250
) : ViewModel(),
    UartMessengerListener {

    companion object {
        val TAG: String = AbstractUartInterfaceViewModel::class.java.name

        private const val UART_CHECK_PERIOD_MILLIS: Long = 50
    }

    enum class InterfaceState {
        Disabled, NoDevice, UartConnected
    }

    private val token = Any()
    private var state: InterfaceState = InterfaceState.Disabled

    private var uartMessenger: UartMessenger? = null

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var checkTask: ScheduledFuture<*>? = null

    private val messengerKey = key
    private val messengerCheckPeriodMillis = checkPeriodMillis
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
        Log.d(TAG, "onUartStateChange $uartConnected")
        when (getState()) {
            InterfaceState.Disabled -> {
            }

            InterfaceState.NoDevice -> {
                if (uartConnected) {
                    setState(InterfaceState.UartConnected)
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

    fun sendMessage(header: UByte, payload: UByteArray? = null): Boolean {
        return uartMessenger?.sendMessage(header = header, payload = payload) ?: false
    }

    fun startUpdates(activity: Activity, serialInterface: SerialInterface) {
        Log.d(TAG, "Start check task")

        // Stop any existing updates first to ensure clean state
        if (uartMessenger != null) {
            stopUpdates()
        }

        uartMessenger = UartMessenger(
            serialInterface = serialInterface,
            key = messengerKey,
            checkPeriodMillis = messengerCheckPeriodMillis,
            messageStackSize = messengerMessageStackSize,
            maxPayloadSize = messengerMaxPayloadSize
        )

        setState(InterfaceState.NoDevice)
        checkTask?.cancel(false)
        startCheckTask(activity)
    }

    private fun startCheckTask(activity: Activity) {
        val messenger = uartMessenger ?: return
        messenger.receiveListener = this

        checkTask = scheduler.scheduleWithFixedDelay(
            {
                when (getState()) {
                    InterfaceState.Disabled -> {
                        Log.d(TAG, "Messenger check when disabled")
                        checkTask?.cancel(false)
                    }

                    InterfaceState.NoDevice -> {
                        if (messenger.isConnected()) {
                            setState(InterfaceState.UartConnected)
                            Log.d(TAG, "Messenger connected")

                            onInterfaceStart()
                        } else if (!messenger.isConnecting()) {
                            messenger.connect(activity)
                            Log.d(TAG, "Messenger connect requested")
                        }
                    }

                    InterfaceState.UartConnected -> {
                        Log.d(TAG, "Messenger connected")
                        checkTask?.cancel(false)
                    }
                }
            }, UART_CHECK_PERIOD_MILLIS / 2, UART_CHECK_PERIOD_MILLIS, TimeUnit.MILLISECONDS
        )
    }

    fun reconnect(activity: Activity) {
        when (getState()) {
            InterfaceState.Disabled -> {
                setState(InterfaceState.NoDevice)
            }

            InterfaceState.NoDevice -> {
                startCheckTask(activity)
            }

            InterfaceState.UartConnected -> {
            }
        }
    }

    fun stopUpdates() {
        Log.d(TAG, "Stop check task and Messenger")
        checkTask?.cancel(false)
        checkTask = null

        uartMessenger?.disconnect()
        uartMessenger = null

        if (getState() == InterfaceState.UartConnected) {
            onInterfaceStop()
        }

        setState(InterfaceState.Disabled)
    }
}
