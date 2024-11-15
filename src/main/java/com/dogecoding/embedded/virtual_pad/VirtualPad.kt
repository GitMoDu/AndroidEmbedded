package com.dogecoding.embedded.virtual_pad

import com.dogecoding.embedded.virtual_pad.model.VirtualPadState
import com.dogecoding.embedded.virtual_pad.model.DPadEnum
import com.dogecoding.embedded.virtual_pad.model.MainButtonEnum
import com.dogecoding.embedded.virtual_pad.model.MenuButtonEnum

/**
 * VirtualPad (read-only) interface.
 * Use of button masks is slower than boolean fields, but uses way less RAM and speeds up state copies significantly.
 * Modelled after RetroArch's RetroPad.
 *
 * Menu Buttons:
 *
 * [Home]        [Share]
 * [Select]    [Start]
 *
 *
 * DPad:
 *
 * [↖]	[↑] [↗]
 * [←]	 · 	[→]
 * [↙]	[↓]	[↘]
 *
 *
 * Face Buttons:
 *
 *      [Y]
 * [X]      [B]
 *      [A]
 *
 *
 * Joysticks 1 and 2, with respective L3 and R3:
 *
 * [↖]	[↑]	[↗]		[↖]	[↑]	[↗]
 * [←]	L3	[→]		[←]	 R3	[→]
 * [↙]	[↓]	[↘]		[↙]	[↓]	[↘]
 *
 *
 * Digital and Analog Triggers:
 *
 * [L1]        [R1]
 * [L2]        [R2]
 *
 */
open class VirtualPad {
    protected val virtualPadState = VirtualPadState()

    protected fun buttonMask(bitIndex: Int): UByte {
        return (1 shl bitIndex).toUByte()
    }

    private fun getButton(buttonsState: UByte, bitIndex: Int): Boolean {
        return (buttonMask(bitIndex) and buttonsState) != 0.toUByte()
    }

    fun clear() {
        virtualPadState.clear()
    }

    fun copyFrom(source: VirtualPadState) {
        virtualPadState.copyFrom(source)
    }

    fun getConnected(): Boolean {
        return virtualPadState.connected
    }

    fun getHome(): Boolean = getButton(virtualPadState.menuButtons, MenuButtonEnum.Home.index)
    fun getSelect(): Boolean = getButton(virtualPadState.menuButtons, MenuButtonEnum.Select.index)
    fun getStart(): Boolean = getButton(virtualPadState.menuButtons, MenuButtonEnum.Start.index)
    fun getShare(): Boolean = getButton(virtualPadState.menuButtons, MenuButtonEnum.Share.index)

    fun getAccept(): Boolean = getA()
    fun getReject(): Boolean = getB()

    fun getA(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.A.index)

    fun getB(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.B.index)
    fun getX(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.X.index)
    fun getY(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.Y.index)
    fun getL1(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.L1.index)
    fun getR1(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.R1.index)
    fun getL3(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.L3.index)
    fun getR3(): Boolean = getButton(virtualPadState.mainButtons, MainButtonEnum.R3.index)

    fun getDPad(): DPadEnum {
        return virtualPadState.dPad
    }

    fun getDPadUp(): Boolean {
        return virtualPadState.dPad == DPadEnum.Up || virtualPadState.dPad == DPadEnum.UpRight || virtualPadState.dPad == DPadEnum.UpLeft
    }

    fun getDPadDown(): Boolean {
        return virtualPadState.dPad == DPadEnum.Down || virtualPadState.dPad == DPadEnum.DownRight || virtualPadState.dPad == DPadEnum.DownLeft
    }

    fun getDPadLeft(): Boolean {
        return virtualPadState.dPad == DPadEnum.Left || virtualPadState.dPad == DPadEnum.DownLeft || virtualPadState.dPad == DPadEnum.UpLeft
    }

    fun getDPadRight(): Boolean {
        return virtualPadState.dPad == DPadEnum.Right || virtualPadState.dPad == DPadEnum.DownRight || virtualPadState.dPad == DPadEnum.UpRight
    }

    fun getJoy1X(): Short {
        return virtualPadState.joy1X
    }

    fun getJoy1Y(): Short {
        return virtualPadState.joy1Y
    }

    fun getJoy2X(): Short {
        return virtualPadState.joy2X
    }

    fun getJoy2Y(): Short {
        return virtualPadState.joy2Y
    }

    fun getL2(): Int {
        return virtualPadState.l2
    }

    fun getR2(): Int {
        return virtualPadState.r2
    }
}