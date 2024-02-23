package com.moritoui.recordaccel.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.model.TimeTerm
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.viewModel.DetailScreenViewModel

private val timeManager: TimeManager = TimeManager()

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        DateTimeRangeChangeButton(
            selectTimeTerm = uiState.selectTimeTerm,
            onClickTerm = { viewModel.selectTimeTerm(it) },
            modifier = Modifier
                .height(50.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp / 3).dp)
        ) {
            AccChartView(
                accDataList = uiState.accDataList,
                minValue = uiState.minValue,
                maxValue = uiState.maxValue,
                xAxisStart = uiState.xStart,
                xAxisEnd = uiState.xEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        DateList(
            dateList = uiState.dateList,
            onClickDateTimeElement = remember {
                {
                    viewModel.updateSelectedDatetime(it)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccChartView(
    accDataList: MutableList<AccData>,
    minValue: Double,
    maxValue: Double,
    xAxisStart: Long,
    xAxisEnd: Long,
    modifier: Modifier = Modifier
) {
    if (accDataList.isEmpty()) {
        return
    }
    val points: MutableList<Offset> = mutableListOf()
    val zeroPoints: MutableList<Offset> = mutableListOf()

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .pointerInteropFilter { motionEvent : MotionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d("action", "tap")
                    }
                    MotionEvent.ACTION_MOVE -> {
                        Log.d("action", "move")
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d("action", "up")
                    }
                    else -> return@pointerInteropFilter  false
                }
                true
            }
    ) {
        val resultAccPath = Path()
        accDataList.forEachIndexed { index, accData ->
            val resultAccPathXY = getChartPath(
                canvasWidthSize = size.width,
                canvasHeightSize = size.height - 60,
                value = accData.resultAcc,
                maxValue = maxValue,
                minValue = minValue,
                time = timeManager.dateToEpochTime(accData.date),
                xAxisStart = xAxisStart,
                xAxisEnd = xAxisEnd
            )

            when (accData.resultAcc) {
                in 0.0..0.5 -> zeroPoints.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
                else -> points.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
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

        drawText(
//            topLeft = Offset(size.width, size.height),
            topLeft = Offset(600.toFloat(), size.height - 40),
            style = TextStyle.Default,
            textMeasurer = textMeasurer,
            text = "aaaaaaaaaa"
        )

        drawPoints(
            points = points,
            pointMode = PointMode.Points,
            color = Color.Black,
            strokeWidth = 30.toFloat()
        )
        drawPoints(
            points = zeroPoints,
            pointMode = PointMode.Points,
            color = Color.Red,
            strokeWidth = 30.toFloat()
        )
//        drawPath(
//            path = resultAccPath,
//            color = Color.Black,
//            style = Stroke(8f)
//        )
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
    xAxisEnd: Long
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
fun DateList(
    dateList: MutableList<String>,
    onClickDateTimeElement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dateList.isEmpty()) {
        return
    }
    var selectedDate: String? by rememberSaveable { mutableStateOf(dateList.last()) }
    LazyColumn(
        modifier = modifier
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
                }
            )
        }
    }
}

@Composable
fun DateListElement(
    text: String,
    isSelected: Boolean,
    onClickDateTimeElement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp)
            .clickable { onClickDateTimeElement(text) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (isSelected) {
                        true -> MaterialTheme.colorScheme.primaryContainer
                        false -> Color.Unspecified
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun DateTimeRangeChangeButton(
    selectTimeTerm: TimeTerm,
    onClickTerm: (TimeTerm) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TimeTerm.values().forEach {
            OutlinedButton(
                onClick = {
                    onClickTerm(it)
                },
                colors = when (selectTimeTerm) {
                    it -> ButtonDefaults.buttonColors()
                    else -> ButtonDefaults.outlinedButtonColors()
                }
            ) {
                Text(
                    it.text
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun DetailScreenPreview() {
    val timeManager = TimeManager()
    var accDataList = AccDataList.getAccDataList()
    val dateList = accDataList.groupBy { it.date }.keys.toList()
    val selectDate = dateList.last()
    var selectTimeTerm: TimeTerm = TimeTerm.Day
    val xAxisStart = timeManager.dateToEpochTime(accDataList.first().date.withHour(0).withMinute(0).withSecond(0))
    val xAxisEnd = timeManager.dateToEpochTime(accDataList.last().date.withHour(23).withMinute(59).withSecond(59))
    accDataList = accDataList.filter {
        it.date == selectDate
    }
    RecordAccelTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
//                DateTimeRangeChangeButton(
//                    selectTimeTerm = selectTimeTerm,
//                    onClickTerm = { selectTimeTerm = it },
//                    modifier = Modifier
//                        .height(50.dp)
//                )
                AccChartView(
                    accDataList = accDataList.toMutableList(),
                    minValue = accDataList.minOf { it.resultAcc },
                    maxValue = accDataList.maxOf { it.resultAcc },
                    xAxisStart = xAxisStart,
                    xAxisEnd = xAxisEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((LocalConfiguration.current.screenHeightDp / 3).dp)
                        .padding(16.dp)
                )
//                DateList(
//                    dateList = dateList.reversed().toMutableList(),
//                    onClickDateTimeElement = { selectDate = it },
//                    modifier = Modifier.weight(1f)
//                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun DetailScreenPreviewLight() {
    DetailScreenPreview()
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun DetailScreenPreviewDark() {
    DetailScreenPreview()
}
