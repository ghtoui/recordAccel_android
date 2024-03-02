package com.moritoui.recordaccel.model

import com.moritoui.recordaccel.usecases.GetSelfUserUseCase
import com.moritoui.recordaccel.usecases.SumlizeAccDataUseCase
import com.moritoui.recordaccel.usecases.UpdateAccDataListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private var updateAccDataJob: Job? = null
private var sumlizeAccDateJob: Job? = null
class SensorCollectSender @Inject constructor(
    private val getSelfUserUseCase: GetSelfUserUseCase,
    private val updateAccDataListUseCase: UpdateAccDataListUseCase,
    private val sumlizeAccDataUseCase: SumlizeAccDataUseCase
) {
    var isCollect = true

    fun updateAndSendAccData() {
        if (updateAccDataJob?.isActive != true) {
            updateAccDataJob = CoroutineScope(Dispatchers.Default).launch {
                updateAccData()
            }
        }

        // 30秒毎に加速度をまとめる
        // 指定数ごとにpushする
        if (sumlizeAccDateJob?.isActive != true) {
            sumlizeAccDateJob = CoroutineScope(Dispatchers.IO).launch {
                val selfUser = getSelfUserUseCase()
                if (selfUser != null) {
                    sumlizeAccData(selfUser)
                }
            }
        }
    }

    private suspend fun updateAccData() {
        while (isCollect) {
            delay(1000)
            updateAccDataListUseCase()
        }
    }

    private suspend fun sumlizeAccData(selfUser: User) {
        while (isCollect) {
            delay(1000 * 30)
            sumlizeAccDataUseCase(selfUser, pushCount = 3)
        }
    }
}
