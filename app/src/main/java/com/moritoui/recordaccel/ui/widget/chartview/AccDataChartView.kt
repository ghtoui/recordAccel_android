package com.moritoui.recordaccel.ui.widget.chartview

import android.graphics.PointF
import android.util.Log
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
        }
    }
}
