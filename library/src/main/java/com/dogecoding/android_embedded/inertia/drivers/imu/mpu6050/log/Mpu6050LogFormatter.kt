package com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class Mpu6050LogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[MPU6050] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.ErrorBoot -> "🚫 ErrorBoot"
            Model.LogCodeEnum.ErrorReadMotion -> "📉 ErrorReadMotion"
            Model.LogCodeEnum.ErrorReadTemperature -> "🌡️ ErrorReadTemperature"
            Model.LogCodeEnum.RecoveryAttempt -> "🔧 RecoveryAttempt"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)

        if (log.value != 0) {
            builder.append(" (Val: ${log.value})")
        }

        return builder
    }
}