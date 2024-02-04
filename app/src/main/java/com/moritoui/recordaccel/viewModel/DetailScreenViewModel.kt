package com.moritoui.recordaccel.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.repositories.SensorDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailScreenUiState(
    val accDataList: MutableList<AccData> = mutableListOf(),
    val minXValue: Float = 0.0F,
    val maxXValue: Float = 0.0F,
    val minYValue: Float = 0.0F,
    val maxYValue: Float = 0.0F,
    val minZValue: Float = 0.0F,
    val maxZValue: Float = 0.0F,
    val selectedDateTime: String? = null,
    val dateList: MutableList<String> = mutableListOf(),
    val isLoading: Boolean
)

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val sensorDataRepository: SensorDataRepository,
    private val timeManager: TimeManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailScreenUiState(isLoading = true))
    val uiState: StateFlow<DetailScreenUiState> = _uiState.asStateFlow()
    private var isLoadedDateList = false

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateIsLoading(false)
                sensorDataRepository.updateAccDataList()
//                val accDataList = when (_uiState.value.selectedDateTime) {
//                    null -> sensorDataRepository.accDataList
//                    else -> AccDataList.getAccDataList()
//                }
                val accDataList = AccDataList.getAccDataList() + sensorDataRepository.accDataList
                if (!isLoadedDateList) {
                    isLoadedDateList = true
                    // 保存されているものだけ渡す
                    updateDateList(accDataList)
                }
                updateSensorUiState(accDataList)
                Log.d("size", accDataList.size.toString())
                // log確認用
//                val it = sensorDataRepository.accDataList.last()
//                Log.d(
//                    "test",
//                    "AccData(accX = ${it.accX}.toFloat(), accY = ${it.accY}.toFloat(), accZ = ${it.accZ}.toFloat(), date = \"${it.date}\"),"
//                )
            }
        }
    }

    // 加速度データからx, y ,z の最小値・最大値を計算して、UiStateを更新
    private fun updateSensorUiState(accDataList: List<AccData>) {
        val accDataList = accDataDateFilter(accDataList).toMutableList()
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

    // 保存されている加速度の年月日を取得
    private fun updateDateList(accDataList: List<AccData>) {
        _uiState.update {
            it.copy(
                dateList = extractDateList(accDataList)
            )
        }
    }

    // 年月日でグループ化する
    private fun extractDateList(accDataList: List<AccData>): MutableList<String> {
        return accDataList.groupBy {
            timeManager.textToDate(it.date).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.keys.toMutableList()
    }

    // 加速度データから選択されている年月日だけのものを抜き出す
    private fun accDataDateFilter(accDataList: List<AccData>): List<AccData> {
        return when (_uiState.value.selectedDateTime) {
            null -> accDataList
            else -> accDataList.filter {
                timeManager.textToDate(it.date)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE) == _uiState.value.selectedDateTime
            }
        }
    }

    private fun updateIsLoading(isLoading: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }

    fun selectedDateTime(selectedDateTime: String) {
        _uiState.update {
            it.copy(
                selectedDateTime = when (selectedDateTime) {
                    it.selectedDateTime -> null
                    else -> selectedDateTime
                }
            )
        }
        updateIsLoading(true)
    }
}
