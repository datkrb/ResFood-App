package com.muatrenthenang.resfood.ui.viewmodel.auth

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

    private val _loginResult =
        MutableStateFlow<LoginState?>(null) // null = chưa làm gì
    val loginResult = _loginResult.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginResult.value = LoginState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Hiện vòng xoay loading
            val result = repository.login(email, pass)

            result.onSuccess { user ->
                val isAdmin = user.role == "admin"
                _loginResult.value = LoginState.Success(isAdmin)
            }.onFailure {
                _loginResult.value = LoginState.Error("Đăng nhập thất bại: ${it.message}")
            }
            _isLoading.value = false // Tắt vòng xoay
        }
    }

    // Hàm reset trạng thái khi rời màn hình
    fun resetLoginState() {
        _loginResult.value = null
    }

    sealed class LoginState {
        data class Success(val isAdmin: Boolean) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess { user ->
                val isAdmin = user.role == "admin"
                _loginResult.value = LoginState.Success(isAdmin)
            }.onFailure {
                 _loginResult.value = LoginState.Error("Lỗi Google: ${it.message}")
            }
            _isLoading.value = false
        }
    }

}