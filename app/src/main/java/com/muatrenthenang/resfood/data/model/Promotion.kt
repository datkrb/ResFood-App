package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp

data class Promotion(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val discountType: Int = 0, // 0: %, 1: Amount
    val discountValue: Int = 0,
    val minOrderValue: Int = 0,
    val maxDiscountValue: Int = 0,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val applyFor: String = "ALL" // ALL, FOOD_ID, SHIP
)
