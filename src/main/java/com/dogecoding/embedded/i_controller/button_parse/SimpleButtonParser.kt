package com.dogecoding.embedded.i_controller.button_parse

class SimpleButtonParser {
    private var previousState: Boolean = false

    // Returns true only when state switches to pressed.
    fun parse(state: Boolean): Boolean {
        if (state != previousState) {
            previousState = state

            return state
        } else {
            return false
        }
    }
}