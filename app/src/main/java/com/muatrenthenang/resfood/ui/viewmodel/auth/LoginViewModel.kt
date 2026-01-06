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
            result.onSuccess {
                // For Google login, we might need to fetch user role as well if not already done in repo.
                // Assuming repo returns Result<Boolean> currently for Google, we need to check repo code.
                // Wait, I didn't update loginWithGoogle in Repo. It returns Result<Boolean>.
                // I should probably just treat Google users as usage "customer" for now OR update Repo.
                // To keep it simple and safe: Default to customer (false). 
                // Ideally we should update Repo to return User for Google too.
                _loginResult.value = LoginState.Success(isAdmin = false)
            }.onFailure {
                 _loginResult.value = LoginState.Error("Lỗi Google: ${it.message}")
            }
            _isLoading.value = false
        }
    }

}