package com.dogecoding.embedded.i_controller

import com.dogecoding.embedded.i_controller.model.ControllerInputListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

// Abstract Android to IController mapper and update task.
abstract class IControllerManager(private val updatePeriodMillis: Long) {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var updateTask: ScheduledFuture<*>? = null

    private val controllerMapper = AndroidIControllerMapper()
    private val inputController = IController()

    fun getElapsedMillisSinceLastUpdate(): Long {
        return controllerMapper.getElapsedMillisSinceLastUpdate()
    }

    abstract fun onControllerUpdate(input: IController)
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
                controllerMapper.getIInputNow(inputController)
                onControllerUpdate(inputController)
            }, updatePeriodMillis, updatePeriodMillis, TimeUnit.MILLISECONDS
        )
    }
}