package com.muatrenthenang.resfood.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "", // Recipient
    val title: String = "",
    val body: String = "",
    val type: String = "info", // "order_update", "promo", "system"
    val referenceId: String = "", // Order ID, Promo ID, etc.
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
