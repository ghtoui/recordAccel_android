package com.moritoui.recordaccel.model

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.pow

class MotionSensor @Inject constructor(
    sensorManager: SensorManager,
    private val timeManager: TimeManager
) : SensorEventListener {
    private var accSensor: Sensor? = null
    private var accDataList: MutableList<AccData> = mutableListOf()
    private var accData: String = ""
    private val EXPONENT = 2.0
    private val ROOT = 0.5

    init {
//        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 三軸加速度の合成
        val resultAcc = (x.toDouble().pow(EXPONENT) + y.toDouble().pow(EXPONENT) + z.toDouble().pow(EXPONENT)).pow(ROOT)
        accDataList.add(
            AccData(
                resultAcc = resultAcc,
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

    fun clearAccDataList() {
        accDataList = mutableListOf()
    }
}
