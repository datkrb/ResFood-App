package com.muatrenthenang.resfood.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Models ---
data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String,
    val address: NominatimAddress?
)

data class NominatimAddress(
    val road: String?,
    val suburb: String?, // Ward
    val quarter: String?, // Ward alternative
    val city_district: String?, // District
    val county: String?, // District alternative
    val city: String?,
    val state: String?, // City alternative
    val country: String?
)

// --- API Interface ---
interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 1,
        @Header("User-Agent") userAgent: String = "ResFood-App"
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Header("User-Agent") userAgent: String = "ResFood-App"
    ): NominatimResult
}

// --- Singleton Service ---
object NominatimService {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    // Interceptor to add User-Agent (Required by Nominatim usage policy)
    private val userAgentInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("User-Agent", "ResFood-App (your_email@example.com)") // Replace with valid contact if publishing
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: NominatimApi = retrofit.create(NominatimApi::class.java)
}
