package com.muatrenthenang.resfood.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
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
                val result = authRepository.getUserProfile(userId)
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

    // Hàm cập nhật thông tin user
    suspend fun updateUser(fullName: String, phone: String?, email: String): Result<Boolean> {
        return try {
            val userId = authRepository.getCurrentUserId() ?: throw Exception("Chưa đăng nhập")
            val result = authRepository.updateUser(userId, fullName, phone, email)
            if (result.isSuccess) {
                // Refresh user data sau khi cập nhật
                fetchUserProfile()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm upload avatar
    suspend fun uploadAvatar(imageUri: Uri, context: android.content.Context): Result<String> {
        return try {
            val result = authRepository.uploadAvatar(imageUri, context)
            if (result.isSuccess) {
                // Refresh user data để lấy avatarUrl mới
                fetchUserProfile()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Hàm chuyển đổi theme
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        // Lưu vào SharedPreferences
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }
    
    // Hàm đổi mật khẩu
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return authRepository.changePassword(currentPassword, newPassword)
    }
}