package com.dogecoding.android_embedded.virtual_pad.controller

import com.dogecoding.android_embedded.virtual_pad.VirtualPad
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

// Abstract Android to IController mapper and update task.
abstract class VirtualPadUpdater(private val updatePeriodMillis: Long) {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var updateTask: ScheduledFuture<*>? = null

    private val controllerMapper = AndroidVirtualPadMapper()
    private val inputController = VirtualPad()

    fun getElapsedMillisSinceLastUpdate(): Long {
        return controllerMapper.getElapsedMillisSinceLastUpdate()
    }

    abstract fun onControllerUpdate(input: VirtualPad)
    abstract fun onStart()

    fun getInputControllerListener(): ControllerInputListener {
        return controllerMapper
    }

    fun clear() {
        inputController.clear()
    }

    fun stop() {
        updateTask?.cancel(false)
    }

    fun start() {
        if (updateTask == null
            || updateTask?.isCancelled == true
        ) {
            updateTask?.cancel(false)
            startUpdates()
        }
    }

    private fun startUpdates() {
        updateTask?.cancel(false)
        updateTask = scheduler.scheduleWithFixedDelay(
            {
                controllerMapper.getVirtualPadNow(inputController)
                onControllerUpdate(inputController)
            }, updatePeriodMillis, updatePeriodMillis, TimeUnit.MILLISECONDS
        )
    }
}