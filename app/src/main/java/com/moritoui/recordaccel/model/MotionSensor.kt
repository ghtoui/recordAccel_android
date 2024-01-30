package com.moritoui.recordaccel.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.inject.Inject

class MotionSensor @Inject constructor(
    sensorManager: SensorManager
) : SensorEventListener {
    private var accSensor: Sensor? = null
    private var accDataList: MutableList<Float> = mutableListOf()
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
        accDataList.add(x)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    fun getAccData(): String {
        return accData
    }

    fun getAccDataList(): MutableList<Float> {
        return accDataList
    }
}
