package com.muatrenthenang.resfood.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.data.repository.PromotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class VoucherViewModel : ViewModel() {

    private val repository = PromotionRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // Expose userId để UI dùng tính số lượng còn lại
    val currentUserId: String? get() = auth.currentUser?.uid

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPromotions()
    }

    fun loadPromotions() {
        val userId = currentUserId
        if (userId == null) {
            _error.value = "Bạn cần đăng nhập để xem mã giảm giá"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getPromotionsForUser(userId)
                .onSuccess { promos ->
                    _promotions.value = promos
                    Log.d("VoucherViewModel", "Loaded ${promos.size} promotions")
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Lỗi tải mã giảm giá"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
