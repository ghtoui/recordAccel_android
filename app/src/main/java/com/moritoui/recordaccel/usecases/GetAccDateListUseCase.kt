package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.repositories.SensorDataRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetAccDateListUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
) {
    suspend operator fun invoke(pageNumber: Int, dateList: MutableList<String>): MutableList<String> {
        val accDataList = sensorDataRepository.accDataList
        return (sensorDataRepository.getApiAccelDateList(pageNumber) + dateList + extractDateList(accDataList))
            .groupBy { it }.keys.toMutableList()
    }

    // 年月日でグループ化する
    private fun extractDateList(accDataList: List<AccData>): MutableList<String> {
        return accDataList.groupBy {
            it.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.keys.toMutableList()
    }
}
