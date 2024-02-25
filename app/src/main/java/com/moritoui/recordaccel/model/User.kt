package com.moritoui.recordaccel.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "user_name")
    val userName: String,
    @Json(name = "user_id")
    val userId: String,
    @Json(name = "user_kind")
    val userKind: UserKind
)

enum class UserKind {
    Self,
    Other
}
