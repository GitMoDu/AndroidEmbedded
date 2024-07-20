package com.dogecoding.embedded.uart_interface

import android.app.Activity
import android.util.Log
import com.dogecoding.embedded.uart_interface.codec.Cobs
import com.dogecoding.embedded.uart_interface.codec.Cobs.DecodeResult
import com.dogecoding.embedded.uart_interface.codec.Cobs.DecodeStatus
import com.dogecoding.embedded.uart_interface.codec.Cobs.EncodeResult
import com.dogecoding.embedded.uart_interface.codec.Cobs.EncodeStatus
import com.dogecoding.embedded.uart_interface.codec.UartCrc
import com.dogecoding.embedded.uart_interface.model.Message
import com.dogecoding.embedded.uart_interface.usb_serial.TimestampSource.Companion.getMillis
import com.dogecoding.embedded.uart_interface.usb_serial.UsbSerial
import com.dogecoding.embedded.uart_interface.usb_serial.UsbSerialListener

// Abstract UART Client with COBS framing and Fletcher16 CRC.
@OptIn(ExperimentalUnsignedTypes::class)
abstract class AbstractUartInterfaceClient(
    baudRate: Int, key: UByteArray
) : UsbSerialListener {

    companion object {
        private const val TAG: String = "UartInterfaceClient"

        val MinMessageSize: Int = Message.Format.Payload.index
        val PacketEndMarker: UByte = 0u
    }

    abstract fun onMessageReceived(header: UByte, payload: UByteArray?)

    private val writeToken = Any()
    private val readToken = Any()

    private val usbSerial = UsbSerial(baudRate)
    private val uartCrc = UartCrc(key)

    private val inBuffer = UByteArray(255)
    private var inSize: Int = 0
    private val inMessage = UByteArray(255)

    private val outMessage = UByteArray(255)
    private val outBuffer = UByteArray(255)

    private var lastValidReceived: Long? = null

    fun getLastValidReceived(): Long? {
        return lastValidReceived
    }

    open fun clear() {
        lastValidReceived = null
        inSize = 0
    }

    protected fun sendMessage(header: UByte, payload: UByteArray? = null): Boolean {
        synchronized(writeToken) {
            outMessage[Message.Format.Header.index] = header
            if (!payload.isNullOrEmpty()) {
                for (i in 0 until payload.count()) {
                    outMessage[Message.Format.Payload.index + i] = payload[i]
                }
                return sendOutMessage(Message.Format.Payload.index + payload.count())
            } else {
                return sendOutMessage(Message.Format.Payload.index)
            }
        }
    }

    fun isConnected(): Boolean {
        return usbSerial.isConnected()
    }

    fun isConnecting(): Boolean {
        return usbSerial.isConnecting()
    }

    fun disconnect() {
        usbSerial.disconnect()
    }

    fun connect(activity: Activity) {
        usbSerial.connect(activity, this)
    }

    private fun sendOutMessage(size: Int): Boolean {
        if (size >= MinMessageSize) {
            val crc = uartCrc.crc(
                outMessage, size - Message.Format.Header.index, Message.Format.Header.index
            )
            outMessage[Message.Format.Crc0.index] = (crc and 255).toUByte()
            outMessage[Message.Format.Crc1.index] = ((crc shr 8) and 255).toUByte()

            val result: EncodeResult = Cobs.encode(
                dst_buf_ptr = outBuffer,
                dst_buf_len = outBuffer.count(),
                src_ptr = outMessage,
                src_len = size
            )

            if (result.status == EncodeStatus.OK) {
                usbSerial.serialWrite(outBuffer, size + 1)
                usbSerial.serialWrite(PacketEndMarker)

                return true
            }
        }

        Log.d(TAG, "SendMessage failed")

        return false
    }

    override fun onRunError(e: Exception) {
        Log.d(TAG, "onRunError: ${e?.message}")
    }

    override fun onNewData(data: ByteArray) {
        synchronized(readToken) {
            parseNewData(data.asUByteArray())
        }
    }

    private fun parseNewData(data: UByteArray) {
        for (i in 0 until data.count()) {
            if (data[i] == PacketEndMarker) {
                if (inSize > 0) {
                    parsePacket(inBuffer, inSize)
                }
                inSize = 0

                if (i + 1 < data.count()) {
                    parseNewData(data.copyOfRange(i + 1, data.count() - 1))
                }
                break
            } else {
                addInBuffer(data[i])
            }
        }
    }

    private fun addInBuffer(data: UByte) {
        inBuffer[inSize++] = data
        if (inSize >= inBuffer.count()) {
            inSize = 0
            Log.d(TAG, "InBuffer overrun")
        }
    }

    private fun parsePacket(packet: UByteArray, size: Int) {
        if (size >= MinMessageSize) {
            val decodeResult: DecodeResult = Cobs.decode(
                dst_buf_ptr = inMessage,
                dst_buf_len = inMessage.count(),
                src_ptr = packet,
                src_len = size,
            )
            if (decodeResult.status == DecodeStatus.OK) {
                parseMessage(inMessage, size - 1)
            } else {
                Log.d(TAG, "Invalid COBS framing")
            }
        }
    }

    private fun parseMessage(packet: UByteArray, size: Int) {
        if (size >= Message.Format.Payload.index) {
            val crc = uartCrc.crc(
                bytes = packet,
                size = size - Message.Format.Header.index,
                offset = Message.Format.Header.index
            )

            val match: Int =
                ((packet[Message.Format.Crc0.index].toInt()) or (packet[Message.Format.Crc1.index].toInt() shl 8))

            if (crc == match) {
                lastValidReceived = getMillis()
                val payloadSize = size - Message.Format.Payload.index
                try {
                    if (payloadSize > 0) {
                        onMessageReceived(
                            packet[Message.Format.Header.index],
                            packet.copyOfRange(Message.Format.Payload.index, size)
                        )
                    } else {
                        onMessageReceived(packet[Message.Format.Header.index], null)
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message.toString())
                }
            } else {
                Log.d(TAG, "Invalid message CRC")
            }
        }
    }

    fun unitTestSingle(header: UByte): Boolean {
        val testBuffer = UByteArray(256)
        val testOutMessage = UByteArray(256)
        val testInMessage = UByteArray(256)

        testOutMessage[Message.Format.Header.index] = header

        val size = Message.getMessageSize(40)
        val outCrc = uartCrc.crc(
            testOutMessage, size - Message.Format.Header.index, Message.Format.Header.index
        )

        testOutMessage[Message.Format.Crc0.index] = (outCrc and 255).toUByte()
        testOutMessage[Message.Format.Crc1.index] = ((outCrc shr 8) and 255).toUByte()

        val outResult: EncodeResult = Cobs.encode(
            dst_buf_ptr = testBuffer,
            dst_buf_len = testBuffer.count(),
            src_ptr = testOutMessage,
            src_len = size
        )

        if (outResult.status != EncodeStatus.OK) {
            return false
        }

        val outSize = size + 1

        val decodeResult: DecodeResult = Cobs.decode(
            dst_buf_ptr = testInMessage,
            dst_buf_len = testInMessage.count(),
            src_ptr = testBuffer,
            src_len = size,
        )


        if (decodeResult.status != DecodeStatus.OK) {
            return false
        }

        val inSize = outSize - 1
        val inCrc = uartCrc.crc(
            bytes = testInMessage,
            size = inSize - Message.Format.Header.index,
            offset = Message.Format.Header.index
        )

        if (outCrc != inCrc) {
            return false
        }

        return true
    }
}