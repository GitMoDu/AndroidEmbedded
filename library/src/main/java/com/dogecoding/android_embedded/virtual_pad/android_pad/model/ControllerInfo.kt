package com.dogecoding.android_embedded.virtual_pad.android_pad.model

data class ControllerInfo(
    val name: String,
    val vendorId: Int,
    val productId: Int,
    val descriptor: String
)