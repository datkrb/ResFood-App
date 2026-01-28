package com.muatrenthenang.resfood.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ResFoodPaymentClient {
    // Assuming 10.0.2.2:3000 for Android Emulator to access localhost
    // Use your computer's IP if running on a real device
    private const val BASE_URL = com.muatrenthenang.resfood.BuildConfig.NGROK_URL

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ResFoodPaymentApi = retrofit.create(ResFoodPaymentApi::class.java)
}
