package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class TableReservation(
    var id: String = "",
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @get:PropertyName("branch_id") @set:PropertyName("branch_id") var branchId: String = "",
    @get:PropertyName("branch_name") @set:PropertyName("branch_name") var branchName: String = "",
    @get:PropertyName("time_slot") @set:PropertyName("time_slot") var timeSlot: Timestamp = Timestamp.now(),
    @get:PropertyName("guest_count_adult") @set:PropertyName("guest_count_adult") var guestCountAdult: Int = 0,
    @get:PropertyName("guest_count_child") @set:PropertyName("guest_count_child") var guestCountChild: Int = 0,
    var note: String = "",
    var status: String = "PENDING", // PENDING, CONFIRMED, CANCELLED
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp = Timestamp.now()
) {
    fun getTotalGuests(): Int {
        return guestCountAdult + guestCountChild
    }
}
