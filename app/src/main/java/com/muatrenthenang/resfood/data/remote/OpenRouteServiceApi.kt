package com.muatrenthenang.resfood.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenRouteServiceApi {
    @GET("v2/directions/driving-car")
    suspend fun getDirections(
        @Query("api_key") apiKey: String,
        @Query("start") start: String, // "lng,lat"
        @Query("end") end: String      // "lng,lat"
    ): Response<OrsResponse>
}

data class OrsResponse(
    @SerializedName("features") val features: List<OrsFeature>
)

data class OrsFeature(
    @SerializedName("properties") val properties: OrsProperties
)

data class OrsProperties(
    @SerializedName("summary") val summary: OrsSummary
)

data class OrsSummary(
    @SerializedName("distance") val distance: Double, // meters
    @SerializedName("duration") val duration: Double
)
