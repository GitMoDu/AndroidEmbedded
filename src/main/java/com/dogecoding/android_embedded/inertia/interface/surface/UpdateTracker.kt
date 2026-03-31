package com.dogecoding.android_embedded.inertia.`interface`.surface

class UpdateTracker(private val updatePeriod: Long) {
    private var updating: Boolean = false
    private var lastUpdate: Long = 0

    fun elapsed(timestamp: Long): Long {
        return timestamp - lastUpdate
    }

    fun onUpdated(timestamp: Long) {
        lastUpdate = timestamp
        updating = false
    }

    fun onStart() {
        updating = true
    }

    fun isUpdating(): Boolean {
        return updating
    }

    fun reset(timestamp: Long) {
        lastUpdate = timestamp - updatePeriod
    }

    fun elapsedOver(timestamp: Long): Long {
        val elapsed = elapsed(timestamp)
        if (elapsed > updatePeriod) {
            return elapsed - updatePeriod
        } else {
            return 0
        }
    }
}