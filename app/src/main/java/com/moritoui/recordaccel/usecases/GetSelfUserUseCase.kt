package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.model.User
import com.moritoui.recordaccel.model.UserKind
import javax.inject.Inject

class GetSelfUserUseCase @Inject constructor(
    private val loadUserListUseCase: LoadUserListUseCase,
    private val getUserListUseCase: GetUserListUseCase
) {
    suspend operator fun invoke(): User? {
        loadUserListUseCase()
        return getUserListUseCase().value.first { it.userKind == UserKind.Self }
    }
}
