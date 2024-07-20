package com.dogecoding.embedded.i_controller

import com.dogecoding.embedded.i_controller.model.ControllerState
import com.dogecoding.embedded.i_controller.model.DPadEnum
import com.dogecoding.embedded.i_controller.model.MainButtonEnum
import com.dogecoding.embedded.i_controller.model.MenuButtonEnum

// IInputController (read) interface.
// Modelled after RetroArch's RetroPad.
open class IController {
    protected val controllerState = ControllerState()

    protected fun buttonMask(bitIndex: Int): UByte {
        return (1 shl bitIndex).toUByte()
    }

    private fun getButton(buttonsState: UByte, bitIndex: Int): Boolean {
        return (buttonMask(bitIndex) and buttonsState) != 0.toUByte()
    }

    fun copyFrom(source: ControllerState) {
        controllerState.copyFrom(source)
    }

    fun getConnected(): Boolean {
        return controllerState.connected
    }

    fun getHome(): Boolean = getButton(controllerState.menuButtons, MenuButtonEnum.Home.index)
    fun getSelect(): Boolean = getButton(controllerState.menuButtons, MenuButtonEnum.Select.index)
    fun getStart(): Boolean = getButton(controllerState.menuButtons, MenuButtonEnum.Start.index)
    fun getShare(): Boolean = getButton(controllerState.menuButtons, MenuButtonEnum.Share.index)

    fun getAccept(): Boolean = getA()
    fun getReject(): Boolean = getB()

    fun getA(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.A.index)

    fun getB(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.B.index)
    fun getX(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.X.index)
    fun getY(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.Y.index)
    fun getL1(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.L1.index)
    fun getR1(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.R1.index)
    fun getL3(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.L3.index)
    fun getR3(): Boolean = getButton(controllerState.mainButtons, MainButtonEnum.R3.index)

    fun getDPad(): DPadEnum {
        return controllerState.dPad
    }

    fun getDPadUp(): Boolean {
        return controllerState.dPad == DPadEnum.Up || controllerState.dPad == DPadEnum.UpRight || controllerState.dPad == DPadEnum.UpLeft
    }

    fun getDPadDown(): Boolean {
        return controllerState.dPad == DPadEnum.Down || controllerState.dPad == DPadEnum.DownRight || controllerState.dPad == DPadEnum.DownLeft
    }

    fun getDPadLeft(): Boolean {
        return controllerState.dPad == DPadEnum.Left || controllerState.dPad == DPadEnum.DownLeft || controllerState.dPad == DPadEnum.UpLeft
    }

    fun getDPadRight(): Boolean {
        return controllerState.dPad == DPadEnum.Right || controllerState.dPad == DPadEnum.DownRight || controllerState.dPad == DPadEnum.UpRight
    }

    fun getJoy1X(): Short {
        return controllerState.joy1X
    }

    fun getJoy1Y(): Short {
        return controllerState.joy1Y
    }

    fun getJoy2X(): Short {
        return controllerState.joy2X
    }

    fun getJoy2Y(): Short {
        return controllerState.joy2Y
    }

    fun getL2(): Int {
        return controllerState.l2
    }

    fun getR2(): Int {
        return controllerState.r2
    }
}