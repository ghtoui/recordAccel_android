package com.moritoui.recordaccel.ui.widget.chartview

import android.graphics.PointF
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.dummies.DetailScreenDummies
import com.moritoui.recordaccel.model.AccData
import com.moritoui.recordaccel.preview.MultiDevicePreview
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import java.time.LocalDateTime

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccChartView(
    accDataList: List<AccData>,
    minValue: Double,
    maxValue: Double,
    xAxisStart: Long,
    xAxisEnd: Long,
    convertDateToDate: (LocalDateTime) -> Long,
    onClickGraph: (Float, Float, MotionEvent?) -> Unit,
    selectedData: AccData?,
    modifier: Modifier = Modifier,
) {
    if (accDataList.isEmpty()) {
        return
    }
    val points: MutableList<Offset> = mutableListOf()
    val zeroPoints: MutableList<Offset> = mutableListOf()
    val selectedPoints: MutableList<Offset> = mutableListOf()

    val textMeasurer = rememberTextMeasurer()
    val pointSize = 30.toFloat()

    var width by rememberSaveable { mutableFloatStateOf(0.0.toFloat()) }
    var height by rememberSaveable { mutableFloatStateOf(0.0.toFloat()) }

    Box(
        modifier = modifier,
    ) {
        val selectedPointColor = RecordAccelTheme.colors.primary
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent: MotionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onClickGraph(height, width, motionEvent)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            onClickGraph(height, width, motionEvent)
                        }

                        else -> {
                            onClickGraph(height, width, null)
                            return@pointerInteropFilter false
                        }
                    }
                    true
                },
        ) {
            width = size.width
            height = size.height

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
                    true -> points.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
                    false -> zeroPoints.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
                }
                if (accData == selectedData) {
                    selectedPoints.add(Offset(resultAccPathXY.x, resultAccPathXY.y))
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
            drawPoints(
                points = selectedPoints,
                pointMode = PointMode.Points,
                color = selectedPointColor,
                strokeWidth = pointSize,
            )
        }

        DrawLabel(
            modifier = Modifier
                .align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun DrawLabel(
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

private fun getChartPath(
    canvasHeightSize: Float,
    canvasWidthSize: Float,
    value: Double,
    maxValue: Double,
    minValue: Double,
    time: Long,
    xAxisStart: Long,
    xAxisEnd: Long,
): PointF {
    val timeRange = xAxisEnd - xAxisStart
    val timeOffset = time - xAxisStart
    val valueRange = maxValue - minValue
    val valueOffset = maxValue - value

    val x = canvasWidthSize * timeOffset / timeRange
    var y = canvasHeightSize * valueOffset / valueRange
    if (y.isNaN()) {
        y = canvasHeightSize * value
    }

    return PointF(x, y.toFloat())
}

@MultiDevicePreview
@Preview
@Composable
private fun AccDataChartViewPreview() {
    RecordAccelTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            AccChartView(
                accDataList = DetailScreenDummies.accDataList,
                minValue = 0.0,
                maxValue = 1.0,
                xAxisStart = 0,
                xAxisEnd = 1,
                convertDateToDate = { 0 },
                onClickGraph = { _, _, _ -> },
                selectedData = DetailScreenDummies.accDataList.first(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
        }
    }
}
