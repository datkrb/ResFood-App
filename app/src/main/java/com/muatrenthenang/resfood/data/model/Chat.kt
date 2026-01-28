package com.muatrenthenang.resfood.data.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "", // Typically the customerId
    val customerId: String = "",
    val customerName: String = "",
    val customerAvatarUrl: String = "", // Optional
    val lastMessage: String = "",
    val lastMessageSenderId: String = "", // To know if "You: " prefix is needed
    val lastMessageTime: Timestamp = Timestamp.now(),
    val unreadCountAdmin: Int = 0, // Number of messages admin hasn't read
    val unreadCountCustomer: Int = 0 // Number of messages customer hasn't read
)
