package com.muatrenthenang.resfood.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                val result = repository.getUserProfile(currentUser.uid)
                result.onSuccess { user ->
                    val isAdmin = user.role == "admin"
                    _authState.value = AuthState.Authenticated(isAdmin)
                }.onFailure {
                    // Fallback or retry? for now treat as guest/logic needed
                    // potentially could still be logged in but failed to fetch profile
                    // but safely, let's say unauthenticated or maybe Authenticated as default?
                    // Let's assume customer if fail to fetch for now to avoid block, or Unauth?
                    // Safest is Unauth or try validation.
                    // Let's log out if we can't get user details (integrity check)
                    repository.logout()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val isAdmin: Boolean) : AuthState()
        object Unauthenticated : AuthState()
    }
}