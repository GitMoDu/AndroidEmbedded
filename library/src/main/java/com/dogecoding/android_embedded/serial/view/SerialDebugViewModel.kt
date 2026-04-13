package com.dogecoding.android_embedded.serial.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogecoding.android_embedded.serial.model.SerialInterface
import com.dogecoding.android_embedded.serial.model.SerialListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@OptIn(ExperimentalUnsignedTypes::class)
class SerialDebugViewModel : ViewModel(), SerialListener {

    companion object {
        val pingData = "PING".toByteArray()
        val pongData = "PONG".toByteArray()
    }

    private var serialInterface: SerialInterface? = null

    private val _logEvents = MutableSharedFlow<LogEvent>()
    val logEvents: SharedFlow<LogEvent> = _logEvents.asSharedFlow()

    val loopbackEnabled = MutableStateFlow(false)
    val periodicSendEnabled = MutableStateFlow(false)

    private var periodicJob: Job? = null

    sealed class LogEvent {
        data class Data(val data: ByteArray, val isRx: Boolean) : LogEvent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Data

                if (isRx != other.isRx) return false
                if (!data.contentEquals(other.data)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = isRx.hashCode()
                result = 31 * result + data.contentHashCode()
                return result
            }
        }

        data class Message(val message: String) : LogEvent()
    }

    fun setSerialInterface(serial: SerialInterface?) {
        this.serialInterface = serial
        if (serial == null) {
            stopPeriodicSend()
        }
    }

    fun toggleLoopback(enabled: Boolean) {
        loopbackEnabled.value = enabled
    }

    fun togglePeriodicSend(enabled: Boolean) {
        periodicSendEnabled.value = enabled
        if (enabled) {
            startPeriodicSend()
        } else {
            stopPeriodicSend()
        }
    }

    private fun startPeriodicSend() {
        periodicJob?.cancel()
        periodicJob = viewModelScope.launch(Dispatchers.IO) {

            while (isActive) {
                if (periodicSendEnabled.value) {
                    writeToSerial(pingData)
                    _logEvents.emit(LogEvent.Data(pingData, false))
                }
                delay(1000) // 1 second interval
            }
        }
    }

    private fun stopPeriodicSend() {
        periodicJob?.cancel()
        periodicJob = null
    }

    private fun writeToSerial(data: ByteArray) {
        serialInterface?.let { serial ->
            if (serial.isConnected()) {
                val uData = data.toUByteArray()
                serial.serialWrite(uData, uData.size)
            }
        }
    }

    // SerialListener Implementation
    override fun onConnected() {
        viewModelScope.launch {
            _logEvents.emit(LogEvent.Message("Connected"))
        }
    }

    override fun onDisconnected() {
        viewModelScope.launch {
            _logEvents.emit(LogEvent.Message("Disconnected"))
            stopPeriodicSend()
        }
    }

    override fun onNewData(data: ByteArray) {
        viewModelScope.launch {
            _logEvents.emit(LogEvent.Data(data, true))

            if (loopbackEnabled.value) {
                writeToSerial(data)
                _logEvents.emit(LogEvent.Data(data, false))
            }
        }
    }

    override fun onRunError(e: Exception) {
        viewModelScope.launch {
            _logEvents.emit(LogEvent.Message("Error: ${e.message}"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicSend()
    }
}
