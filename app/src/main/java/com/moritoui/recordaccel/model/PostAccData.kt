package com.moritoui.recordaccel.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccDataJsonData(
    @Json(name = "_id")
    val id: String,
    @Json(name = "accDatas")
    val accDatas: List<AccDataJson>,
)

@JsonClass(generateAdapter = true)
data class AccDataJson(
    @Json(name = "date")
    val date: String,
    @Json(name = "accData")
    val accData: Double,
)

@JsonClass(generateAdapter = true)
data class PostAccData(
    @Json(name = "userId")
    val userId: String,
    @Json(name = "accData")
    val accData: Double,
    @Json(name = "date")
    val date: String,
)
