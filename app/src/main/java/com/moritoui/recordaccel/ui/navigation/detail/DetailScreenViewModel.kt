package com.moritoui.recordaccel.ui.navigation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.TimeTerm
import com.moritoui.recordaccel.usecases.GetAccDataListUseCase
import com.moritoui.recordaccel.usecases.GetAccDateListUseCase
import com.moritoui.recordaccel.usecases.GetApiAccelDataUseCase
import com.moritoui.recordaccel.usecases.GetSelectedUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailScreenUiState(
    val accDataList: List<AccData>,
    val minValue: Double,
    val maxValue: Double,
    val selectedDateTime: String?,
    val dateList: List<String>,
    val isLoading: Boolean,
    val selectTimeTerm: TimeTerm,
    val xStart: Long,
    val xEnd: Long,
    val selectData: AccData?,
) {
    companion object {
        fun initialState() = DetailScreenUiState(
            accDataList = emptyList(),
            minValue = 0.0,
            maxValue = 0.0,
            selectedDateTime = null,
            dateList = emptyList(),
            isLoading = false,
            selectTimeTerm= TimeTerm.Day,
            xStart = 0,
            xEnd = 0,
            selectData = null
        )
    }
}

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val timeManager: TimeManager,
    private val getAccDataListUseCase: GetAccDataListUseCase,
    private val getDateListUseCase: GetAccDateListUseCase,
    private val getApiAccelDataUseCase: GetApiAccelDataUseCase,
    getSelectedUserUseCase: GetSelectedUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailScreenUiState.initialState())
    val uiState: StateFlow<DetailScreenUiState> = _uiState.asStateFlow()
    private var isLoadedDateList = false
    private var selectedUser = getSelectedUserUseCase()
    private var accDataList: MutableList<AccData> = getAccDataListUseCase(userKind = selectedUser?.userKind, selectedDate = _uiState.value.selectedDateTime)

    init {
        updateXAxis()
        // 1秒毎に加速度を収集する
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateIsLoading(false)
                accDataList = getAccDataListUseCase(userKind = selectedUser?.userKind, selectedDate = _uiState.value.selectedDateTime)
                if (!isLoadedDateList) {
                    isLoadedDateList = true
                    // 保存されているものだけ渡す
                    val getAsyncDateList = async {
                        getDateListUseCase(
                            pageNumber = 0,
                            dateList = _uiState.value.dateList,
                        )
                    }
                    val accDateList = getAsyncDateList.await()
                    if (accDateList.isNotEmpty()) {
                        updateSelectedDatetime(accDateList.last())
                    }
                    updateDateList(accDateList)
                }
                updateSensorUiState(accDataList)
            }
        }
    }

    // 加速度データからx, y ,z の最小値・最大値を計算して、UiStateを更新
    private fun updateSensorUiState(accDataList: List<AccData>) {
        val accDataList = accDataDateFilter(accDataList).toMutableList()
        if (accDataList.isEmpty()) {
            return
        }
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
    private fun updateDateList(accDataList: List<String>) {
        _uiState.update {
            it.copy(
                dateList = accDataList,
            )
        }
    }

    // 加速度データから選択されている年月日だけのものを抜き出す
    private fun accDataDateFilter(accDataList: List<AccData>): List<AccData> {
        return when (_uiState.value.selectedDateTime) {
            null -> accDataList
            else -> accDataList.filter {
                it.date.format(DateTimeFormatter.ISO_LOCAL_DATE) == _uiState.value.selectedDateTime
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
                isLoading = isLoading,
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
                xEnd = xAxis.second,
            )
        }
    }

    // 日付が選択されたら、min, maxのスケールを初期化する
    // その日によって違うから
    fun updateSelectedDatetime(selectedDateTime: String) {
        _uiState.update {
            it.copy(
                selectedDateTime = selectedDateTime,
                minValue = 0.0,
                maxValue = 0.0,
                selectTimeTerm = TimeTerm.Day,
            )
        }
        viewModelScope.launch {
            val getAsyncApiAccelData = async { getApiAccelDataUseCase(selectedDateTime) }
            getAsyncApiAccelData.await()
        }
        selectReload()
    }

    fun selectTimeTerm(selectTimeTerm: TimeTerm) {
        _uiState.update {
            it.copy(
                selectTimeTerm = selectTimeTerm,
            )
        }
        selectReload()
    }

    fun getDateLabelText(): String {
        val selectAccData = _uiState.value.selectData
        return if (selectAccData != null) {
            timeManager.dateToText(selectAccData.date)
        } else {
            ""
        }
    }

    fun convertDateToTime(dateTime: LocalDateTime): Long {
        return timeManager.dateToEpochTime(dateTime)
    }
}
