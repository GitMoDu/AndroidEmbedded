package com.dogecoding.embedded.virtual_pad.model


class VirtualPadState {
    var joy1X: Short = 0
    var joy1Y: Short = 0
    var joy2X: Short = 0
    var joy2Y: Short = 0

    var l2: Int = 0
    var r2: Int = 0

    // Single value state.
    var dPad: DPadEnum = DPadEnum.None

    // Up to 8 main buttons.
    var mainButtons: UByte = 0u

    // Up to 8 menu buttons.
    var menuButtons: UByte = 0u

    var connected: Boolean = false

    fun copyFrom(source: VirtualPadState) {
        this.joy1X = source.joy1X
        this.joy1Y = source.joy1Y
        this.joy2X = source.joy2X
        this.joy2Y = source.joy2Y

        this.l2 = source.l2
        this.r2 = source.r2

        this.dPad = source.dPad
        this.mainButtons = source.mainButtons
        this.menuButtons = source.menuButtons
        this.connected = source.connected
    }

    fun clear() {
        this.joy1X = 0
        this.joy1Y = 0
        this.joy2X = 0
        this.joy2Y = 0

        this.l2 = 0
        this.r2 = 0

        this.dPad = DPadEnum.None
        this.mainButtons = 0u
        this.menuButtons = 0u
        this.connected = false
    }
}