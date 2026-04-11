package com.dogecoding.android_embedded.inertia.components.log.model

@OptIn(ExperimentalUnsignedTypes::class)
enum class LogType(val value: UByte) {
    Debug(0u),
    Info(1u),
    Warning(2u),
    Error(3u);

    companion object {
        fun fromUByte(value: UByte) = entries.firstOrNull { it.value == value } ?: Debug
    }
}
