package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutSuccess()
        }
    }

    // Tạm thời mock data user để hiển thị lên giao diện
    val userName = "Nguyễn Văn A"
    val userRank = "Thành viên Vàng"
}