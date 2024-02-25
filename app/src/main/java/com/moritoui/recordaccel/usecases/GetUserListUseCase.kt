package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.repositories.UserListDataRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetUserListUseCase @Inject constructor(
    private val userListDataRepository: UserListDataRepository
) {
    operator fun invoke(): StateFlow<List<User>> {
        return userListDataRepository.userList
    }
}
