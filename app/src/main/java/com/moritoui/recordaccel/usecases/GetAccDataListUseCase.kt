package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.repositories.SensorDataRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetAccDataListUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
) {
    operator fun invoke(selectedDate: String?): MutableList<AccData> {
        val accDataList = sensorDataRepository.accDataList
        val date = accDataList.groupBy { it.date.format(DateTimeFormatter.ISO_LOCAL_DATE) }.keys.first()
        val data = if (selectedDate == null || selectedDate == date) {
            (sensorDataRepository.apiAccDataList + accDataList).toMutableList()
        } else {
            sensorDataRepository.apiAccDataList
        }
        return data
    }
}
