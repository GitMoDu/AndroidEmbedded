package com.dogecoding.android_embedded.inertia.log

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.inertia.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.log.model.LogType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

object LogFormatter {
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    fun formatLogEntry(context: Context, log: LogDbRecord): CharSequence {
        val spannable = SpannableStringBuilder()
        val time = sdf.format(Date(log.getInferredUtc()))

        val header = String.format(
            Locale.US,
            "[%s] [%d:%d] Code(%d) Val(%d)",
            time, log.tag, log.instance, log.code, log.value
        )

        spannable.append(header)

        val color = when (LogType.fromUByte(log.type.toUByte())) {
            LogType.Error -> ContextCompat.getColor(context, R.color.log_text_error)
            LogType.Warning -> ContextCompat.getColor(context, R.color.log_text_warning)
            LogType.Info -> ContextCompat.getColor(context, R.color.log_text_info)
            LogType.Debug -> ContextCompat.getColor(context, R.color.log_text_debug)
        }

        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    fun formatLogs(context: Context, logs: List<LogDbRecord>): CharSequence {
        val builder = SpannableStringBuilder()
        logs.forEach { log ->
            builder.append(formatLogEntry(context, log))
        }
        return builder
    }
}
