package com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050.log

object Model {
    const val LOG_TAG = 867172820L

    enum class LogCodeEnum {
        ErrorBoot,
        ErrorReadMotion,
        ErrorReadTemperature,
        RecoveryAttempt
    }
}
