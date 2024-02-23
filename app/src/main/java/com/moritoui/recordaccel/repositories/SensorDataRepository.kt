package com.moritoui.recordaccel.repositories

import android.util.Log
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.MotionSensor
import com.moritoui.recordaccel.model.PostAccData
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.network.AccelApiService
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SensorDataRepository {
    var accDataList: MutableList<AccData>
    var apiAccDataList: MutableList<AccData>
    var sumlizeCount: Int
    fun updateAccDataList()
    fun sumlizeAccList(): MutableList<AccData>
    suspend fun getApiAccelDateList(): MutableList<String>
    suspend fun getApiAccelDataList(selectDate: String)
    suspend fun postAccelDataList()
}
class SensorDataRepositoryImpl @Inject constructor(
    private val motionSensor: MotionSensor,
    private val accelApi: AccelApiService,
    private val timeManager: TimeManager
) : SensorDataRepository {
    private val tempAccDataList: MutableList<AccData> = mutableListOf()
    override var accDataList: MutableList<AccData> = mutableListOf()
    override var apiAccDataList: MutableList<AccData> = mutableListOf()
    override var sumlizeCount = 0

    init {
        updateAccDataList()
    }

    override fun updateAccDataList() {
        val accData = tempAccDataList + motionSensor.getAccDataList()
        accDataList = accData.toMutableList()
    }

    // 無制限に取得し続けてもデータが多すぎたり、グラフ上で見づらいためまとめる
    // 全体の平均をとって、日付は一番初めのもの
    override fun sumlizeAccList(): MutableList<AccData> {
        val accDataList = accDataList.drop(tempAccDataList.size)
        tempAccDataList.add(
            AccData(
                resultAcc = accDataList.sumOf { it.resultAcc } / accDataList.size,
                date = accDataList.first().date
            )
        )
        motionSensor.clearAccDataList()
        this.sumlizeCount += 1
        return tempAccDataList
    }

    private fun clearSumlizeCount() {
        this.sumlizeCount = 0
    }

    override suspend fun getApiAccelDateList(): MutableList<String> {
        var dateList: MutableList<String> = mutableListOf()
        withContext(Dispatchers.IO) {
            try {
                dateList = accelApi.getAccDateList(userId = "123", pageNumber = 0).body()!!.toMutableList()
            } catch (error: IOException) {
                Log.d("error", "fetch dateList error: $error")
            }
        }
        return dateList
    }

    override suspend fun getApiAccelDataList(selectDate: String) {
        withContext(Dispatchers.IO) {
            try {
                val accJsonData = accelApi.getAccDataList("123", selectDate).body()
                if (!accJsonData.isNullOrEmpty()) {
                    apiAccDataList = accJsonData.first().accDatas.map {
                        AccData(
                            resultAcc = it.accData,
                            date = LocalDateTime.parse(it.date.dropLast(1))
                        )
                    }.toMutableList()
                } else { }
            } catch (error: IOException) {
                Log.d("error", "$error")
            }
        }
    }

    override suspend fun postAccelDataList() {
        withContext(Dispatchers.IO) {
            val postAccData: List<PostAccData> = tempAccDataList.map {
                PostAccData(
                    userId = "123",
                    accData = it.resultAcc,
                    date = timeManager.dateToISOText(it.date)
                )
            }.takeLast(sumlizeCount)
            try {
                clearSumlizeCount()
                accelApi.postAccData(body = postAccData)
            } catch (error: IOException) {
                Log.d("error", "fetch post error: $error")
            }
        }
    }
}
