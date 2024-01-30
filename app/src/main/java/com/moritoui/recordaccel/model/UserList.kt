package com.moritoui.recordaccel.model
object UserList {
    fun getUserList(): List<User> {
        return listOf<User>(
            User(name = "mori"),
            User(name = "mori2"),
            User(name = "mori3")
        )
    }
}
