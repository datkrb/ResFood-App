package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.muatrenthenang.resfood.data.repository.PromotionRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình "Tôi" (Me/Profile Screen)
 */
class MeViewModel : ViewModel() {

    private val promotionRepository = PromotionRepository()
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    // User profile state
    private val _userProfile = MutableStateFlow(MeUserProfile())
    val userProfile: StateFlow<MeUserProfile> = _userProfile.asStateFlow()

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
        loadMockUserProfile()
        loadVoucherCount()
        loadOrderCounts()
    }

    private fun loadMockUserProfile() {
        // Mock user profile data
        _userProfile.value = MeUserProfile(
            name = "Nguyễn Văn A",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDHDUMg9VVxaTuv-Z6vsuz5-PC0at9LC8AlUk7Uji0GS6M5jdyhMvNNDyL4nF0ldUB6U0lW9_oMYvAOSBEdIfUsP2to47IfoJWH_7hCtPmNjT6tp0vC1gKbPnWquxbdk9FAJHWgF62CIShe-W-MYMTEeXRfk6q_iyWWLHfnS-PELhd8FecroGWpzLJewTSPOOjagXZWYfGxskbHYvI2Wm0aW9GGZQ8HRTFOJLtv86UBA8BMiTX130G9nGqcyI2JAlwPLaacP0z-a_M",
            rank = "Gold",
            rankDisplayName = "Thành viên Vàng (Gold)"
        )
        // Initial utility menu with 0 vouchers
        updateUtilityMenu(voucherCount = 0)
    }

    private fun loadOrderCounts() {
        val userId = auth.currentUser?.uid ?: return
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
    
    /**
     * Load số lượng voucher thật từ Firestore
     */
    private fun loadVoucherCount() {
        val userId = auth.currentUser?.uid ?: return
        
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
}

/**
 * Data class chứa thông tin người dùng cho màn hình "Tôi"
 */
data class MeUserProfile(
    val name: String = "",
    val avatarUrl: String = "",
    val rank: String = "",
    val rankDisplayName: String = ""
)

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
}
