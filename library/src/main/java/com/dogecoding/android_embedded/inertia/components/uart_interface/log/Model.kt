package com.dogecoding.android_embedded.inertia.components.uart_interface.log

object Model {
    const val LOG_TAG: Long = 3851162775L

    enum class LogCodeEnum {
        // Connection state change codes.
        Connected,
        Disconnected,
        ConnectingTimeout,

        // Interface handling error codes.
        ErrorRxUnrecognizedHeader,
        ErrorRxUnexpectedMessageInState,
        ErrorRxUnexpectedSize,

        // Uart RX error codes.
        ErrorRxStartTimeout,
        ErrorRxCrc,
        ErrorRxTooShort,
        ErrorRxTooLong,
        ErrorRxEndTimeout,
        ErrorRxUnknown,

        // Uart TX error codes.
        ErrorTxStartTimeout,
        ErrorTxDataTimeout,
        ErrorTxEndTimeout,
        ErrorTxUnknown
    }
}