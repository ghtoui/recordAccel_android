
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserKind
import com.moritoui.recordaccel.model.UserList
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import com.moritoui.recordaccel.viewModel.MainScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenTopBar(
    isEdit: Boolean,
    onEditButtonClick: () -> Unit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("みまもり")
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painterResource(id = R.drawable.guardian),
                        contentDescription = null
                    )
                }
                IconButton(
                    onClick = { onEditButtonClick() },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    when (isEdit) {
                        true -> Icon(
                            Icons.Filled.Check,
                            contentDescription = null
                        )

                        false -> Icon(
                            Icons.Filled.Edit,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MainScreen(
    popUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val userList by viewModel.userList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            MainScreenTopBar(
                isEdit = uiState.isEdit,
                onEditButtonClick = remember {
                    { viewModel.changeEditState(!uiState.isEdit) }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MainUserList(
                isEdit = uiState.isEdit,
                userList = userList,
                popUp = remember {
                    {
                        popUp()
                        viewModel.selectUser(it)
                    }
                },
                onIconButtonClick = viewModel::openSelfUserInfoBottomSheet,
                onDeleteButtonClick = remember {
                    { viewModel.deleteUser(it) }
                },
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
            inputText = uiState.selfNameInputText,
            isRegister = uiState.isRegisterUser,
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
        SelfUserInfoBottomSheet(
            clipBoardManager = LocalClipboardManager.current,
            isOpenBottomSheet = uiState.isOpenBottomSheet,
            userId = uiState.selfUserId,
            onDismissBottomSheet = viewModel::closeSelfUserInfoBottomSheet,
            onCopyButtonClick = remember {
                {
                    viewModel.closeSelfUserInfoBottomSheet()
                    scope.launch {
                        snackBarHostState.showSnackbar("コピーしました！")
                    }
                }
            },
            modifier = Modifier
                .height((LocalConfiguration.current.screenHeightDp / 2).dp)
        )
    }
}

@Composable
fun MainUserList(
    userList: List<User>,
    isEdit: Boolean,
    popUp: (User) -> Unit,
    onIconButtonClick: () -> Unit,
    onDeleteButtonClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        items(
            userList,
            key = { item -> item.uuid }
        ) { user ->
            key(user.uuid) {
                ListElement(
                    isEdit = isEdit,
                    user = user,
                    onElementClick = popUp,
                    onIconButtonClick = onIconButtonClick,
                    onDeleteButtonClick = onDeleteButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ListElement(
    user: User,
    isEdit: Boolean,
    onElementClick: (User) -> Unit,
    onIconButtonClick: () -> Unit,
    onDeleteButtonClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onElementClick(user) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                user.userName,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 28.sp
            )
            when (user.userKind) {
                UserKind.Self -> Image(
                    painterResource(id = R.drawable.person),
                    contentDescription = null
                )
                UserKind.Other -> Image(
                    painterResource(id = R.drawable.groups),
                    contentDescription = null
                )
            }
            if (user.userKind == UserKind.Self && !isEdit) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onIconButtonClick() },
                    modifier = Modifier
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = stringResource(id = R.string.setting_button_description),
                        modifier = Modifier
                            .aspectRatio(1f / 1f)
                    )
                }
            }
            if (isEdit) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        expanded = true
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pending),
                        contentDescription = null
                    )
                    EditDropDownMenu(
                        expanded = expanded,
                        onDismissClick = { expanded = false },
                        onDeleteButtonClick = remember {
                            {
                                expanded = false
                                onDeleteButtonClick(user)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditDropDownMenu(
    expanded: Boolean,
    onDismissClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissClick() },
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.delete_text))
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            },
            onClick = { onDeleteButtonClick() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfUserInfoBottomSheet(
    clipBoardManager: ClipboardManager,
    isOpenBottomSheet: Boolean,
    userId: String?,
    onDismissBottomSheet: () -> Unit,
    onCopyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpenBottomSheet && userId != null) {
        ModalBottomSheet(
            onDismissRequest = { onDismissBottomSheet() },
            modifier = modifier
        ) {
            ShowUserId(
                clipBoardManager = clipBoardManager,
                userId = userId,
                onCopyButtonClick = onCopyButtonClick
            )
        }
    }
}

@Composable
fun ShowUserId(
    clipBoardManager: ClipboardManager,
    userId: String,
    onCopyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(stringResource(R.string.user_id_text))
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(userId)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    clipBoardManager.setText(AnnotatedString(userId))
                    onCopyButtonClick()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.content_copy),
                    contentDescription = stringResource(R.string.userid_copy_button_description)
                )
            }
        }
        Divider(
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
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
                isEdit = true,
                userList = UserList.getUserList(),
                popUp = { },
                onIconButtonClick = {},
                onDeleteButtonClick = {}
            )
            UserAddButton(
                isNotAddSelfUser = isNotAddSelfUser,
                addSelfUserButtonClick = { /*TODO*/ },
                addOtherUserButtonClick = { /*TODO*/ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuPreview() {
    EditDropDownMenu(
        expanded = true,
        onDismissClick = { /*TODO*/ },
        onDeleteButtonClick = { /*TODO*/ }
    )
}
