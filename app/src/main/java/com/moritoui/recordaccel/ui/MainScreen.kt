import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserList
import com.moritoui.recordaccel.viewModel.MainScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    popUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    var accData by rememberSaveable { mutableStateOf("") }
    MainUserList(
        userList = UserList.getUserList(),
        popUp = popUp
    )
//    ShowAccel(
//        updateAccData = {
//            accData = viewModel.getAccData()
//        },
//        accData =  accData
//    )
}

@Composable
fun MainUserList(
    userList: List<User>,
    popUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(userList) { user ->
            ListElement(
                user = user,
                onElementClick = popUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
            )
        }
    }
}

@Composable
fun ListElement(
    user: User,
    onElementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(top = 10.dp)
            .clickable { onElementClick() }
    ) {
        Text(
            user.name,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 28.sp
        )
    }
}

@Composable
fun ShowAccel(
    updateAccData: () -> Unit,
    accData: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        // 一秒に一回Modelから加速度センサの値を読み込む
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                updateAccData()
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
    MainUserList(
        userList = UserList.getUserList(),
        popUp = { }
    )
}
