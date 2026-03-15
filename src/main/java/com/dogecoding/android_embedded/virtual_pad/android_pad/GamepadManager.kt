package com.dogecoding.android_embedded.virtual_pad.android_pad

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.dogecoding.android_embedded.virtual_pad.android_pad.model.ControllerInfo

class GamepadManager(
    context: Context,
    private val viewModel: VirtualPadViewModel
) : InputManager.InputDeviceListener {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager

    fun register() {
        inputManager.registerInputDeviceListener(this, null)
        updateControllerConnection()
    }

    fun unregister() {
        inputManager.unregisterInputDeviceListener(this)
    }

    fun updateControllerConnection() {
        val deviceIds = inputManager.inputDeviceIds
        var firstGamepad: ControllerInfo? = null
        
        for (deviceId in deviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (isGamepad(device)) {
                firstGamepad = ControllerInfo(
                    name = device!!.name,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    descriptor = device.descriptor
                )
                break
            }
        }
        viewModel.updateControllerInfo(firstGamepad)
    }

    private fun isGamepad(device: InputDevice?): Boolean {
        val sources = device?.sources ?: 0
        return (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
                (sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)
    }

    fun handleGenericMotionEvent(ev: MotionEvent): Boolean {
        if (ev.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK) {
            return viewModel.processMotionEvent(ev)
        }
        return false
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (isGamepad(event.device)) {
            return viewModel.processKeyEvent(event.keyCode, event)
        }
        return false
    }

    override fun onInputDeviceAdded(deviceId: Int) = updateControllerConnection()
    override fun onInputDeviceRemoved(deviceId: Int) = updateControllerConnection()
    override fun onInputDeviceChanged(deviceId: Int) = updateControllerConnection()
}