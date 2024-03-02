package com.moritoui.recordaccel.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "user_id")
    val userId: String,
    @Json(name = "user_kind")
    val userKind: UserKind,
    @Json(ignore = true)
    val uuid: UUID = UUID.randomUUID()
)

enum class UserKind {
    Self,
    Other
}
