package com.muatrenthenang.resfood.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OrsClient {
    private const val BASE_URL = "https://api.openrouteservice.org/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OpenRouteServiceApi by lazy {
        retrofit.create(OpenRouteServiceApi::class.java)
    }
}
