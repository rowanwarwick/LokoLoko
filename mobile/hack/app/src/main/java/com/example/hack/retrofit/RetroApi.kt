package com.example.hack.retrofit

import com.example.hack.retrofit.model.Request
import com.example.hack.retrofit.model.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetroApi {

    @POST("/predict")
    suspend fun getDamage(@Query("text") text: String): Response

}