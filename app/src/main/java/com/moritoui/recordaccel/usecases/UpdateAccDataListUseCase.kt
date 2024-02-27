package com.moritoui.recordaccel.usecases

import android.util.Log
import com.moritoui.recordaccel.repositories.SensorDataRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateAccDataListUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
) {
    operator fun invoke() {
        Log.d(this.javaClass.name, "${LocalDateTime.now()}")
        sensorDataRepository.updateAccDataList()
    }
}
