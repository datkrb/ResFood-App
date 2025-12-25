package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    // Gọi lớp Repository (Nơi xử lý Firebase)
    private val repository = AuthRepository()

    // 1. Biến trạng thái Loading (để hiện vòng xoay khi đang đăng ký)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 2. Biến kết quả (để thông báo thành công hoặc lỗi cho màn hình)
    private val _registerResult = MutableStateFlow<String?>(null)
    val registerResult = _registerResult.asStateFlow()

    // Hàm xử lý logic Đăng ký
    fun register(email: String, pass: String, rePass: String, fullName: String) {
        // --- Bước 1: Kiểm tra dữ liệu nhập vào (Validate) ---
        if (fullName.isBlank()) {
            _registerResult.value = "Vui lòng nhập họ và tên"
            return
        }
        if (email.isBlank()) {
            _registerResult.value = "Vui lòng nhập Email"
            return
        }
        if (pass.isBlank()) {
            _registerResult.value = "Vui lòng nhập mật khẩu"
            return
        }
        if (pass.length < 6) {
            _registerResult.value = "Mật khẩu phải có ít nhất 6 ký tự"
            return
        }
        if (pass != rePass) {
            _registerResult.value = "Mật khẩu nhập lại không khớp"
            return
        }

        // --- Bước 2: Gọi Repository để đăng ký lên Firebase ---
        viewModelScope.launch {
            _isLoading.value = true // Bật loading

            val result = repository.register(email, pass, fullName)

            if (result.isSuccess) {
                _registerResult.value = "Đăng kí thành công, vui lòng kiểm tra email" // Báo thành công
            } else {
                // Lấy thông báo lỗi từ Firebase (ví dụ: Email đã tồn tại)
                _registerResult.value = result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
            }

            _isLoading.value = false // Tắt loading
        }
    }

    // Hàm reset trạng thái (để khi quay lại màn hình này không bị hiện lỗi cũ)
    fun resetState() {
        _registerResult.value = null
    }

    fun registerWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _registerResult.value = "Success"
            } else {
                _registerResult.value = "Lỗi Google: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }
}