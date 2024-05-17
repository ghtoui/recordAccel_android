package com.moritoui.recordaccel.ui.navigation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.TimeTerm
import com.moritoui.recordaccel.preview.MultiDevicePreview
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.ui.widget.buttons.DateTimeRangeChangeButton
import com.moritoui.recordaccel.ui.widget.chartview.AccChartView
import java.time.LocalDateTime

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    DetailScreen(
        uiState = uiState,
        onClickTerm = viewModel::selectTimeTerm,
        dateToText = viewModel::getDateLabelText,
        convertDateToDate = viewModel::convertDateToTime,
        onClickDateTimeElement = viewModel::updateSelectedDatetime
    )
}

@Composable
private fun DetailScreen(
    uiState: DetailScreenUiState,
    onClickTerm: (TimeTerm) -> Unit,
    dateToText: () -> String,
    convertDateToDate: (LocalDateTime) -> Long,
    onClickDateTimeElement: (String) -> Unit
) {
    Column {
        DateTimeRangeChangeButton(
            selectTimeTerm = uiState.selectTimeTerm,
            onClickTerm = onClickTerm
        )
        ShowTapDataInformation(
            accData = uiState.selectData,
            dateToText = dateToText,
            modifier = Modifier
                .padding(16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp / 2.5).dp),
        ) {
            AccChartView(
                accDataList = uiState.accDataList,
                minValue = uiState.minValue,
                maxValue = uiState.maxValue,
                xAxisStart = uiState.xStart,
                xAxisEnd = uiState.xEnd,
                convertDateToDate = convertDateToDate,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        DateList(
            dateList = uiState.dateList,
            onClickDateTimeElement = onClickDateTimeElement,
            modifier = Modifier
                .weight(1f),
        )
    }
}

@Composable
fun ShowTapDataInformation(
    modifier: Modifier = Modifier,
    accData: AccData?,
    dateToText: () -> String,
) {
    accData ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = dateToText()
        )
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (accData.isMove) {
                            true -> Color.Black
                            false -> Color.Red
                        }
                    ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                when (accData.isMove) {
                    true -> stringResource(id = R.string.move_graph_label_text)
                    false -> stringResource(id = R.string.unmove_graph_label_text)
                }
            )
        }
    }
}

@Composable
fun DateList(
    dateList: List<String>,
    onClickDateTimeElement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (dateList.isEmpty()) {
        return
    }
    var selectedDate: String? by rememberSaveable { mutableStateOf(dateList.last()) }
    LazyColumn(
        modifier = modifier,
    ) {
        items(dateList.asReversed()) { date ->
            DateListElement(
                text = date,
                onClickDateTimeElement = {
                    if (it != selectedDate) {
                        selectedDate = it
                        onClickDateTimeElement(it)
                    }
                },
                // 親で処理することで、子の無駄なreComposeを防ぐことができる
                isSelected = when (selectedDate) {
                    date -> true
                    else -> false
                },
            )
        }
    }
}

@Composable
fun DateListElement(
    text: String,
    isSelected: Boolean,
    onClickDateTimeElement: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp)
            .clickable { onClickDateTimeElement(text) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (isSelected) {
                        true -> MaterialTheme.colorScheme.primaryContainer
                        false -> Color.Unspecified
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
            )
        }
    }
}

@MultiDevicePreview
@Composable
private fun DetailScreenPreview() {
    RecordAccelTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            DetailScreen(
                uiState = DetailScreenUiState.initialState(),
                onClickTerm = {},
                dateToText = {"2020"},
                convertDateToDate = {0},
                onClickDateTimeElement = {}
            )
        }
    }
}
