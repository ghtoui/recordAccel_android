package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.repositories.SensorDataRepository
import javax.inject.Inject

class GetApiAccelDataUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
){
    suspend operator fun invoke(selectDate: String) {
        return sensorDataRepository.getApiAccelDataList(selectDate)
    }
}
