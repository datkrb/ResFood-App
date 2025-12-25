package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository() // Gọi lớp data

    // Các biến trạng thái để UI lắng nghe
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<String?>(null) // null = chưa làm gì, "Success" = thành công, còn lại là lỗi
    val loginResult = _loginResult.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginResult.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Hiện vòng xoay loading
            val result = repository.login(email, pass)

            if (result.isSuccess) {
                _loginResult.value = "Success"
            } else {
                _loginResult.value = "Đăng nhập thất bại: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false // Tắt vòng xoay
        }
    }

    // Hàm reset trạng thái khi rời màn hình
    fun resetLoginState() {
        _loginResult.value = null
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _loginResult.value = "Success"
            } else {
                _loginResult.value = "Lỗi Google: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

}