package com.muatrenthenang.resfood.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()

    // State chứa thông tin User. Null nghĩa là chưa tải xong hoặc chưa đăng nhập.
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("resfood_prefs", Context.MODE_PRIVATE)

    // State quản lý chế độ tối
    // Đọc từ SharedPreferences, mặc định là true nếu chưa có cài đặt (hoặc false tùy ý)
    private val _isDarkTheme = MutableStateFlow(sharedPreferences.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

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

    // Hàm chuyển đổi theme
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        // Lưu vào SharedPreferences
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }
}