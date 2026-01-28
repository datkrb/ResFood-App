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

    init {
        // If user is admin, they might want to see list. If customer, they only care about their own chat.
        // We can expose a function to load what is needed.
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
                 
                 // Auto-fix: If name is "Admin", it's likely wrong (overwritten). Fetch real name.
                 if (chat != null && (chat.customerName == "Admin" || chat.customerName == "Khách hàng")) {
                     val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                     db.collection("users").document(chat.customerId).get()
                        .addOnSuccessListener { doc ->
                            val realName = doc.getString("fullName")
                            if (!realName.isNullOrEmpty() && realName != chat.customerName) {
                                // Update DB
                                db.collection("chats").document(chatId).update("customerName", realName)
                                // Update UI
                                _currentChat.value = chat.copy(customerName = realName)
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
        }
    }
    
    // Setup for Customer: Customer's chat ID is their own User ID
    fun initCustomerChat() {
        if (currentUserId.isNotEmpty()) {
            loadMessages(currentUserId)
        }
    }
}
