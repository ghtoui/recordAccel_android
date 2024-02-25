package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.repositories.UserListDataRepository
import javax.inject.Inject

class SetSelectedUserUseCase @Inject constructor(
    private val userListDataRepository: UserListDataRepository
) {
    operator fun invoke (selectUser: User) {
        userListDataRepository.selectedUser = selectUser
    }
}
