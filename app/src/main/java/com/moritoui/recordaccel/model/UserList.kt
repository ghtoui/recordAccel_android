package com.moritoui.recordaccel.model

import java.util.UUID

object UserList {
    fun getUserList(): List<User> {
        return listOf<User>(
            User(userName = "mori", userId = UUID.randomUUID().toString(), UserKind.Self),
            User(userName = "mori2", userId = UUID.randomUUID().toString(), UserKind.Other),
            User(userName = "mori3", userId = UUID.randomUUID().toString(), UserKind.Other),
        )
    }
}
