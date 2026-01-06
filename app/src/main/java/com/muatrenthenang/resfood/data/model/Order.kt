package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val address: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Int = 0,
    val discount: Int = 0,
    val deliveryFee: Int = 0,
    val total: Int = 0,
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, CANCELLED, REJECTED
    val paymentMethod: String = "COD",
    val createdAt: Timestamp = Timestamp.now()
)

data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val foodImage: String? = null,
    val quantity: Int = 0,
    val price: Int = 0,
    val note: String? = null
)
