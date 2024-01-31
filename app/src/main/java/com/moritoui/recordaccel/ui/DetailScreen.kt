package com.moritoui.recordaccel.ui

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
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
    AccChartView(
        accDataList = AccDataList.getAccDataList(),
        modifier = Modifier.fillMaxSize()
    )
    Text("detail")
}

@Composable
fun AccChartView(
    accDataList: List<AccData>,
    modifier: Modifier = Modifier
) {
    var maxValue by rememberSaveable { mutableFloatStateOf(0.0f) }
    var minValue by rememberSaveable { mutableFloatStateOf(0.0f) }
    var start by rememberSaveable { mutableLongStateOf(0) }
    var end by rememberSaveable { mutableLongStateOf(0) }

    minValue = accDataList.minOf { it.accX }
    maxValue = accDataList.maxOf { it.accX }
    start = timeManager.stringToEpochTime(accDataList.first().date)
    end = timeManager.stringToEpochTime(accDataList.last().date)
    Log.d("value", "$minValue $maxValue $start $end")

    Canvas(
        modifier = modifier
            .background(Color.White),
    ) {
        val path = Path()
        accDataList.forEachIndexed { index, accData ->
            val pathXY = getChartPath(
                canvasSize = size,
                value = accData.accX,
                maxValue = maxValue,
                minValue = minValue,
                time = timeManager.stringToEpochTime(accData.date),
                start = start,
                end = end
            )
            Log.d("path", pathXY.toString())
            when (index) {
                0 -> path.moveTo(pathXY.x, pathXY.y)
                else -> path.lineTo(
                    pathXY.x, pathXY.y
                )
            }
        }

        drawPath(
            path = path,
            color = Color.Black,
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
    Log.d("path", timeRange.toString())
    Log.d("path", valueOffset.toString())

    return PointF(
        width * timeOffset / timeRange,
        height * valueOffset / valueRange
    )

}

@Preview
@Composable
fun DetailScreenPreview() {
    Surface() {
        AccChartView(
            accDataList = AccDataList.getAccDataList(),
            modifier = Modifier.fillMaxSize()
        )
    }
}
