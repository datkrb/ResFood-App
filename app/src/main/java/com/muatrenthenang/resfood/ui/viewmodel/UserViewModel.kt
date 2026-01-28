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


import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.repository.PromotionRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val promotionRepository = PromotionRepository()
    private val orderRepository = OrderRepository()
    
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("resfood_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(sharedPreferences.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Push notification state
    private val _isPushNotificationEnabled = MutableStateFlow(sharedPreferences.getBoolean("push_notification_enabled", true))
    val isPushNotificationEnabled: StateFlow<Boolean> = _isPushNotificationEnabled.asStateFlow()

    // === MeViewModel States ===
    // Order status counts
    private val _orderCounts = MutableStateFlow(MeOrderCounts())
    val orderCounts: StateFlow<MeOrderCounts> = _orderCounts.asStateFlow()

    // Available vouchers count
    private val _voucherCount = MutableStateFlow(0)
    val voucherCount: StateFlow<Int> = _voucherCount.asStateFlow()

    // Referral Promo content
    private val _referralPromo = MutableStateFlow(
        ReferralPromoData(
            title = "Mời bạn bè, nhận quà!",
            subtitle = "Tặng voucher 50k cho mỗi lời mời thành công",
            buttonText = "Mời ngay"
        )
    )
    val referralPromo: StateFlow<ReferralPromoData> = _referralPromo.asStateFlow()

    // Utility Menu Items
    private val _utilityMenu = MutableStateFlow<List<UtilityMenuOption>>(emptyList())
    val utilityMenu: StateFlow<List<UtilityMenuOption>> = _utilityMenu.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserProfile()
        updateUtilityMenu(voucherCount = 0)
    }

    fun fetchUserProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // First, ensure total spending is consistent by recalculating
                    // This acts as a self-healing mechanism every time profile is loaded
                    orderRepository.recalculateUserSpending(userId)
                    
                    // Then fetch the updated profile
                    val result = authRepository.getUserProfile(userId)
                    result.onSuccess { user ->
                        _userState.value = user
                    }
                    result.onFailure {
                        // Handle error if needed
                    }

                    loadOrderCounts(userId)
                    loadVoucherCount(userId)
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
             _userState.value = null
        }
    }

    private fun loadOrderCounts(userId: String) {
        viewModelScope.launch {
            try {
                orderRepository.getOrdersByUserId(userId).collect { orders ->
                    _orderCounts.value = MeOrderCounts(
                        pending = orders.count { it.status == "PENDING" },
                        processing = orders.count { it.status == "PROCESSING" },
                        delivering = orders.count { it.status == "DELIVERING" },
                        toReview = orders.count { it.status == "COMPLETED" }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadVoucherCount(userId: String) {
        viewModelScope.launch {
            promotionRepository.getPromotionsForUser(userId)
                .onSuccess { promos ->
                    updateUtilityMenu(voucherCount = promos.size)
                }
                .onFailure {
                    // Keep 0 if failed
                }
        }
    }

    private fun updateUtilityMenu(voucherCount: Int) {
        _utilityMenu.value = listOf(
            UtilityMenuOption(
                id = "membership",
                title = "Hạng thành viên",
                subtitle = "Xem ưu đãi & tích điểm",
                iconType = UtilityIconType.RANK
            ),
            UtilityMenuOption(
                id = "spending_statistics",
                title = "Thống kê chi tiêu",
                subtitle = "Xem chi tiêu theo danh mục",
                iconType = UtilityIconType.STATISTICS
            ),
            UtilityMenuOption(
                id = "vouchers",
                title = "Mã giảm giá của tôi",
                subtitle = if (voucherCount > 0) "Bạn có $voucherCount mã khả dụng" else "Chưa có mã nào",
                iconType = UtilityIconType.VOUCHER
            ),
            UtilityMenuOption(
                id = "addresses",
                title = "Địa chỉ đã lưu",
                subtitle = "Nhà riêng, Văn phòng...",
                iconType = UtilityIconType.ADDRESS
            ),
            UtilityMenuOption(
                id = "help",
                title = "Trung tâm trợ giúp",
                subtitle = "Giải đáp thắc mắc 24/7",
                iconType = UtilityIconType.HELP
            ),
            UtilityMenuOption(
                id = "payment",
                title = "Phương thức thanh toán",
                subtitle = "Visa, MoMo, ZaloPay",
                iconType = UtilityIconType.PAYMENT
            )
        )
        _voucherCount.value = voucherCount
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _userState.value = null
            _orderCounts.value = MeOrderCounts()
            updateUtilityMenu(0)
            onLogoutSuccess()
        }
    }

    suspend fun updateUser(fullName: String, phone: String?, email: String): Result<Boolean> {
        return try {
            val userId = authRepository.getCurrentUserId() ?: throw Exception("Chưa đăng nhập")
            val result = authRepository.updateUser(userId, fullName, phone, email)
            if (result.isSuccess) {
                fetchUserProfile()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(imageUri: Uri, context: android.content.Context): Result<String> {
        return try {
            val result = authRepository.uploadAvatar(imageUri, context)
            if (result.isSuccess) {
                fetchUserProfile()
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return authRepository.changePassword(currentPassword, newPassword)
    }
    
    fun toggleTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun togglePushNotification(enabled: Boolean) {
        _isPushNotificationEnabled.value = enabled
        sharedPreferences.edit().putBoolean("push_notification_enabled", enabled).apply()
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.deleteAccount()
            _isLoading.value = false
            
            result.onSuccess {
                _userState.value = null
                 _orderCounts.value = MeOrderCounts()
                updateUtilityMenu(0)
                onSuccess()
            }
            result.onFailure { e ->
                val errorMsg = if (e is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    "Để bảo mật, vui lòng đăng xuất và đăng nhập lại trước khi xóa tài khoản."
                } else {
                    e.message ?: "Xóa tài khoản thất bại"
                }
                onError(errorMsg)
            }
        }
    }
}

/**
 * Data class chứa số lượng đơn hàng theo trạng thái
 */
data class MeOrderCounts(
    val pending: Int = 0,
    val processing: Int = 0,
    val delivering: Int = 0,
    val toReview: Int = 0
)

data class ReferralPromoData(
    val title: String,
    val subtitle: String,
    val buttonText: String
)

data class UtilityMenuOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconType: UtilityIconType
)

enum class UtilityIconType {
    VOUCHER, ADDRESS, HELP, PAYMENT, RANK, STATISTICS
}