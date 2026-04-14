package com.dogecoding.android_embedded.inertia.drivers.i2c.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class I2cInterfaceLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[I2C] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.ErrorTimeout -> "⌛ ErrorTimeout"
            Model.LogCodeEnum.RecoveryAttempt -> "🔧 RecoveryAttempt"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}