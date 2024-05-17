package com.moritoui.recordaccel.ui

import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.TimeTerm
import com.moritoui.recordaccel.preview.MultiDevicePreview
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.viewModel.DetailScreenUiState
import com.moritoui.recordaccel.viewModel.DetailScreenViewModel
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
            onClickTerm = onClickTerm,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccChartView(
    accDataList: List<AccData>,
    minValue: Double,
    maxValue: Double,
    xAxisStart: Long,
    xAxisEnd: Long,
    convertDateToDate: (LocalDateTime) -> Long,
    modifier: Modifier = Modifier,
) {
    if (accDataList.isEmpty()) {
        return
    }
    val points: MutableList<Offset> = mutableListOf()
    val zeroPoints: MutableList<Offset> = mutableListOf()

    val textMeasurer = rememberTextMeasurer()
    val pointSize = 30.toFloat()

    Box(
        modifier = modifier,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent: MotionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            Log.d("action", "tap")
                            Log.d("tap", "${motionEvent.x}, ${motionEvent.y}")
                        }

                        MotionEvent.ACTION_MOVE -> {
                            Log.d("action", "move")
                            Log.d("move", "${motionEvent.x}, ${motionEvent.y}")
                        }

                        MotionEvent.ACTION_UP -> {
                            Log.d("action", "up")
                        }

                        else -> return@pointerInteropFilter false
                    }
                    true
                },
        ) {
            val resultAccPath = Path()
            accDataList.forEachIndexed { index, accData ->
                val resultAccPathXY = getChartPath(
                    canvasWidthSize = size.width,
                    canvasHeightSize = size.height - 60,
                    value = accData.resultAcc,
                    maxValue = maxValue,
                    minValue = minValue,
                    time = convertDateToDate(accData.date),
                    xAxisStart = xAxisStart,
                    xAxisEnd = xAxisEnd,
                )

                when (accData.isMove) {
                    true -> zeroPoints.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
                    false -> points.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
                }
                when (index) {
                    0 -> {
                        resultAccPath.moveTo(resultAccPathXY.x, resultAccPathXY.y)
                    }

                    else -> {
                        resultAccPath.lineTo(resultAccPathXY.x, resultAccPathXY.y)
                    }
                }
            }
            //        drawText(
            //            topLeft = Offset(size.width, size.height),
            //            topLeft = Offset(600.toFloat(), size.height - 40),
            //            style = TextStyle.Default,
            //            textMeasurer = textMeasurer,
            //            text = "aaaaaaaaaa"
            //        )

            drawPoints(
                points = points,
                pointMode = PointMode.Points,
                color = Color.Black,
                strokeWidth = pointSize,
            )
            drawPoints(
                points = zeroPoints,
                pointMode = PointMode.Points,
                color = Color.Red,
                strokeWidth = pointSize,
            )
            //        drawPath(
            //            path = resultAccPath,
            //            color = Color.Black,
            //            style = Stroke(8f)
            //        )
        }

        DrawLabel(
            modifier = Modifier
                .align(Alignment.TopEnd),
        )
    }
}

@Composable
fun DrawLabel(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(
                color = Color.DarkGray,
                width = 1.dp,
            )
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Black),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.move_graph_label_text),
                modifier = Modifier,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Red),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.unmove_graph_label_text),
                modifier = Modifier,
            )
        }
    }
}

fun getChartPath(
    canvasHeightSize: Float,
    canvasWidthSize: Float,
    value: Double,
    maxValue: Double,
    minValue: Double,
    time: Long,
    xAxisStart: Long,
    xAxisEnd: Long,
): PointF {
    val width = canvasWidthSize
    val height = canvasHeightSize
    val timeRange = xAxisEnd - xAxisStart
    val timeOffset = time - xAxisStart
    val valueRange = maxValue - minValue
    val valueOffset = maxValue - value

    val x = width * timeOffset / timeRange
    var y = height * valueOffset / valueRange
    if (y.isNaN()) {
        y = height * value
    }

    return PointF(x, y.toFloat())
}

@Composable
fun ShowTapDataInformation(
    accData: AccData?,
    dateToText: () -> String,
    modifier: Modifier = Modifier
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

@Composable
fun DateTimeRangeChangeButton(
    selectTimeTerm: TimeTerm,
    onClickTerm: (TimeTerm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TimeTerm.values().forEach {
            OutlinedButton(
                onClick = {
                    onClickTerm(it)
                },
                colors = when (selectTimeTerm) {
                    it -> ButtonDefaults.buttonColors()
                    else -> ButtonDefaults.outlinedButtonColors()
                },
            ) {
                Text(
                    stringResource(R.string.date_change_buttontext, it.text),
                )
            }
        }
    }
}

@MultiDevicePreview
@Composable
private fun DetailScreenPreview() {
    val timeManager = TimeManager()
    var accDataList = AccDataList.getAccDataList()
    val dateList = accDataList.groupBy { it.date }.keys.toList()
    val selectDate = dateList.last()
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
