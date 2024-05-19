package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.repositories.SensorDataRepository
import javax.inject.Inject

class SumlizeAccDataUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository,
) {
    suspend operator fun invoke(selfUser: User, pushCount: Int) {
        sensorDataRepository.sumlizeAccList()
        if (sensorDataRepository.sumlizeCount >= pushCount) {
            sensorDataRepository.postAccelDataList(selfUser)
        }
    }
}
