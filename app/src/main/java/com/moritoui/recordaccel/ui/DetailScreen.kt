package com.moritoui.recordaccel.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.viewModel.DetailScreenViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeManager = TimeManager()

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp / 3).dp)
        ) {
            AccChartView(
                accDataList = uiState.accDataList,
                minXValue = uiState.minXValue,
                maxXValue = uiState.maxXValue,
                minYValue = uiState.minYValue,
                maxYValue = uiState.maxYValue,
                minZValue = uiState.minZValue,
                maxZValue = uiState.maxZValue,
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
                    viewModel.selectedDateTime(it)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AccChartView(
    accDataList: MutableList<AccData>,
    minXValue: Float,
    maxXValue: Float,
    minYValue: Float,
    maxYValue: Float,
    minZValue: Float,
    maxZValue: Float,
    modifier: Modifier = Modifier
) {
    if (accDataList.isEmpty()) {
        return
    }

//    val start = timeManager.stringToEpochTime(accDataList.first().date)
//    val end = timeManager.stringToEpochTime(accDataList.last().date)
    val start = timeManager.textToDate(accDataList.first().date).withHour(12).withMinute(0).withSecond(0).atZone(
        ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = timeManager.textToDate(accDataList.last().date).withHour(18).withMinute(59).withSecond(59).atZone(
        ZoneId.systemDefault()).toInstant().toEpochMilli()

    Canvas(
        modifier = modifier
    ) {
        val accXPath = Path()
        val accYPath = Path()
        val accZPath = Path()
        accDataList.forEachIndexed { index, accData ->
            val accXPathXY = getChartPath(
                canvasSize = size,
                value = accData.accX,
                maxValue = maxXValue,
                minValue = minXValue,
                time = timeManager.stringToEpochTime(accData.date),
                start = start,
                end = end
            )
            val accYPathXY = getChartPath(
                canvasSize = size,
                value = accData.accY,
                maxValue = maxYValue,
                minValue = minYValue,
                time = timeManager.stringToEpochTime(accData.date),
                start = start,
                end = end
            )
            val accZPathXY = getChartPath(
                canvasSize = size,
                value = accData.accZ,
                maxValue = maxZValue,
                minValue = minZValue,
                time = timeManager.stringToEpochTime(accData.date),
                start = start,
                end = end
            )

            when (index) {
                0 -> {
                    accXPath.moveTo(accXPathXY.x, accXPathXY.y)
                    accYPath.moveTo(accYPathXY.x, accYPathXY.y)
                    accZPath.moveTo(accZPathXY.x, accZPathXY.y)
                }
                else -> {
                    accXPath.lineTo(accXPathXY.x, accXPathXY.y)
                    accYPath.lineTo(accYPathXY.x, accYPathXY.y)
                    accZPath.lineTo(accZPathXY.x, accZPathXY.y)
                }
            }
        }

        drawPath(
            path = accXPath,
            color = Color.Red,
            style = Stroke(width = 8f)
        )
        drawPath(
            path = accYPath,
            color = Color.Green,
            style = Stroke(width = 8f)
        )
        drawPath(
            path = accZPath,
            color = Color.Blue,
            style = Stroke(width = 8f)
        )
    }
}

fun getChartPath(
    canvasSize: Size,
    value: Float,
    maxValue: Float,
    minValue: Float,
    time: Long,
    start: Long,
    end: Long
): PointF {
    val width = canvasSize.width
    val height = canvasSize.height
    val timeRange = end - start
    val timeOffset = time - start
    val valueRange = maxValue - minValue
    val valueOffset = maxValue - value

    val x = width * timeOffset / timeRange
    var y = height * valueOffset / valueRange
    if (y.isNaN()) {
        y = height * value
    }

    return PointF(x, y)
}

@Composable
fun DateList(
    dateList: MutableList<String>,
    onClickDateTimeElement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate: String? by rememberSaveable { mutableStateOf(null) }
    LazyColumn(
        modifier = modifier
    ) {
        items(dateList.asReversed()) { date ->
            DateListElement(
                text = date,
                onClickDateTimeElement = {
                    selectedDate = when (it) {
                        selectedDate -> null
                        else -> it
                    }
                    onClickDateTimeElement(it)

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

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun DetailScreenPreview() {
    val timeManager = TimeManager()
    var accDataList = AccDataList.getAccDataList()
    val dateList = accDataList.groupBy { timeManager.textToDate(it.date).format(DateTimeFormatter.ISO_LOCAL_DATE) }.keys.toList()
    var selectDate = dateList.last()
    accDataList = accDataList.filter {
        timeManager.textToDate(it.date).format(DateTimeFormatter.ISO_LOCAL_DATE) == selectDate
    }
    RecordAccelTheme {
        Surface {
            Column {
                AccChartView(
                    accDataList = accDataList.toMutableList(),
                    minXValue = accDataList.minOf { it.accX },
                    maxXValue = accDataList.maxOf { it.accX },
                    minYValue = accDataList.minOf { it.accY },
                    maxYValue = accDataList.maxOf { it.accY },
                    minZValue = accDataList.minOf { it.accZ },
                    maxZValue = accDataList.maxOf { it.accZ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((LocalConfiguration.current.screenHeightDp / 3).dp)
                        .padding(16.dp)
                )
                DateList(
                    dateList = dateList.reversed().toMutableList(),
                    onClickDateTimeElement = { selectDate = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
