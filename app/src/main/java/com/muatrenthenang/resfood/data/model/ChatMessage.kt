package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
