package com.moritoui.recordaccel.viewModel

import androidx.lifecycle.ViewModel
import com.moritoui.recordaccel.model.MotionSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val motionSensor: MotionSensor
) : ViewModel() {
    fun getAccData(): String {
        return motionSensor.getAccData()
    }
}
