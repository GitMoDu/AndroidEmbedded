package com.dogecoding.android_embedded.inertia.components.log

import com.dogecoding.android_embedded.inertia.components.ahrs.log.AhrsLogFormatter
import com.dogecoding.android_embedded.inertia.components.ahrs.reefwing.log.ReefwingAhrsLogFormatter
import com.dogecoding.android_embedded.inertia.components.ahrs.xio.log.XioAhrsLogFormatter
import com.dogecoding.android_embedded.inertia.components.boot_counter.log.BootCounterLogFormatter
import com.dogecoding.android_embedded.inertia.components.core.lifecycle.log.LifecycleLogFormatter
import com.dogecoding.android_embedded.inertia.components.light.log.LightLogFormatter
import com.dogecoding.android_embedded.inertia.components.link.log.LinkLogFormatter
import com.dogecoding.android_embedded.inertia.components.log.format.LogFormatter
import com.dogecoding.android_embedded.inertia.components.log.log.LogMetaFormatter
import com.dogecoding.android_embedded.inertia.components.power_train.log.PowerTrainLogFormatter
import com.dogecoding.android_embedded.inertia.components.power_train.pwm.log.PwmLogFormatter
import com.dogecoding.android_embedded.inertia.components.power_train.pwm_actuator.log.PwmActuatorLogFormatter
import com.dogecoding.android_embedded.inertia.components.power_train.servo.log.ServoLogFormatter
import com.dogecoding.android_embedded.inertia.components.power_train.servo_actuator.log.ServoActuatorLogFormatter
import com.dogecoding.android_embedded.inertia.components.storage.allocation.log.StorageAllocationLogFormatter
import com.dogecoding.android_embedded.inertia.components.storage.fram.log.FramLogFormatter
import com.dogecoding.android_embedded.inertia.components.storage.little_fs.log.LittleFsLogFormatter
import com.dogecoding.android_embedded.inertia.components.task_profiler.log.TaskProfilerLogFormatter
import com.dogecoding.android_embedded.inertia.components.uart_interface.log.UartInterfaceLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.hardware_interface.i2c.log.I2cLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.hardware_interface.serial.log.SerialLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.hardware_interface.spi.log.SpiLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.imu.mpu6050.log.Mpu6050LogFormatter
import com.dogecoding.android_embedded.inertia.drivers.optical_flow.mtf0x.log.Mtf0xLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.storage.fram.mb85rc.log.Mb85rcFramLogFormatter
import com.dogecoding.android_embedded.inertia.drivers.uart.vg6328A.log.Vg6328aLogFormatter

object InertiaLogFormatter {

    /**
     * Registers all standard Inertia library log formatters.
     */
    fun registerAll() {
        // --- Components ---

        // Log meta.
        LogFormatter.registerFormatter(LogMetaFormatter())
        LogFormatter.registerFormatter(BootCounterLogFormatter())

        // Core
        LogFormatter.registerFormatter(LifecycleLogFormatter())

        // Link
        LogFormatter.registerFormatter(LinkLogFormatter())
        LogFormatter.registerFormatter(UartInterfaceLogFormatter())

        // PowerTrain
        LogFormatter.registerFormatter(PowerTrainLogFormatter())
        LogFormatter.registerFormatter(PwmLogFormatter())
        LogFormatter.registerFormatter(PwmActuatorLogFormatter())
        LogFormatter.registerFormatter(ServoLogFormatter())
        LogFormatter.registerFormatter(ServoActuatorLogFormatter())

        // Ahrs
        LogFormatter.registerFormatter(AhrsLogFormatter())
        LogFormatter.registerFormatter(ReefwingAhrsLogFormatter())
        LogFormatter.registerFormatter(XioAhrsLogFormatter())

        // Light
        LogFormatter.registerFormatter(LightLogFormatter())

        // Storage
        LogFormatter.registerFormatter(StorageAllocationLogFormatter())
        LogFormatter.registerFormatter(FramLogFormatter())
        LogFormatter.registerFormatter(LittleFsLogFormatter())

        // TaskProfiler
        LogFormatter.registerFormatter(TaskProfilerLogFormatter())

        // --- Drivers ---

        // Hardware peripherals.
        LogFormatter.registerFormatter(SerialLogFormatter())
        LogFormatter.registerFormatter(I2cLogFormatter())
        LogFormatter.registerFormatter(SpiLogFormatter())

        // Sensors
        LogFormatter.registerFormatter(Mpu6050LogFormatter())
        LogFormatter.registerFormatter(Mtf0xLogFormatter())

        // Uart
        LogFormatter.registerFormatter(Vg6328aLogFormatter())

        // Storage
        LogFormatter.registerFormatter(Mb85rcFramLogFormatter())
    }
}
