package com.moritoui.recordaccel.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.MotionSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val motionSensor: MotionSensor
) : ViewModel() {

    init {
        viewModelScope.launch {
            while (true) {
                checkAccDataList()
            }
        }
    }

    fun getAccData(): String {
        return motionSensor.getAccData()
    }

    private suspend fun checkAccDataList() {
        delay(1000)
        val accList = motionSensor.getAccDataList()
        val it = accList.last()
        Log.d("test",
            "AccData(" +
                    "accX = ${it.accX}.toFloat()," +
                    "accY = ${it.accY}.toFloat()," +
                    "accZ = ${it.accZ}.toFloat()," +
                    "date = \"${it.date}\"" +
                    "),"
        )
    }
}
