package com.moritoui.recordaccel.repositories

import android.util.Log
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.MotionSensor
import javax.inject.Inject

interface SensorDataRepository {
    var accDataList: List<AccData>
    fun updateAccDataList()
    fun sumlizeAccList()
}
class SensorDataRepositoryImpl @Inject constructor(
    private val motionSensor: MotionSensor
) : SensorDataRepository {
    private val tempAccDataList: MutableList<AccData> = mutableListOf()
    override var accDataList: List<AccData> = emptyList()

    init {
        updateAccDataList()
    }

    override fun updateAccDataList() {
        val accData = tempAccDataList + motionSensor.getAccDataList()
        accDataList = accData
    }

    // 無制限に取得し続けてもデータが多すぎたり、グラフ上で見づらいためまとめる
    // 全体の平均をとって、日付は一番初めのもの
    override fun sumlizeAccList() {
        val accDataList = accDataList.drop(tempAccDataList.size)
        tempAccDataList.add(
            AccData(
                resultAcc = accDataList.sumOf { it.resultAcc } / accDataList.size,
                date = accDataList.first().date
            )
        )
        Log.d("test", tempAccDataList.toString())
        motionSensor.clearAccDataList()
    }
}
