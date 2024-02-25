package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.repositories.UserListDataRepository
import javax.inject.Inject

class GetSelectedUserUseCase @Inject constructor(
    private val userListDataRepository: UserListDataRepository
) {
    operator fun invoke(): User? {
        return userListDataRepository.selectedUser
    }
}
