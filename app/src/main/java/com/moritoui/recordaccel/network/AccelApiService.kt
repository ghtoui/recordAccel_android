package com.moritoui.recordaccel.network

import com.moritoui.recordaccel.model.AccDataJsonData
import com.moritoui.recordaccel.model.PostAccData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AccelApiService {
    @GET("/date")
    suspend fun getAccDateList(
        @Query("userId") userId: String,
        @Query("pageNumber") pageNumber: Int
    ): Response<List<String>>

    @GET("/")
    suspend fun getAccDataList(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<List<AccDataJsonData>>

    @POST("/")
    suspend fun postAccData(
        @Body body:  List<PostAccData>
    )
}
