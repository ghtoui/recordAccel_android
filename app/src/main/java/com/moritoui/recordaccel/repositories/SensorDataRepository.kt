package com.moritoui.recordaccel.repositories

import android.util.Log
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.MotionSensor
import com.moritoui.recordaccel.model.PostAccData
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.network.AccelApiService
import java.time.LocalDateTime
import javax.inject.Inject

interface SensorDataRepository {
    var accDataList: MutableList<AccData>
    var apiAccDataList: MutableList<AccData>
    var sumlizeCount: Int
    var selectedUser: User?
    fun updateAccDataList()
    fun sumlizeAccList(): MutableList<AccData>
    suspend fun getApiAccelDateList(pageNumber: Int): MutableList<String>
    suspend fun getApiAccelDataList(selectDate: String)
    suspend fun postAccelDataList(selfUser: User)
}
class SensorDataRepositoryImpl @Inject constructor(
    private val motionSensor: MotionSensor,
    private val accelApi: AccelApiService,
    private val timeManager: TimeManager,
) : SensorDataRepository {
    private val tempAccDataList: MutableList<AccData> = mutableListOf()
    override var accDataList: MutableList<AccData> = mutableListOf()
    override var apiAccDataList: MutableList<AccData> = mutableListOf()
    override var sumlizeCount = 0
    override var selectedUser: User? = null

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

    override suspend fun getApiAccelDateList(pageNumber: Int): MutableList<String> {
        var dateList: MutableList<String> = mutableListOf()
        try {
            dateList = accelApi.getAccDateList(userId = selectedUser!!.userId, pageNumber = pageNumber).body()!!.toMutableList()
        } catch (error: Exception) {
            Log.e("error", "fetch dateList error: $error")
        }
        return dateList
    }

    override suspend fun getApiAccelDataList(selectDate: String) {
        try {
            val accJsonData = accelApi.getAccDataList(selectedUser!!.userId, selectDate).body()
            if (!accJsonData.isNullOrEmpty()) {
                apiAccDataList = accJsonData.first().accDatas.map {
                    AccData(
                        resultAcc = it.accData,
                        date = LocalDateTime.parse(it.date.dropLast(1))
                    )
                }.toMutableList()
            }
        } catch (error: Exception) {
            Log.e("error", "$error")
        } catch (error: NullPointerException) {
            Log.e("error", "user is null")
        }
    }

    override suspend fun postAccelDataList(selfUser: User) {
        try {
            val postAccData: List<PostAccData> = tempAccDataList.map {
                PostAccData(
                    userId = selfUser.userId,
                    accData = it.resultAcc,
                    date = timeManager.dateToISOText(it.date)
                )
            }.takeLast(sumlizeCount)
            clearSumlizeCount()
            accelApi.postAccData(body = postAccData)
        } catch (error: Exception) {
            Log.e("error", "fetch post error: $error")
        } catch (error: NullPointerException) {
            Log.e("error", "user is null")
        }
    }

    private fun clearSumlizeCount() {
        this.sumlizeCount = 0
    }
}
