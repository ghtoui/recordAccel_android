import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserList
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.viewModel.MainScreenViewModel

@Composable
fun MainScreen(
    popUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val userList by viewModel.userList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainUserList(
            userList = userList,
            popUp = popUp,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
        )
        UserAddButton(
            isNotAddSelfUser = uiState.isNotAddSelfUser,
            addSelfUserButtonClick = viewModel::openSelfUserRegisterDialog,
            addOtherUserButtonClick = viewModel::openOtherUserRegisterDialog
        )
    }
    SelfUserRegisterDialog(
        isOpenDiagram = uiState.isOpenSelfRegisterDialog,
        isRegister = uiState.isRegisterUser,
        inputText = uiState.selfNameInputText,
        textFieldChanged = { viewModel.onChangedSelfNameTextField(it) },
        onConfirmClick = { viewModel.registerUser(isSelf = true) },
        onDismissClick = viewModel::closeDialog
    )
    OtherUserRegisterDialog(
        isLoading = uiState.isRegisterLoading,
        isSearchUserError = uiState.isSearchUserError,
        isOpenDiagram = uiState.isOpenOtherRegisterDialog,
        isRegister = uiState.isRegisterUser,
        idInputText = uiState.idInputText,
        nameInputText = uiState.otherNameInputText,
        idTextFieldChanged = remember {
            { viewModel.onChangedIdTextField(it) }
        },
        nameTextFieldChanged = remember {
            { viewModel.onChangedOtherNameTextField(it) }
        },
        onConfirmClick = { viewModel.registerUser(isSelf = false) },
        onDismissClick = viewModel::closeDialog
    )
}

@Composable
fun MainUserList(
    userList: List<User>,
    popUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        items(userList) { user ->
            ListElement(
                user = user,
                onElementClick = popUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
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
            .clickable { onElementClick() }
    ) {
        Text(
            user.userName,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 28.sp
        )
    }
}

@Composable
fun SelfUserRegisterDialog(
    isOpenDiagram: Boolean,
    isRegister: Boolean,
    inputText: String,
    textFieldChanged: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpenDiagram) {
        AlertDialog(
            onDismissRequest = { onDismissClick() },
            title = { Text(stringResource(R.string.input_self_name)) },
            text = { TextField(value = inputText, onValueChange = { textFieldChanged(it) }) },
            confirmButton = {
                OutlinedButton(
                    onClick = { onConfirmClick() },
                    colors = if (isRegister) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(stringResource(R.string.register_button_text))
                }
            },
            dismissButton = {
                Button(onClick = { onDismissClick() }) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            },
            modifier = modifier
        )
    }
}

@Composable
fun OtherUserRegisterDialog(
    isLoading: Boolean,
    isSearchUserError: Boolean,
    isOpenDiagram: Boolean,
    isRegister: Boolean,
    idInputText: String,
    nameInputText: String,
    idTextFieldChanged: (String) -> Unit,
    nameTextFieldChanged: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpenDiagram) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(stringResource(R.string.other_user_register_title_text))
            },
            text = {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = idInputText,
                            onValueChange = { idTextFieldChanged(it) },
                            label = { Text(stringResource(R.string.id_text)) }
                        )
                        if (isSearchUserError) {
                            Text(
                                text = stringResource(R.string.other_user_register_error_text),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextField(
                            value = nameInputText,
                            onValueChange = { nameTextFieldChanged(it) },
                            label = { Text(stringResource(R.string.other_user_name_text)) }
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator()
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { onConfirmClick() },
                    colors = if (isRegister) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(stringResource(R.string.register_button_text))
                }
            },
            dismissButton = {
                Button(onClick = { onDismissClick() }) {
                    Text(stringResource(R.string.cancel_button_text))
                }
            },
            modifier = modifier
        )
    }
}

@Composable
fun UserAddButton(
    isNotAddSelfUser: Boolean,
    addSelfUserButtonClick: () -> Unit,
    addOtherUserButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isNotAddSelfUser) {
            Button(
                onClick = { addSelfUserButtonClick() },
                modifier = Modifier.padding(end = 15.dp)
            ) {
                Text(stringResource(R.string.user_add_button_text))
            }
        }
        Button(onClick = { addOtherUserButtonClick() }) {
            Text(stringResource(R.string.other_user_add_button_text))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    val isNotAddSelfUser = true
    RecordAccelTheme {
        Column {
            MainUserList(
                userList = UserList.getUserList(),
                popUp = { },
            )
            UserAddButton(
                isNotAddSelfUser = isNotAddSelfUser,
                addSelfUserButtonClick = { /*TODO*/ },
                addOtherUserButtonClick = { /*TODO*/ }
            )
        }
    }
}
