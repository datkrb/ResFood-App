package com.muatrenthenang.resfood.data.model

import com.muatrenthenang.resfood.data.model.Food

/**
 * Model cho mặt hàng trong giỏ hàng
 */
data class CartItem(
    val id: String = "", // Document ID from Firestore
    val food: Food = Food(),
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val note: String? = null,
    val toppings: List<Topping> = emptyList() // Selected toppings
)
