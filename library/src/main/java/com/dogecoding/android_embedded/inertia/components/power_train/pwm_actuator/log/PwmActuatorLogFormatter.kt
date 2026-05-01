package com.dogecoding.android_embedded.inertia.components.power_train.pwm_actuator.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class PwmActuatorLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[PWM ACTUATOR] ")

        val logCode = Model.LogCodeEnum.entries.getOrNull(log.code)
        val message = when (logCode) {
            Model.LogCodeEnum.CalibrationLoadFailed -> "⚠️ CalibrationLoadFailed"
            Model.LogCodeEnum.CalibrationSaveFailed -> "⚠️ CalibrationSaveFailed"
            null -> "❓ Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}
