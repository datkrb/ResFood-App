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
    // Using FirebaseAuth directly for simple ID check if needed, or rely on AuthRepository
    // AuthRepository likely has getCurrentUserId, let's use that.

    // State chứa thông tin User. Null nghĩa là chưa tải xong hoặc chưa đăng nhập.
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("resfood_prefs", Context.MODE_PRIVATE)

    // State quản lý chế độ tối
    private val _isDarkTheme = MutableStateFlow(sharedPreferences.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

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

    init {
        fetchUserProfile()
        // Initialize utility menu with 0 vouchers first
        updateUtilityMenu(voucherCount = 0)
    }

    // Hàm tải dữ liệu user
    fun fetchUserProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                // Fetch User Details
                val result = authRepository.getUserDetails(userId)
                result.onSuccess { user ->
                    _userState.value = user
                }
                result.onFailure {
                    // Xử lý lỗi nếu cần
                }

                // Fetch Me Screen Data
                loadOrderCounts(userId)
                loadVoucherCount(userId)
            }
        } else {
             _userState.value = null
             // Reset other states if needed
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
                    // Giữ giá trị 0 nếu lỗi
                }
        }
    }

    private fun updateUtilityMenu(voucherCount: Int) {
        _utilityMenu.value = listOf(
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

    // Hàm đăng xuất
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _userState.value = null // Xóa dữ liệu user khỏi bộ nhớ
            // Reset other states
            _orderCounts.value = MeOrderCounts()
            updateUtilityMenu(0)
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
}

/**
 * Data class chứa số lượng đơn hàng theo trạng thái
 */
data class MeOrderCounts(
    val pending: Int = 0,      // Chờ xác nhận
    val processing: Int = 0,   // Đang chế biến
    val delivering: Int = 0,   // Đang giao
    val toReview: Int = 0      // Chờ đánh giá
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
    VOUCHER, ADDRESS, HELP, PAYMENT
    
    // Hàm đổi mật khẩu
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return authRepository.changePassword(currentPassword, newPassword)
    }
}