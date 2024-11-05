package com.dogecoding.embedded.uart_interface

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.dogecoding.embedded.uart_interface.model.UartMessengerListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalUnsignedTypes::class)
abstract class AbstractUartInterfaceViewModel(
    baudRate: Int, key: UByteArray,
    checkPeriodMillis: Long = 2,
    messageStackSize: Int = 10,
    maxPayloadSize: Int = 250
) : ViewModel(),
    UartMessengerListener {

    companion object {
        val TAG: String = AbstractUartInterfaceViewModel::class.java.name

        private val UART_CHECK_PERIOD_MILLIS: Long = 50
    }

    enum class InterfaceState {
        Disabled, NoDevice, UartConnected
    }

    private val token = Any()
    private var state: InterfaceState = InterfaceState.Disabled

    private val uartMessenger = UartMessenger(
        baudRate = baudRate,
        key = key,
        checkPeriodMillis = checkPeriodMillis,
        messageStackSize = messageStackSize,
        maxPayloadSize = maxPayloadSize
    )


    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var checkTask: ScheduledFuture<*>? = null

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
                }
            }
        }
    }

    fun startUpdates(activity: Activity) {
        Log.d(TAG, "Start check task")

        setState(InterfaceState.NoDevice)
        checkTask?.cancel(false)
        startCheckTask(activity)
    }

    private fun startCheckTask(activity: Activity) {
        uartMessenger.receiveListener = this

        checkTask = scheduler.scheduleWithFixedDelay(
            {
                when (getState()) {
                    InterfaceState.Disabled -> {
                        Log.d(TAG, "Messenger check when disabled")
                        checkTask?.cancel(false)
                    }

                    InterfaceState.NoDevice -> {
                        if (uartMessenger.isConnected()) {
                            setState(InterfaceState.UartConnected)
                            Log.d(TAG, "Messenger connected")

                            onInterfaceStart()
                        } else if (!uartMessenger.isConnecting()) {
                            uartMessenger.connect(activity)
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
        uartMessenger.disconnect()

        when (getState()) {
            InterfaceState.Disabled -> {
            }

            InterfaceState.NoDevice -> {
            }

            InterfaceState.UartConnected -> {
                onInterfaceStop()
            }
        }

        setState(InterfaceState.Disabled)
    }
}