package com.moritoui.recordaccel.ui

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.model.AccDataList
import com.moritoui.recordaccel.model.TimeManager
import com.moritoui.recordaccel.viewModel.DetailScreenViewModel

private val timeManager = TimeManager()

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        AccChartView(
            accDataList = uiState.accDataList,
            minXValue = uiState.minXValue,
            maxXValue = uiState.maxXValue,
            minYValue = uiState.minYValue,
            maxYValue = uiState.maxYValue,
            minZValue = uiState.minZValue,
            maxZValue = uiState.maxZValue,
            modifier = Modifier
                .fillMaxWidth()
                .height((LocalConfiguration.current.screenHeightDp / 3).dp)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AccChartView(
    accDataList: List<AccData>,
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

    val start = timeManager.stringToEpochTime(accDataList.first().date)
    val end = timeManager.stringToEpochTime(accDataList.last().date)

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

@Preview
@Composable
fun DetailScreenPreview() {
    Surface() {
        val accDataList = AccDataList.getAccDataList()
        AccChartView(
            accDataList = accDataList,
            modifier = Modifier.fillMaxSize(),
            minXValue = accDataList.minOf { it.accX },
            maxXValue = accDataList.maxOf { it.accX },
            minYValue = accDataList.minOf { it.accY },
            maxYValue = accDataList.maxOf { it.accY },
            minZValue = accDataList.minOf { it.accZ },
            maxZValue = accDataList.maxOf { it.accZ }
        )
    }
}
