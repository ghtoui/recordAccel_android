package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.repositories.SensorDataRepository
import javax.inject.Inject

class UpdateAccDataListUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository,
) {
    operator fun invoke() {
        sensorDataRepository.updateAccDataList()
    }
}
