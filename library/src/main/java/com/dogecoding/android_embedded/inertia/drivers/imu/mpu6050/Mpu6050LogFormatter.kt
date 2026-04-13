package com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.TagLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord

class Mpu6050LogFormatter : TagLogFormatter(867172820L) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[MPU6050] ")

        val message = when (log.code) {
            0 -> "ErrorBoot"
            1 -> "ErrorReadMotion"
            2 -> "ErrorReadTemperature"
            3 -> "RecoveryAttempt"
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)

        if (log.code == 3) {
            builder.append(" count=")
            builder.append(log.value.toString())
        }

        return builder
    }
}

