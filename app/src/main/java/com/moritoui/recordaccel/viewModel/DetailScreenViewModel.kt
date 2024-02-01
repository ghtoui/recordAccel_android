package com.moritoui.recordaccel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.repositories.SensorDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailScreenUiState(
    val accDataList: List<AccData> = emptyList(),
    val minXValue: Float = 0.0F,
    val maxXValue: Float = 0.0F,
    val minYValue: Float = 0.0F,
    val maxYValue: Float = 0.0F,
    val minZValue: Float = 0.0F,
    val maxZValue: Float = 0.0F
)

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val sensorDataRepository: SensorDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailScreenUiState())
    val uiState: StateFlow<DetailScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                sensorDataRepository.updateAccDataList()
                updateSensorUiState(sensorDataRepository.accDataList)
            }
        }
    }

    // 加速度データからx, y ,z の最小値・最大値を計算して、UiStateを更新
    private fun updateSensorUiState(accDataList: List<AccData>) {
        var minXValue = accDataList.minOf { it.accX }
        if (minXValue > _uiState.value.minXValue) {
            minXValue = _uiState.value.minXValue
        }
        var maxXValue = accDataList.maxOf { it.accX }
        if (maxXValue < _uiState.value.maxXValue) {
            maxXValue = _uiState.value.maxXValue
        }
        var minYValue = accDataList.minOf { it.accY }
        if (minYValue > _uiState.value.minYValue) {
            minYValue = _uiState.value.minYValue
        }
        var maxYValue = accDataList.maxOf { it.accY }
        if (maxYValue < _uiState.value.maxYValue) {
            maxYValue = _uiState.value.maxYValue
        }
        var minZValue = accDataList.minOf { it.accZ }
        if (minZValue > _uiState.value.minZValue) {
            minZValue = _uiState.value.minZValue
        }
        var maxZValue = accDataList.maxOf { it.accZ }
        if (maxZValue < _uiState.value.maxZValue) {
            maxZValue = _uiState.value.maxZValue
        }
        _uiState.update {
            it.copy(
                accDataList = accDataList,
                minXValue = minXValue,
                maxXValue = maxXValue,
                minYValue = minYValue,
                maxYValue = maxYValue,
                minZValue = minZValue,
                maxZValue = maxZValue,
            )
        }
    }
}
