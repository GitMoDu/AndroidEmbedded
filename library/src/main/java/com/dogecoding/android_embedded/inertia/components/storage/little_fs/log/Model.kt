package com.dogecoding.android_embedded.inertia.components.storage.little_fs.log

object Model {
    const val LOG_TAG = 540971179L

    enum class LogCodeEnum {
        BeginFailed,
        FormatFailed,
        Format,
        BeginAfterFormatFailed,
        Mounted,
        Unmounted
    }
}
