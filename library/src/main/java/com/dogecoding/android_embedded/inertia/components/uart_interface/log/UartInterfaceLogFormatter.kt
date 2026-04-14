package com.dogecoding.android_embedded.inertia.components.uart_interface.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.uart_interface.log.Model

class UartInterfaceLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[UART] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.Connected -> "🟢 ${logCode.name}"
            Model.LogCodeEnum.Disconnected -> "🔴 ${logCode.name}"
            Model.LogCodeEnum.ConnectingTimeout -> "⏳ ${logCode.name}"
            Model.LogCodeEnum.ErrorRxUnrecognizedHeader,
            Model.LogCodeEnum.ErrorRxUnexpectedMessageInState,
            Model.LogCodeEnum.ErrorRxUnexpectedSize,
            Model.LogCodeEnum.ErrorRxStartTimeout,
            Model.LogCodeEnum.ErrorRxCrc,
            Model.LogCodeEnum.ErrorRxTooShort,
            Model.LogCodeEnum.ErrorRxTooLong,
            Model.LogCodeEnum.ErrorRxEndTimeout,
            Model.LogCodeEnum.ErrorRxUnknown,
            Model.LogCodeEnum.ErrorTxStartTimeout,
            Model.LogCodeEnum.ErrorTxDataTimeout,
            Model.LogCodeEnum.ErrorTxEndTimeout,
            Model.LogCodeEnum.ErrorTxUnknown -> "⚠️ ${logCode.name}"
            else -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)

        return builder
    }
}