package com.muatrenthenang.resfood.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.muatrenthenang.resfood.data.model.Chat
import com.muatrenthenang.resfood.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatsRef = db.collection("chats")

    // Get all chats (For Admin) - Ordered by last message time
    fun getAllChatsFlow(): Flow<List<Chat>> = callbackFlow {
        val listener = chatsRef.orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    // Get specific chat (For Customer or Admin detail)
    // If chat doesn't exist (first time), it returns null, we might need to handle creation
    suspend fun getChatById(chatId: String): Result<Chat?> {
        return try {
            val doc = chatsRef.document(chatId).get().await()
            if (doc.exists()) {
                Result.success(doc.toObject(Chat::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get real-time messages for a specific chat
    fun getMessagesFlow(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatsRef.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    // Send a message
    suspend fun sendMessage(chatId: String, senderId: String, text: String, senderName: String? = null): Result<Boolean> {
        return try {
            val chatDocRef = chatsRef.document(chatId)
            val messageRef = chatDocRef.collection("messages").document()
            
            val timestamp = Timestamp.now()
            
            val message = ChatMessage(
                id = messageRef.id,
                senderId = senderId,
                text = text,
                timestamp = timestamp,
                isRead = false
            )

            db.runTransaction { transaction ->
                // 1. Check if chat exists, if not create it
                val chatSnapshot = transaction.get(chatDocRef)
                if (!chatSnapshot.exists()) {
                    val newChat = Chat(
                        id = chatId,
                        customerId = senderId, // Assuming first message usually from Customer, but if Admin starts logic handles it too
                        customerName = senderName ?: "Khách hàng",
                        lastMessage = text,
                        lastMessageSenderId = senderId,
                        lastMessageTime = timestamp,
                        unreadCountAdmin = 1,
                        unreadCountCustomer = 0
                    )
                    transaction.set(chatDocRef, newChat)
                } else {
                    // Update existing chat
                    // Determine who is sending to update unread counts
                    // If sender is NOT the customer (i.e., Admin), increment CustomerUnread
                    // If sender IS the customer, determine by ID match or logic
                    
                    // Simple logic: We need to know who is the "owner" (Customer) of the chat.
                    // The chatId IS the customerId in our design.
                    
                    val isCustomerSender = (senderId == chatId)
                    
                    val currentUnreadAdmin = chatSnapshot.getLong("unreadCountAdmin") ?: 0
                    val currentUnreadCustomer = chatSnapshot.getLong("unreadCountCustomer") ?: 0
                    
                    val updates = mutableMapOf<String, Any>(
                        "lastMessage" to text,
                        "lastMessageSenderId" to senderId,
                        "lastMessageTime" to timestamp
                    )
                    
                    if (isCustomerSender) {
                        updates["unreadCountAdmin"] = currentUnreadAdmin + 1
                        if (senderName != null) {
                             updates["customerName"] = senderName // Update name only if sent by customer
                        }
                    } else {
                        updates["unreadCountCustomer"] = currentUnreadCustomer + 1
                        // Do NOT update customerName if Admin sends message
                    }
                    
                    transaction.update(chatDocRef, updates)
                }
                
                // 2. Add message
                transaction.set(messageRef, message)
            }.await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mark messages as read
    suspend fun markAsRead(chatId: String, isReaderAdmin: Boolean): Result<Boolean> {
        return try {
            val chatDocRef = chatsRef.document(chatId)
            val fieldToReset = if (isReaderAdmin) "unreadCountAdmin" else "unreadCountCustomer"
            
            chatDocRef.update(fieldToReset, 0).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
