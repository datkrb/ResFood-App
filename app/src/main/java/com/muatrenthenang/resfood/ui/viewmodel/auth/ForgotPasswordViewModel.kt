package com.muatrenthenang.resfood.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _resetResult = MutableStateFlow<String?>(null)
    val resetResult = _resetResult.asStateFlow()

    fun sendResetEmail(email: String) {
        if (email.isBlank()) {
            _resetResult.value = "Vui lòng nhập Email"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _resetResult.value = "Success"
            } else {
                _resetResult.value = "Lỗi: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun resetState() {
        _resetResult.value = null
    }
}