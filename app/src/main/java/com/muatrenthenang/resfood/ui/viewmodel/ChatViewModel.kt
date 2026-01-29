package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.model.Chat
import com.muatrenthenang.resfood.data.model.ChatMessage
import com.muatrenthenang.resfood.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    // For Admin: List of all chats
    private val _allChats = MutableStateFlow<List<Chat>>(emptyList())
    val allChats: StateFlow<List<Chat>> = _allChats.asStateFlow()

    // For Detail Screen: Messages of current chat
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // Current active chat ID & Object
    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()
    
    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat.asStateFlow()

    // Total unread count for Badge (Global)
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    init {
        // If user is admin, they might want to see list. If customer, they only care about their own chat.
        // We can expose a function to load what is needed.
    }
    
    // Call this from AppLayout or MainViewModel to start monitoring unread count
    fun startUnreadCountMonitor(isAdmin: Boolean, userId: String) {
        viewModelScope.launch {
            if (isAdmin) {
                // For Admin: Monitor ALL chats to sum up unreadCountAdmin
                repository.getAllChatsFlow().collect { chats ->
                    val total = chats.sumOf { it.unreadCountAdmin }
                    _totalUnreadCount.value = total
                    _allChats.value = chats
                }
            } else {
                // For Customer: Monitor THEIR chat to get unreadCountCustomer
                // We listen to the specific chat document
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("chats").document(userId)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val chat = snapshot.toObject(Chat::class.java)
                            _totalUnreadCount.value = chat?.unreadCountCustomer ?: 0
                        } else {
                            _totalUnreadCount.value = 0
                        }
                    }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
            // If admin, the list stream will auto-update.
        }
    }

    fun loadAllChatsForAdmin() {
        viewModelScope.launch {
            repository.getAllChatsFlow().collect {
                _allChats.value = it
            }
        }
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        viewModelScope.launch {
            // Load messages
            repository.getMessagesFlow(chatId).collect {
                _messages.value = it
            }
        }
        
        // Also load chat details to get name
        viewModelScope.launch {
             repository.getChatById(chatId).onSuccess { chat ->
                 _currentChat.value = chat
                 
                 // Sync Name and Avatar from User Profile
                 if (chat != null) {
                     val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                     db.collection("users").document(chat.customerId).get()
                        .addOnSuccessListener { doc ->
                            val realName = doc.getString("fullName")
                            val realAvatar = doc.getString("avatarUrl") ?: ""
                            
                            val updates = mutableMapOf<String, Any>()
                            var updatedChat = chat

                            if (!realName.isNullOrEmpty() && realName != chat.customerName) {
                                updates["customerName"] = realName
                                updatedChat = updatedChat.copy(customerName = realName)
                            }
                            
                            // Sync Avatar if it's new or changed
                            if (realAvatar.isNotEmpty() && realAvatar != chat.customerAvatarUrl) {
                                updates["customerAvatarUrl"] = realAvatar
                                updatedChat = updatedChat.copy(customerAvatarUrl = realAvatar)
                            }

                            if (updates.isNotEmpty()) {
                                db.collection("chats").document(chatId).update(updates)
                                _currentChat.value = updatedChat
                            }
                        }
                 }
             }
        }
    }
    
    fun sendMessage(text: String, senderName: String? = null) {
        val chatId = _currentChatId.value ?: return
        if (text.isBlank()) return
        
        viewModelScope.launch {
            repository.sendMessage(chatId, currentUserId, text, senderName)
            // No need to manually update list, Flow will handle it
        }
    }
    
    // Call this when entering the chat screen
    fun markAsRead(chatId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            repository.markAsRead(chatId, isAdmin)
            // Currently viewing, so we might want to locally reset count too if stream is slow
        }
    }
    
    // Setup for Customer: Customer's chat ID is their own User ID
    fun initCustomerChat() {
        if (currentUserId.isNotEmpty()) {
            loadMessages(currentUserId)
        }
    }
}
