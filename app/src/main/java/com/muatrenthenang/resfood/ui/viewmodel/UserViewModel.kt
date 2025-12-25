package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // State chứa thông tin User. Null nghĩa là chưa tải xong hoặc chưa đăng nhập.
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    init {
        fetchUserProfile()
    }

    // Hàm tải dữ liệu user
    fun fetchUserProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                val result = authRepository.getUserDetails(userId)
                result.onSuccess { user ->
                    _userState.value = user
                }
                result.onFailure {
                    // Xử lý lỗi nếu cần
                }
            }
        }
    }

    // Hàm đăng xuất
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _userState.value = null // Xóa dữ liệu user khỏi bộ nhớ
            onLogoutSuccess()
        }
    }

    // State quản lý chế độ tối (Mặc định là true)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Hàm chuyển đổi theme
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}