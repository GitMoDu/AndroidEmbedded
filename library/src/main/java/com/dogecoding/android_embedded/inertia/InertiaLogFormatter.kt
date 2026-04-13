package com.dogecoding.android_embedded.inertia

import com.dogecoding.android_embedded.inertia.components.boot_counter.BootCounterLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.LogMetaFormatter
import com.dogecoding.android_embedded.inertia.components.log.format.LogFormatter
import com.dogecoding.android_embedded.inertia.components.uart_interface.UartInterfaceLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.ahrs.AhrsLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.ahrs.reefwing.ReefwingAhrsLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.i2c.I2cInterfaceLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050.Mpu6050LogFormatter
import com.dogecoding.android_embedded.inertia.drivers.serial.SerialInterfaceLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.spi.SpiInterfaceLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.uart.vg6328A.Vg6328aLogFormatter

object InertiaLogFormatter {

    /**
     * Registers all standard Inertia library log formatters.
     */
    fun registerAll() {
        // Log meta.
        LogFormatter.registerFormatter(LogMetaFormatter())
        LogFormatter.registerFormatter(BootCounterLogFormatter())

        // Hardware peripherals.
        LogFormatter.registerFormatter(SerialInterfaceLogFormatter())
        LogFormatter.registerFormatter(I2cInterfaceLogFormatter())
        LogFormatter.registerFormatter(SpiInterfaceLogFormatter())

        // Uart interface
        LogFormatter.registerFormatter(UartInterfaceLogFormatter())

        // Hardware Drivers.
        LogFormatter.registerFormatter(Mpu6050LogFormatter())
        LogFormatter.registerFormatter(Vg6328aLogFormatter())

        // AHRS model and drivers.
        LogFormatter.registerFormatter(AhrsLogFormatter())
        LogFormatter.registerFormatter(ReefwingAhrsLogFormatter())
    }
}
