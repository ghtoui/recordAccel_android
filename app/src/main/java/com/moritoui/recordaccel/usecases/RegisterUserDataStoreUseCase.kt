package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserListDataRepository
import javax.inject.Inject

class RegisterUserDataStoreUseCase @Inject constructor(
    private val userListDataRepository: UserListDataRepository
) {
    suspend operator fun invoke(user: User) {
        userListDataRepository.addUser(user)
        userListDataRepository.saveUserList()
    }
}
