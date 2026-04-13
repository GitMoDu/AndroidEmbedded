package com.dogecoding.android_embedded.inertia.components.log.format

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.dogecoding.android_embedded.R
import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord
import com.dogecoding.android_embedded.inertia.components.log.model.LogType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.forEach

class LogFormatter {

    companion object {
        private val customFormatters = mutableListOf<LogEntryFormatter>()

        fun registerFormatter(formatter: LogEntryFormatter) {
            if (!customFormatters.contains(formatter)) {
                customFormatters.add(0, formatter) // Add to front so app-level can override lib-level
            }
        }

        private fun findCustomFormatter(log: LogDbRecord): LogEntryFormatter? {
            return customFormatters.find { it.canFormat(log) }
        }

        private val sdf = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun formatLogEntry(context: Context, log: LogDbRecord): CharSequence {
            val time = sdf.format(Date(log.getInferredUtc()))
            val spannable = SpannableStringBuilder().append("[$time] ")

            val content: CharSequence = findCustomFormatter(log)?.format(context, log)
                ?: String.format(
                    Locale.US,
                    "[%d:%d] Code(%d) Val(%d)",
                    log.tag, log.instance, log.code, log.value
                )

            spannable.append(content)

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
}
