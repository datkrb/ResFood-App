package com.muatrenthenang.resfood.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface ResFoodPaymentApi {
    @POST("api/v1/payments/sepay/create")
    suspend fun createSepayPayment(
        @Body request: CreateSepayPaymentRequest
    ): SepayResponse
}

data class CreateSepayPaymentRequest(
    val orderId: String
)

data class SepayResponse(
    val transId: String,
    val qrUrl: String,
    val amount: Int,
    val description: String
)
