package com.dogecoding.android_embedded.uart_interface

import android.content.Context
import android.util.Log
import com.dogecoding.android_embedded.serial.model.SerialInterface
import com.dogecoding.android_embedded.serial.model.SerialListener
import com.dogecoding.android_embedded.uart_interface.async.AbstractUartAsyncReceiver
import com.dogecoding.android_embedded.uart_interface.codec.Cobs
import com.dogecoding.android_embedded.uart_interface.codec.UartCrc
import com.dogecoding.android_embedded.uart_interface.model.Format
import com.dogecoding.android_embedded.uart_interface.model.Message
import com.dogecoding.android_embedded.uart_interface.model.UartMessengerListener

// Abstract UART messenger with COBS framing and Fletcher16 CRC.
@OptIn(ExperimentalUnsignedTypes::class)
class UartMessenger(
    private val serialInterface: SerialInterface,
    key: UByteArray,
    checkPeriodMillis: Long = 2,
    maxPayloadSize: Int = 250,
    messageStackSize: Int = 10
) : SerialListener {

    companion object {
        private val TAG: String = UartMessenger::class.java.name
        val MinMessageSize: Int = Message.getMessageSize(0)
        val PacketEndMarker: UByte = 0u

        private val UseAsyncDeliver = true
    }

    private val writeToken = Any()
    private val readToken = Any()
    private val deliverToken = Any()

    private val uartCrc = UartCrc(key)
    private val uartParser = UartParser()

    private val inBuffer = UByteArray(255)
    private val inMessage = UByteArray(255)
    private val outMessage = UByteArray(255)
    private val outBuffer = UByteArray(255)

    private var inSize: Int = 0

    var receiveListener: UartMessengerListener? = null

    private val asyncReceiver = object : AbstractUartAsyncReceiver(
        checkPeriodMillis = checkPeriodMillis,
        maxPayloadSize = maxPayloadSize,
        messageStackSize = messageStackSize
    ) {
        override fun onAsyncMessageReceived(header: UByte, payload: UByteArray, payloadSize: Int) {
            synchronized(deliverToken) {
                deliverMessage(header = header, payload = payload, payloadSize = payloadSize)
            }
        }
    }

    fun sendMessage(header: UByte, payload: UByteArray? = null): Boolean {
        if (isConnected()) {
            synchronized(writeToken) {
                outMessage[Format.Header.index] = header
                if (!payload.isNullOrEmpty()) {
                    for (i in 0 until payload.count()) {
                        outMessage[Format.Payload.index + i] = payload[i]
                    }
                    return sendOutMessage(Format.Payload.index + payload.count())
                } else {
                    return sendOutMessage(Format.Payload.index)
                }
            }
        } else {
            return false
        }
    }

    fun isConnected(): Boolean {
        return serialInterface.isConnected()
    }

    fun isConnecting(): Boolean {
        return serialInterface.isConnecting()
    }

    fun disconnect() {
        serialInterface.disconnect()
    }

    fun connect(context: Context) {
        serialInterface.connect(context, this)
    }

    private fun sendOutMessage(size: Int): Boolean {
        if (isConnected() && size >= MinMessageSize) {
            val crc = uartCrc.crc(
                outMessage, size - Format.Header.index, Format.Header.index
            )
            outMessage[Format.Crc0.index] = (crc and 255u).toUByte()
            outMessage[Format.Crc1.index] = ((crc.toInt().shr(8)).toUShort() and 255u).toUByte()

            if (Cobs.encode(target = outBuffer, size = size, source = outMessage) == size + 1) {
                serialInterface.serialWrite(PacketEndMarker)
                serialInterface.serialWrite(outBuffer, size + 1)
                serialInterface.serialWrite(PacketEndMarker)

                return true
            }
        }

        Log.d(TAG, "SendMessage failed")

        return false
    }

    override fun onRunError(e: Exception) {
        Log.d(TAG, "onRunError: ${e.message}")
    }

    override fun onConnected() {
        uartParser.clear()
        receiveListener?.onUartStateChange(true)
    }

    override fun onDisconnected() {
        receiveListener?.onUartStateChange(false)
    }

    override fun onNewData(data: ByteArray) {
        synchronized(readToken) {
            data.forEach {
                inSize = uartParser.parseIn(inBuffer, it.toUByte())
                if (inSize > 0) {
                    parsePacket(inBuffer, inSize)
                }
            }
        }
    }

    private fun parsePacket(packet: UByteArray, size: Int) {
        if (size > MinMessageSize) {
            val decodedSize = Cobs.decode(
                target = inMessage, size = size, source = packet
            )

            if (decodedSize == size - 1) {
                parseMessage(inMessage, size - 1)
            } else {
                Log.d(TAG, "COBS decode size mismatch: expected ${size - 1}, got $decodedSize")
            }
        } else {
            Log.d(TAG, "Invalid packet size: $size (Min: $MinMessageSize)")
        }
    }

    private fun deliverMessage(header: UByte, payload: UByteArray, payloadSize: Int) {
        try {
            if (payloadSize > 0) {
                receiveListener?.onMessageReceived(
                    header,
                    payload.copyOfRange(0, payloadSize)
                )
            } else {
                receiveListener?.onMessageReceived(header, null)
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message.toString())
        }
    }

    private fun parseMessage(message: UByteArray, size: Int) {
        if (size >= Format.Payload.index) {
            val crc = uartCrc.crc(
                bytes = message, size = size - Format.Header.index, offset = Format.Header.index
            )

            val match: UShort = ((message[Format.Crc0.index].toInt()).or(
                message[Format.Crc1.index].toInt().shl(8)
            )).toUShort()

            val payloadSize = size - Format.Payload.index

            if (crc == match) {
                if (UseAsyncDeliver) {
                    var pushed = false
                    if (payloadSize > 0) {
                        pushed = asyncReceiver.receiveAsync(
                            message[Format.Header.index],
                            message.copyOfRange(Format.Payload.index, size),
                            payloadSize
                        )
                    } else {
                        pushed = asyncReceiver.receiveAsync(
                            message[Format.Header.index],
                            null, 0
                        )
                    }
                    if (!pushed) {
                        Log.d(TAG, "Async message dropped, no free slots.")
                    }
                } else {
                    deliverMessage(
                        message[Format.Header.index],
                        message.copyOfRange(Format.Payload.index, size), payloadSize
                    )
                }
            } else {
                Log.d(
                    TAG,
                    "Invalid message CRC for header ${message[Format.Header.index]} payloadSize $payloadSize"
                )
            }
        }
    }
}
