package com.muatrenthenang.resfood.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface ZaloPayApi {
    @POST("api/v1/payments/zalopay")
    suspend fun createPayment(
        @Body request: CreatePaymentRequest
    ): ZaloPayResponse

    @POST("api/v1/payments/zalopay/check-status")
    suspend fun checkStatus(
        @Body request: CheckStatusRequest
    ): CheckStatusResponse
}

data class CreatePaymentRequest(
    val order_id: String
)

data class ZaloPayResponse(
    val zp_trans_token: String?,
    val order_url: String?,
    val return_code: Int,
    val return_message: String?,
    val sub_return_code: Int,
    val sub_return_message: String?
)

data class CheckStatusRequest(
    val app_trans_id: String
)

data class CheckStatusResponse(
    val return_code: Int,
    val return_message: String,
    val is_processing: Boolean,
    val amount: Long,
    val discount_amount: Long,
    val zptransid: String
)
