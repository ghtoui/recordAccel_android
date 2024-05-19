package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.repositories.UserListDataRepository
import javax.inject.Inject

class LoadUserListUseCase @Inject constructor(
    private val userListDataRepository: UserListDataRepository,
) {
    suspend operator fun invoke() {
        userListDataRepository.loadUserList()
    }
}
