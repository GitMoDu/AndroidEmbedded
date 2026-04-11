package com.dogecoding.android_embedded.inertia.components.uart_interface

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class UartInterfaceLogFormatter : TagLogFormatter(3851162775L) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[UART] ")

        val message = when (log.code) {
            0 -> "Connected"
            1 -> "Disconnected"
            2 -> "ConnectingTimeout"
            3 -> "ErrorRxUnrecognizedHeader"
            4 -> "ErrorRxUnexpectedMessageInState"
            5 -> "ErrorRxUnexpectedSize"
            6 -> "ErrorRxStartTimeout"
            7 -> "ErrorRxCrc"
            8 -> "ErrorRxTooShort"
            9 -> "ErrorRxTooLong"
            10 -> "ErrorRxEndTimeout"
            11 -> "ErrorRxUnknown"
            12 -> "ErrorTxStartTimeout"
            13 -> "ErrorTxDataTimeout"
            14 -> "ErrorTxEndTimeout"
            15 -> "ErrorTxUnknown"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}