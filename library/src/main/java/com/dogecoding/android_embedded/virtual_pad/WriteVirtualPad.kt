package com.dogecoding.android_embedded.virtual_pad

import com.dogecoding.android_embedded.virtual_pad.model.VirtualPadState
import com.dogecoding.android_embedded.virtual_pad.model.DPadEnum
import com.dogecoding.android_embedded.virtual_pad.model.MainButtonEnum
import com.dogecoding.android_embedded.virtual_pad.model.MenuButtonEnum

/**
 * VirtualPad write interface.
 */
class WriteVirtualPad : VirtualPad() {

    private fun setMainButton(buttonValue: Boolean, button: MainButtonEnum) {
        if (buttonValue) {
            virtualPadState.mainButtons = virtualPadState.mainButtons or buttonMask(button.index)
        } else {
            virtualPadState.mainButtons =
                virtualPadState.mainButtons and buttonMask(button.index).inv()
        }
    }

    private fun setMenuButton(buttonValue: Boolean, button: MenuButtonEnum) {
        if (buttonValue) {
            virtualPadState.menuButtons = virtualPadState.menuButtons or buttonMask(button.index)
        } else {
            virtualPadState.menuButtons =
                virtualPadState.menuButtons and buttonMask(button.index).inv()
        }
    }

    fun getState(): VirtualPadState {
        return virtualPadState
    }

    fun setConnected(connected: Boolean) {
        virtualPadState.connected = connected
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
                virtualPadState.dPad = DPadEnum.UpLeft
            } else if (right) {
                virtualPadState.dPad = DPadEnum.UpRight
            } else {
                virtualPadState.dPad = DPadEnum.Up
            }
        } else if (down) {
            if (left) {
                virtualPadState.dPad = DPadEnum.DownLeft
            } else if (right) {
                virtualPadState.dPad = DPadEnum.DownRight
            } else {
                virtualPadState.dPad = DPadEnum.Down
            }
        } else if (left) {
            virtualPadState.dPad = DPadEnum.Left
        } else if (right) {
            virtualPadState.dPad = DPadEnum.Right
        } else {
            virtualPadState.dPad = DPadEnum.None
        }
    }

    fun setDPad(dPad: DPadEnum) {
        virtualPadState.dPad = dPad
    }

    fun setJoy1(x: Short, y: Short) {
        virtualPadState.joy1X = x
        virtualPadState.joy1Y = y
    }

    fun setJoy2(x: Short, y: Short) {
        virtualPadState.joy2X = x
        virtualPadState.joy2Y = y
    }

    fun setL2(progress: Int) {
        virtualPadState.l2 = progress
    }

    fun setR2(progress: Int) {
        virtualPadState.r2 = progress
    }
}
