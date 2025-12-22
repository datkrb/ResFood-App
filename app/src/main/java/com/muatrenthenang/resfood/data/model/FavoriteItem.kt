package com.muatrenthenang.resfood.data.model

data class FavoriteItem(
    val food: Food,
    val addedAt: Long = System.currentTimeMillis()
)
