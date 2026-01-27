package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp

data class Reservation(
    val id: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val time: Timestamp? = null,
    val guestCount: Int = 2,
    val tableId: String = "", // Optional, table might be assigned later
    val tableName: String = "", // Denormalized for display
    val status: String = "PENDING", // PENDING, CONFIRMED, COMPLETED, CANCELLED
    val note: String = ""
)
