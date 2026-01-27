package com.muatrenthenang.resfood.data.model

import com.google.firebase.firestore.PropertyName

data class Branch(
    var id: String = "",
    var name: String = "",
    var address: Address = Address(),
    @get:PropertyName("food_ids") @set:PropertyName("food_ids") var foodIds: List<String> = emptyList(),
    @get:PropertyName("table_count") @set:PropertyName("table_count") var tableCount: Int = 0,
    @get:PropertyName("max_capacity") @set:PropertyName("max_capacity") var maxCapacity: Int = 0,

    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String? = null
)
