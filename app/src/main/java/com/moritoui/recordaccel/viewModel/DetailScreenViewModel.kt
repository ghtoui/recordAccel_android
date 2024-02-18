package com.moritoui.recordaccel.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.TimeTerm
import com.moritoui.recordaccel.repositories.SensorDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
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
    val isLoading: Boolean,
    val selectTimeTerm: TimeTerm = TimeTerm.Day,
    val xStart: Long = 0,
    val xEnd: Long = 0
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
        updateXAxis()
        // 1秒毎に加速度を収集する
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
                    selectedDateTime(_uiState.value.dateList.last())
                }
                updateSensorUiState(accDataList)
                // log確認用
//                val it = sensorDataRepository.accDataList.last()
//                Log.d(
//                    "test",
//                    "AccData(resultAcc = ${it.resultAcc}, date = \"${it.date}\"),"
//                )
            }
        }

        // 1分毎に加速度をまとめる
        viewModelScope.launch {
            while (true) {
                delay(1000 * 30)
                sensorDataRepository.sumlizeAccList()
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

    // 要素が選択された時に呼び出すものをまとめる
    private fun selectReload() {
        updateIsLoading(true)
        updateXAxis()
    }

    private fun updateIsLoading(isLoading: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = isLoading
            )
        }
    }

    // startが負の値にならないように調整
    // endは23:59:59を超えないようjに調整
    private fun calcXAxis(): Pair<Long, Long> {
        val now = LocalDateTime.now()
        val baseDateTime: LocalDateTime = if (_uiState.value.selectedDateTime == null) {
            now
        } else {
            timeManager.textToDate(_uiState.value.selectedDateTime + " ${"${now.hour}".padStart(2, '0')}:${"${now.minute}".padStart(2, '0')}:${"${now.second}".padStart(2, '0')}")
        }
        val xAxisStart = when (_uiState.value.selectTimeTerm) {
            TimeTerm.Day -> baseDateTime.withHour(0).withMinute(0).withSecond(0)
            TimeTerm.HalfDay -> when (baseDateTime.hour) {
                in 0..5 -> baseDateTime.withHour(0)
                else -> baseDateTime.withHour(baseDateTime.hour - 6)
            }
            TimeTerm.ThreeHours -> when (baseDateTime.hour) {
                in 0..2 -> baseDateTime.withHour(0)
                else -> baseDateTime.withHour(baseDateTime.hour - 3)
            }
            TimeTerm.Hour -> baseDateTime.withMinute(0)
        }
        val xAxisEnd = when (_uiState.value.selectTimeTerm) {
            TimeTerm.Day -> baseDateTime.withHour(23).withMinute(59).withSecond(59)
            TimeTerm.HalfDay -> when (baseDateTime.hour) {
                in 18..24 -> baseDateTime.withHour(23).withMinute(59).withSecond(59)
                else -> baseDateTime.withHour(baseDateTime.hour + 6)
            }
            TimeTerm.ThreeHours -> when (baseDateTime.hour) {
                in 21..24 -> baseDateTime.withHour(23).withMinute(59).withSecond(59)
                else -> baseDateTime.withHour(baseDateTime.hour + 3)
            }
            TimeTerm.Hour -> baseDateTime.withMinute(59)
        }

        val start = xAxisStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = xAxisEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return Pair(start, end)
    }

    private fun updateXAxis() {
        val xAxis = calcXAxis()
        _uiState.update {
            it.copy(
                xStart = xAxis.first,
                xEnd = xAxis.second
            )
        }
    }

    // 日付が選択されたら、min, maxのスケールを初期化する
// その日によって違うから
    fun selectedDateTime(selectedDateTime: String) {
        _uiState.update {
            it.copy(
                selectedDateTime = selectedDateTime,
                minValue = 0.0,
                maxValue = 0.0,
                selectTimeTerm = TimeTerm.Day
            )
        }
        selectReload()
    }

    fun selectTimeTerm(selectTimeTerm: TimeTerm) {
        _uiState.update {
            it.copy(
                selectTimeTerm = selectTimeTerm
            )
        }
        selectReload()
    }
}
