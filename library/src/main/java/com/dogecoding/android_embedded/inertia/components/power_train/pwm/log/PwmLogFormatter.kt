package com.dogecoding.android_embedded.inertia.components.power_train.pwm.log

import android.content.Context
import android.text.SpannableStringBuilder
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.format.TagLogFormatter

class PwmLogFormatter : TagLogFormatter(Model.LOG_TAG) {

    override fun format(context: Context, log: LogDbRecord): CharSequence {
        val builder = SpannableStringBuilder()
        builder.append("[PWM] ")

        val message = when (log.code) {
            else -> "Unknown Code(${log.code})"
        }

        builder.append(message)
        return builder
    }
}
