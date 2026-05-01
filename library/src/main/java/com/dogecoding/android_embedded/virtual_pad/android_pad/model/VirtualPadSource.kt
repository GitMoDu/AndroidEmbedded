package com.dogecoding.android_embedded.virtual_pad.android_pad.model

import com.dogecoding.android_embedded.virtual_pad.VirtualPad

interface VirtualPadSource {
    fun getVirtualPadState(virtualPad: VirtualPad)
}
