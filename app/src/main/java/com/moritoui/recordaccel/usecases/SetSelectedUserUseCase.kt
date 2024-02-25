package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.repositories.SensorDataRepository
import javax.inject.Inject

class SetSelectedUserUseCase @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
) {
    operator fun invoke(selectUser: User) {
        sensorDataRepository.selectedUser = selectUser
    }
}
