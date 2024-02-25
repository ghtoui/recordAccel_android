package com.moritoui.recordaccel.repositories

import android.util.Log
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.MotionSensor
import com.moritoui.recordaccel.model.PostAccData
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.network.AccelApiService
import com.moritoui.recordaccel.usecases.GetSelectedUserUseCase
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SensorDataRepository {
    var accDataList: MutableList<AccData>
    var apiAccDataList: MutableList<AccData>
    var sumlizeCount: Int
    var selectedUser: User?
    fun updateAccDataList()
    fun sumlizeAccList(): MutableList<AccData>
    fun updateSelectedUser()
    suspend fun getApiAccelDateList(pageNumber: Int): MutableList<String>
    suspend fun getApiAccelDataList(selectDate: String)
    suspend fun postAccelDataList()
}
class SensorDataRepositoryImpl @Inject constructor(
    private val motionSensor: MotionSensor,
    private val accelApi: AccelApiService,
    private val timeManager: TimeManager,
    private val getSelectedUserUseCase: GetSelectedUserUseCase
) : SensorDataRepository {
    private val tempAccDataList: MutableList<AccData> = mutableListOf()
    override var accDataList: MutableList<AccData> = mutableListOf()
    override var apiAccDataList: MutableList<AccData> = mutableListOf()
    override var sumlizeCount = 0
    override var selectedUser: User? = getSelectedUserUseCase()

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

    override fun updateSelectedUser() {
        selectedUser = getSelectedUserUseCase()
    }

    override suspend fun getApiAccelDateList(pageNumber: Int): MutableList<String> {
        var dateList: MutableList<String> = mutableListOf()
        withContext(Dispatchers.IO) {
            try {
                dateList = accelApi.getAccDateList(userId = selectedUser!!.userId, pageNumber = pageNumber).body()!!.toMutableList()
            } catch (error: IOException) {
                Log.e("error", "fetch dateList error: $error")
            }
        }
        return dateList
    }

    override suspend fun getApiAccelDataList(selectDate: String) {
        withContext(Dispatchers.IO) {
            try {
                val accJsonData = accelApi.getAccDataList(selectedUser!!.userId, selectDate).body()
                if (!accJsonData.isNullOrEmpty()) {
                    apiAccDataList = accJsonData.first().accDatas.map {
                        AccData(
                            resultAcc = it.accData,
                            date = LocalDateTime.parse(it.date.dropLast(1))
                        )
                    }.toMutableList()
                } else { }
            } catch (error: IOException) {
                Log.e("error", "$error")
            } catch (error: NullPointerException) {
                Log.e("error", "user is null")
            }
        }
    }

    override suspend fun postAccelDataList() {
        withContext(Dispatchers.IO) {
            try {
                val postAccData: List<PostAccData> = tempAccDataList.map {
                    PostAccData(
                        userId = selectedUser!!.userId,
                        accData = it.resultAcc,
                        date = timeManager.dateToISOText(it.date)
                    )
                }.takeLast(sumlizeCount)
                clearSumlizeCount()
                accelApi.postAccData(body = postAccData)
            } catch (error: IOException) {
                Log.e("error", "fetch post error: $error")
            } catch (error: NullPointerException) {
                Log.e("error", "user is null")
            }
        }
    }

    private fun clearSumlizeCount() {
        this.sumlizeCount = 0
    }
}
