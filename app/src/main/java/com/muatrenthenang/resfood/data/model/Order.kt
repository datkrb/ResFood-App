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
    val discount: Int = 0, // Tổng giảm = productDiscount + shippingDiscount
    val deliveryFee: Int = 0,
    val total: Int = 0,
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, CANCELLED, REJECTED
    val paymentMethod: String = "COD",
    val createdAt: Timestamp = Timestamp.now(),
    
    // === VOUCHER FIELDS ===
    val productVoucherCode: String? = null,   // Mã voucher giảm giá sản phẩm
    val productVoucherId: String? = null,     // ID promotion
    val shippingVoucherCode: String? = null,  // Mã voucher giảm ship
    val shippingVoucherId: String? = null,    // ID promotion
    val productDiscount: Int = 0,             // Tiền giảm từ voucher sản phẩm
    val shippingDiscount: Int = 0             // Tiền giảm từ voucher ship
)

data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val foodImage: String? = null,
    val quantity: Int = 0,
    val price: Int = 0,
    val note: String? = null
)
