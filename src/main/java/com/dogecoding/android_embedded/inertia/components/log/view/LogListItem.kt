package com.dogecoding.android_embedded.inertia.components.log.view

import com.dogecoding.android_embedded.inertia.components.log.database.LogDbRecord


sealed class LogListItem {
    data class SessionHeader(val sessionId: Long) : LogListItem()
    data class Entry(val logDbRecord: LogDbRecord) : LogListItem()
}