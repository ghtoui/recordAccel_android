import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.model.MotionSensor
import com.moritoui.recordaccel.viewModel.MainScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    var accData by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 一秒に一回Modelから加速度センサの値を読み込む
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                accData = viewModel.getAccData()
            }
        }
        Text(
            text = "$accData",
            textAlign = TextAlign.Center
        )
        TextButton(onClick = { }) {
            Text(
                text = "取得開始"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
}
