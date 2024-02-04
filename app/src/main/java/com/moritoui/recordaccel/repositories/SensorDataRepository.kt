package com.moritoui.recordaccel.repositories

import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.MotionSensor
import javax.inject.Inject

interface SensorDataRepository {
    var accDataList: List<AccData>
    fun updateAccDataList()
}
class SensorDataRepositoryImpl @Inject constructor(
    private val motionSensor: MotionSensor
) : SensorDataRepository {
    override var accDataList: List<AccData> = emptyList()

    init {
        updateAccDataList()
    }

    override fun updateAccDataList() {
        val accData = motionSensor.getAccDataList()
        val splitNum = 0
        accDataList = if (accData.size > splitNum && splitNum != 0) {
            accData.takeLast(splitNum)
        } else {
            accData
        }
    }
}
