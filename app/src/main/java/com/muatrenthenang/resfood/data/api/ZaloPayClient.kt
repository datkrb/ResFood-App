package com.muatrenthenang.resfood.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ZaloPayClient {
    // Assuming 10.0.2.2 for Android Emulator to access localhost
    private const val BASE_URL = "http://10.0.2.2:3000/" 

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ZaloPayApi = retrofit.create(ZaloPayApi::class.java)
}
