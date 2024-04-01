package com.moritoui.recordaccel.usecases

import com.moritoui.recordaccel.network.AccelApiService
import javax.inject.Inject

class IsRegisterUserUseCase @Inject constructor(
    private val apiService: AccelApiService,
) {
    suspend operator fun invoke(userId: String): Boolean {
        val isRegisterUser = try {
            apiService.searchUser(userId).body() ?: false
        } catch (error: Exception) {
            false
        }
        return isRegisterUser
    }
}
