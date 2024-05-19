package com.moritoui.recordaccel.ui.navigation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserKind
import com.moritoui.recordaccel.usecases.GetUserListUseCase
import com.moritoui.recordaccel.usecases.IsRegisterUserUseCase
import com.moritoui.recordaccel.usecases.LoadUserListUseCase
import com.moritoui.recordaccel.usecases.RegisterUserDataStoreUseCase
import com.moritoui.recordaccel.usecases.RemoveUserDataStoreUseCase
import com.moritoui.recordaccel.usecases.SetSelectedUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MainScreenUiState(
    val isOpenOtherRegisterDialog: Boolean = false,
    val isOpenSelfRegisterDialog: Boolean = false,
    val isNotAddSelfUser: Boolean = false,
    val selfNameInputText: String = "",
    val otherNameInputText: String = "",
    val idInputText: String = "",
    val isRegisterUser: Boolean = false,
    val isRegisterLoading: Boolean = false,
    val isSearchUserError: Boolean = false,
    val isOpenBottomSheet: Boolean = false,
    val selfUserId: String? = null,
    val isEdit: Boolean = false,
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val registerUserDataStoreUseCase: RegisterUserDataStoreUseCase,
    private val removeUserDataStoreUseCase: RemoveUserDataStoreUseCase,
    private val isRegisterUserUseCase: IsRegisterUserUseCase,
    private val loadUserListUseCase: LoadUserListUseCase,
    getUserListUseCase: GetUserListUseCase,
    private val setSelectedUserUseCase: SetSelectedUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()
    val userList: StateFlow<List<User>> = getUserListUseCase()

    init {
        viewModelScope.launch {
            async { loadUserListUseCase() }.await()
            _uiState.update { currentState ->
                currentState.copy(isNotAddSelfUser = userList.value.all { it.userKind != UserKind.Self })
            }
        }
    }

    fun registerUser(isSelf: Boolean) {
        var error = false
        val registerUser = if (isSelf) {
            User(
                userId = UUID.randomUUID().toString(),
                userKind = UserKind.Self,
                userName = _uiState.value.selfNameInputText,
            )
        } else {
            User(
                userId = _uiState.value.idInputText,
                userKind = UserKind.Other,
                userName = _uiState.value.otherNameInputText,
            )
        }
        // 連打できないように
        if (_uiState.value.isRegisterLoading) {
            return
        }
        updateRegisterLoadingState(true)
        updateSearchUserErrorState(false)
        viewModelScope.launch {
            if (registerUser.userKind == UserKind.Self) {
                registerUserDataStoreUseCase(registerUser)
                resetRegisterUiState()
                closeDialog()
            } else if (isRegisterUserUseCase(registerUser.userId)) {
                registerUserDataStoreUseCase(registerUser)
                resetRegisterUiState()
                closeDialog()
            } else {
                updateSearchUserErrorState(true)
                error = true
            }
            updateRegisterLoadingState(false)
            if (error) {
                delay(5000)
                updateSearchUserErrorState(false)
            }
            _uiState.update { currentState ->
                currentState.copy(isNotAddSelfUser = userList.value.all { it.userKind != UserKind.Self })
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            removeUserDataStoreUseCase(user)
        }
    }

    fun onChangedSelfNameTextField(inputSelfNameText: String) {
        _uiState.update {
            it.copy(
                isRegisterUser = inputSelfNameText.isNotEmpty(),
                selfNameInputText = inputSelfNameText,
            )
        }
    }

    fun onChangedIdTextField(inputIdText: String) {
        _uiState.update {
            it.copy(
                idInputText = inputIdText,
                isRegisterUser = checkUUID(inputIdText) && _uiState.value.otherNameInputText.isNotEmpty(),
            )
        }
    }

    fun onChangedOtherNameTextField(inputOtherName: String) {
        _uiState.update {
            it.copy(
                otherNameInputText = inputOtherName,
                isRegisterUser = inputOtherName.isNotEmpty() && checkUUID(_uiState.value.idInputText),
            )
        }
    }

    fun openSelfUserRegisterDialog() {
        _uiState.update {
            it.copy(isOpenSelfRegisterDialog = true)
        }
    }

    fun openOtherUserRegisterDialog() {
        _uiState.update {
            it.copy(isOpenOtherRegisterDialog = true)
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                isOpenSelfRegisterDialog = false,
                isOpenOtherRegisterDialog = false,
            )
        }
        resetRegisterUiState()
    }

    fun selectUser(selectUser: User) {
        setSelectedUserUseCase(selectUser)
    }

    fun openSelfUserInfoBottomSheet() {
        _uiState.update {
            it.copy(
                selfUserId = userList.value.firstOrNull { it.userKind == UserKind.Self }?.userId,
                isOpenBottomSheet = true,
            )
        }
    }

    fun closeSelfUserInfoBottomSheet() {
        _uiState.update {
            it.copy(isOpenBottomSheet = false)
        }
    }

    fun changeEditState(changeEditState: Boolean) {
        _uiState.update {
            it.copy(
                isEdit = changeEditState,
            )
        }
    }

    private fun checkUUID(id: String): Boolean {
        try {
            UUID.fromString(id)
        } catch (err: Exception) {
            return false
        }
        return true
    }

    private fun resetRegisterUiState() {
        _uiState.update {
            it.copy(
                isRegisterUser = false,
                idInputText = "",
                otherNameInputText = "",
                selfNameInputText = "",
            )
        }
        updateRegisterLoadingState(false)
        updateSearchUserErrorState(false)
    }

    private fun updateRegisterLoadingState(isLoading: Boolean) {
        _uiState.update {
            it.copy(
                isRegisterLoading = isLoading,
            )
        }
    }

    private fun updateSearchUserErrorState(isSearchError: Boolean) {
        _uiState.update {
            it.copy(
                isSearchUserError = isSearchError,
            )
        }
    }
}
