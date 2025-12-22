package com.muatrenthenang.resfood.data.model

import com.muatrenthenang.resfood.data.model.Food

/**
 * Model cho mặt hàng trong giỏ hàng, chỉ cần Food và quantity
 */
data class CartItem(
    val food: Food,           // Thông tin món ăn
    val quantity: Int = 1,    // Số lượng đặt
    val note: String? = null  // Ghi chú của khách hàng (nếu có)
)
