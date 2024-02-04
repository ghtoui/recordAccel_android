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
    val minValue: Double = 0.0,
    val maxValue: Double = 0.0,
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
                val accDataList = AccDataList.getAccDataList() + sensorDataRepository.accDataList
                if (!isLoadedDateList) {
                    isLoadedDateList = true
                    // 保存されているものだけ渡す
                    updateDateList(accDataList)
                }
                updateSensorUiState(accDataList)
                // log確認用
                val it = sensorDataRepository.accDataList.last()
                Log.d(
                    "test",
                    "AccData(resultAcc = ${it.resultAcc}, date = \"${it.date}\"),"
                )
            }
        }
    }

    // 加速度データからx, y ,z の最小値・最大値を計算して、UiStateを更新
    private fun updateSensorUiState(accDataList: List<AccData>) {
        val accDataList = accDataDateFilter(accDataList).toMutableList()
        var minValue = accDataList.minOf { it.resultAcc }
        if (minValue > _uiState.value.minValue) {
            minValue = _uiState.value.minValue
        }
        var maxValue = accDataList.maxOf { it.resultAcc }
        if (maxValue < _uiState.value.maxValue) {
            maxValue = _uiState.value.maxValue
        }
        _uiState.update {
            it.copy(
                accDataList = accDataList,
                minValue = minValue,
                maxValue = maxValue,
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
