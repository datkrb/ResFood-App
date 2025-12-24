package com.muatrenthenang.resfood.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        _isLoggedIn.value = repository.getCurrentUser() != null
    }
}