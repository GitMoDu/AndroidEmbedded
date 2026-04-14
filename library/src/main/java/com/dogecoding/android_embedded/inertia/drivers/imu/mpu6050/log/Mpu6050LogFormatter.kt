package com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class Mpu6050LogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[MPU6050] ")

        val message = when (log.code) {
            Model.LogCodeEnum.RecoveryAttempt.ordinal -> "${Model.LogCodeEnum.RecoveryAttempt.name}(${log.value})"
            Model.LogCodeEnum.ErrorReadTemperature.ordinal -> Model.LogCodeEnum.ErrorReadTemperature.name
            Model.LogCodeEnum.ErrorReadMotion.ordinal -> Model.LogCodeEnum.ErrorReadMotion.name
            Model.LogCodeEnum.ErrorBoot.ordinal -> Model.LogCodeEnum.ErrorBoot.name
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)

        return builder
    }
}