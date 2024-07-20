package com.dogecoding.embedded.i_controller

import com.dogecoding.embedded.i_controller.model.ControllerState
import com.dogecoding.embedded.i_controller.model.DPadEnum
import com.dogecoding.embedded.i_controller.model.MainButtonEnum
import com.dogecoding.embedded.i_controller.model.MenuButtonEnum

class WriteIController : IController() {

    private fun setMainButton(buttonValue: Boolean, button: MainButtonEnum) {
        if (buttonValue) {
            controllerState.mainButtons = controllerState.mainButtons or buttonMask(button.index)
        } else {
            controllerState.mainButtons =
                controllerState.mainButtons and buttonMask(button.index).inv()
        }
    }

    private fun setMenuButton(buttonValue: Boolean, button: MenuButtonEnum) {
        if (buttonValue) {
            controllerState.menuButtons = controllerState.menuButtons or buttonMask(button.index)
        } else {
            controllerState.menuButtons =
                controllerState.menuButtons and buttonMask(button.index).inv()
        }
    }

    fun getState(): ControllerState {
        return controllerState
    }

    fun setConnected(connected: Boolean) {
        controllerState.connected = connected
    }

    fun setHome(buttonValue: Boolean) = setMenuButton(buttonValue, MenuButtonEnum.Home)
    fun setSelect(buttonValue: Boolean) = setMenuButton(buttonValue, MenuButtonEnum.Select)
    fun setStart(buttonValue: Boolean) = setMenuButton(buttonValue, MenuButtonEnum.Start)
    fun setShare(buttonValue: Boolean) = setMenuButton(buttonValue, MenuButtonEnum.Share)

    fun setA(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.A)
    fun setB(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.B)
    fun setX(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.X)
    fun setY(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.Y)
    fun setL1(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.L1)
    fun setR1(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.R1)
    fun setL3(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.L3)
    fun setR3(buttonValue: Boolean) = setMainButton(buttonValue, MainButtonEnum.R3)

    fun setDPad(up: Boolean, down: Boolean, left: Boolean, right: Boolean) {
        if (up) {
            if (left) {
                controllerState.dPad = DPadEnum.UpLeft
            } else if (right) {
                controllerState.dPad = DPadEnum.UpRight
            } else {
                controllerState.dPad = DPadEnum.Up
            }
        } else if (down) {
            if (left) {
                controllerState.dPad = DPadEnum.DownLeft
            } else if (right) {
                controllerState.dPad = DPadEnum.DownRight
            } else {
                controllerState.dPad = DPadEnum.Down
            }
        } else if (left) {
            controllerState.dPad = DPadEnum.Left
        } else if (right) {
            controllerState.dPad = DPadEnum.Right
        } else {
            controllerState.dPad = DPadEnum.None
        }
    }

    fun setDPad(dPad: DPadEnum) {
        controllerState.dPad = dPad
    }

    fun setJoy1(x: Short, y: Short) {
        controllerState.joy1X = x
        controllerState.joy1Y = y
    }

    fun setJoy2(x: Short, y: Short) {
        controllerState.joy2X = x
        controllerState.joy2Y = y
    }

    fun setL2(progress: Int) {
        controllerState.l2 = progress
    }

    fun setR2(progress: Int) {
        controllerState.r2 = progress
    }
}