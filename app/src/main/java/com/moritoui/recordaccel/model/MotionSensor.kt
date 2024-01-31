package com.moritoui.recordaccel.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.time.LocalDateTime
import javax.inject.Inject

class MotionSensor @Inject constructor(
    sensorManager: SensorManager,
    private val timeManager: TimeManager
) : SensorEventListener {
    private var accSensor: Sensor? = null
    private var accDataList: MutableList<AccData> = mutableListOf()
    private var accData: String = ""

    init {
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        accData = "x: $x\ny: $y\nz: $z"
        accDataList.add(
            AccData(
                accX = x,
                accY = y,
                accZ = z,
                date = timeManager.dateToText(LocalDateTime.now())
            )
        )
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    fun getAccData(): String {
        return accData
    }

    fun getAccDataList(): MutableList<AccData> {
        return accDataList
    }
}
