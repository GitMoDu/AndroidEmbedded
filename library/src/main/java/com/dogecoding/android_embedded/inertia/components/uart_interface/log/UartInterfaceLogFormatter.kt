package com.dogecoding.android_embedded.inertia.components.uart_interface.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class UartInterfaceLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[UART API] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.Connected -> "🟢 Connected"
            Model.LogCodeEnum.Disconnected -> "🔴 Disconnected"
            Model.LogCodeEnum.ConnectingTimeout -> "⏳ Connecting Timeout"
            Model.LogCodeEnum.ErrorRxUnrecognizedHeader -> "⚠️: RX Unrecognized Header"
            Model.LogCodeEnum.ErrorRxUnexpectedMessageInState -> "⚠️: RX Unexpected Message in State"
            Model.LogCodeEnum.ErrorRxUnexpectedSize -> "⚠️ RX Unexpected Size"
            Model.LogCodeEnum.ErrorRxStartTimeout -> "⚠️ RX Start Timeout"
            Model.LogCodeEnum.ErrorRxCrc -> "⚠️ RX CRC"
            Model.LogCodeEnum.ErrorRxTooShort -> "⚠️ RX Too Short"
            Model.LogCodeEnum.ErrorRxTooLong -> "⚠️ RX Too Long"
            Model.LogCodeEnum.ErrorRxEndTimeout -> "⚠️ RX End Timeout"
            Model.LogCodeEnum.ErrorRxUnknown -> "⚠️ RX Unknown"
            Model.LogCodeEnum.ErrorTxStartTimeout -> "⚠️ TX Start Timeout"
            Model.LogCodeEnum.ErrorTxDataTimeout -> "⚠️ TX Data Timeout"
            Model.LogCodeEnum.ErrorTxEndTimeout -> "⚠️ TX End Timeout"
            Model.LogCodeEnum.ErrorTxUnknown -> "⚠️ TX Unknown"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)

        if (log.value != 0) {
            builder.append(" (Val: ${log.value})")
        }

        return builder
    }
}
