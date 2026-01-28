package com.muatrenthenang.resfood.data.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Response model từ ImgBB
data class ImgBBResponse(
    val data: ImgBBData?,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String,
    val display_url: String,
    val delete_url: String?
)

// API Interface
interface ImgBBApi {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody? = null
    ): ImgBBResponse
}

// Singleton object để dùng chung
object ImgBBUploader {
    private val API_KEY = com.muatrenthenang.resfood.BuildConfig.IMGBB_API_KEY
    private const val BASE_URL = "https://api.imgbb.com/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: ImgBBApi = retrofit.create(ImgBBApi::class.java)
    
    fun getApiKey(): String = API_KEY
}
