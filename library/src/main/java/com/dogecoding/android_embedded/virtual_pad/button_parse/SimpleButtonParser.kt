package com.dogecoding.android_embedded.virtual_pad.button_parse

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

    fun reset() {
        previousState = false
    }
}