package com.dogecoding.android_embedded.inertia.drivers.optical_flow.mtf0x.log

object Model {
    const val LOG_TAG: Long = 4140776039L

    enum class LogCodeEnum(val value: Int) {
        TaskDriverStartFailed(0),
        WarningSerialReadLimitReached(1),
        ErrorOversizePayload(2),
        ErrorPayloadCrc(3),
        ErrorUnexpectedPayloadSize(4);

        companion object {
            fun fromInt(value: Int) = entries.find { it.value == value }
        }
    }
}
