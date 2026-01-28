package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Notification
import com.muatrenthenang.resfood.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        // Start listening immediately
        viewModelScope.launch {
            try {
                repository.getUserNotifications().collect {
                    _notifications.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getUnreadCount().collect {
                    _unreadCount.value = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
