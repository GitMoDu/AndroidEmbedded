package com.dogecoding.embedded.virtual_pad.controller

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.dogecoding.embedded.uart_interface.usb_serial.TimestampSource.Companion.getMillis
import com.dogecoding.embedded.virtual_pad.VirtualPad
import com.dogecoding.embedded.virtual_pad.WriteVirtualPad
import kotlin.math.abs
import kotlin.math.roundToInt

// Thread-safe Android to IController mapper.
// Listens to native callbacks to update the controller state.
class AndroidVirtualPadMapper(private val focusedMode: Boolean = true) : ControllerInputListener {

    companion object {
        val UINT16_MAX: Int = UByte.MAX_VALUE.toInt() * UByte.MAX_VALUE.toInt()
        val INT16_MAX: Int = Short.MAX_VALUE.toInt()
        val INT16_MIN: Int = Short.MIN_VALUE.toInt()
    }

    private val writeController = WriteVirtualPad()

    private val token = Any()

    private var lastUpdate: Long = 0

    fun getVirtualPadNow(virtualPad: VirtualPad) {
        synchronized(token) {
            virtualPad.copyFrom(writeController.getState())
        }
    }

    fun getElapsedMillisSinceLastUpdate(): Long {
        return getMillis() - lastUpdate
    }

    private fun onKeyEvent(keyCode: Int, pressed: Boolean): Boolean {
        var handled = true
        synchronized(token) {
            if (!writeController.getConnected()) {
                writeController.setConnected(true)
            }
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    writeController.setDPad(
                        pressed,
                        writeController.getDPadDown(),
                        writeController.getDPadLeft(),
                        writeController.getDPadRight()
                    )
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    writeController.setDPad(
                        writeController.getDPadUp(),
                        pressed,
                        writeController.getDPadLeft(),
                        writeController.getDPadRight()
                    )
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    writeController.setDPad(
                        writeController.getDPadUp(),
                        writeController.getDPadDown(),
                        pressed,
                        writeController.getDPadRight()
                    )
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    writeController.setDPad(
                        writeController.getDPadUp(),
                        writeController.getDPadDown(),
                        writeController.getDPadLeft(),
                        pressed
                    )
                }

                KeyEvent.KEYCODE_BUTTON_A -> {
                    writeController.setA(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_B -> {
                    writeController.setB(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_X -> {
                    writeController.setX(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_Y -> {
                    writeController.setY(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_L1 -> {
                    writeController.setL1(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_R1 -> {
                    writeController.setR1(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_THUMBL -> {
                    writeController.setL3(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_THUMBR -> {
                    writeController.setR3(pressed)
                }

                KeyEvent.KEYCODE_MENU -> {
                    writeController.setHome(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_MODE -> {
                    // Capture event to prevent app from exiting
                    if (!focusedMode) {
                        handled = false
                    }
                }

                KeyEvent.KEYCODE_BUTTON_START -> {
                    writeController.setStart(pressed)
                }

                KeyEvent.KEYCODE_BUTTON_SELECT -> {
                    writeController.setSelect(pressed)
                }

                else -> {
                    handled = false
                }
            }

            lastUpdate = getMillis()
        }

        return handled
    }

    override fun onKeyUpEvent(keyCode: Int, event: KeyEvent): Boolean {
        if ((event as? KeyEvent)?.source != null && event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            if (event.repeatCount == 0) {
                return onKeyEvent(keyCode, false)
            }
        }
        return false
    }

    override fun onKeyDownEvent(keyCode: Int, event: KeyEvent): Boolean {
        if ((event as? KeyEvent)?.source != null && event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) {
            if (event.repeatCount == 0) {
                return onKeyEvent(keyCode, true)
            }
        }
        return false
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK && event.action == MotionEvent.ACTION_MOVE) {
            synchronized(token) {
                if (!writeController.getConnected()) {
                    writeController.setConnected(true)
                }

                writeController.setJoy1(
                    getCenteredShort(event, event.device, MotionEvent.AXIS_X),
                    getCenteredShort(event, event.device, MotionEvent.AXIS_Y)
                )

                writeController.setJoy2(
                    getCenteredShort(event, event.device, MotionEvent.AXIS_Z),
                    getCenteredShort(event, event.device, MotionEvent.AXIS_RZ)
                )

                writeController.setL2(getLinearShort(event, event.device, MotionEvent.AXIS_BRAKE))
                writeController.setR2(getLinearShort(event, event.device, MotionEvent.AXIS_GAS))

                val dPadX = getCentered(event, event.device, MotionEvent.AXIS_HAT_X)
                val dPadY = getCentered(event, event.device, MotionEvent.AXIS_HAT_Y)

                writeController.setDPad(dPadY < -0.5f, dPadY > 0.5f, dPadX < -0.5f, dPadX > 0.5f)

                lastUpdate = getMillis()
            }
        }

        return false
    }

    private fun getLinearShort(real: Float, range: Float): Int {
        val value: Float = (real / range) * UINT16_MAX.toFloat()

        val rounded = value.roundToInt()

        if (rounded > UINT16_MAX) {
            return UINT16_MAX
        } else if (rounded < 0) {
            return 0
        } else {
            return rounded
        }
    }

    private fun getCenteredShort(real: Float, range: Float): Short {
        val value: Float
        if (real > 0f) {
            value = (real / (range / 2)) * Short.MAX_VALUE.toFloat()
        } else if (real < 0f) {
            value = (-real / (range / 2)) * Short.MIN_VALUE.toFloat()
        } else {
            return 0
        }

        val rounded = value.roundToInt()

        if (rounded > Short.MAX_VALUE) {
            return Short.MAX_VALUE
        } else if (rounded < Short.MIN_VALUE) {
            return Short.MIN_VALUE
        } else {
            return rounded.toShort()
        }
    }

    private fun getLinearShort(event: MotionEvent, device: InputDevice, axis: Int): Int {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)
        val real = event.getAxisValue(axis)

        if (range?.range == null) {
            return 0
        } else if (real < 0f) {
            return 0
        } else if (real > range.range) {
            return getLinearShort(range.range, range.range)
        } else {
            if (real > range.flat) {
                return getLinearShort(real, range.range)
            } else {
                return 0
            }
        }
    }

    private fun getCentered(event: MotionEvent, device: InputDevice, axis: Int): Float {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)
        val real = event.getAxisValue(axis)

        if (range?.range == null) {
            return 0f
        } else if (abs(real) >= range.range) {
            if (real < 0f) {
                return -1f
            } else {
                return 1f
            }
        } else {
            if (abs(real) > (range.flat / 2)) {
                return real / (range.range / 2)
            } else {
                return 0f
            }
        }
    }

    private fun getCenteredShort(event: MotionEvent, device: InputDevice, axis: Int): Short {
        val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)
        val real = event.getAxisValue(axis)

        if (range?.range == null) {
            return 0
        } else if (abs(real) >= range.range) {
            if (real < 0f) {
                return getCenteredShort(-range.range, range.range)
            } else {
                return getCenteredShort(range.range, range.range)
            }
        } else {
            if (abs(real) > (range.flat / 2)) {
                return getCenteredShort(real, range.range)
            } else {
                return 0
            }
        }
    }
}