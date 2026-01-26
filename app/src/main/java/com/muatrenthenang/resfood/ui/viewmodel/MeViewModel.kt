package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel cho màn hình "Tôi" (Me/Profile Screen)
 */
class MeViewModel : ViewModel() {

    // User profile state
    private val _userProfile = MutableStateFlow(MeUserProfile())
    val userProfile: StateFlow<MeUserProfile> = _userProfile.asStateFlow()

    // Order status counts
    private val _orderCounts = MutableStateFlow(MeOrderCounts())
    val orderCounts: StateFlow<MeOrderCounts> = _orderCounts.asStateFlow()

    // Available vouchers count
    private val _voucherCount = MutableStateFlow(3)
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
        loadMockData()
    }

    private fun loadMockData() {
        // Mock user profile data
        _userProfile.value = MeUserProfile(
            name = "Nguyễn Văn A",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDHDUMg9VVxaTuv-Z6vsuz5-PC0at9LC8AlUk7Uji0GS6M5jdyhMvNNDyL4nF0ldUB6U0lW9_oMYvAOSBEdIfUsP2to47IfoJWH_7hCtPmNjT6tp0vC1gKbPnWquxbdk9FAJHWgF62CIShe-W-MYMTEeXRfk6q_iyWWLHfnS-PELhd8FecroGWpzLJewTSPOOjagXZWYfGxskbHYvI2Wm0aW9GGZQ8HRTFOJLtv86UBA8BMiTX130G9nGqcyI2JAlwPLaacP0z-a_M",
            rank = "Gold",
            rankDisplayName = "Thành viên Vàng (Gold)"
        )

        // Mock order counts
        _orderCounts.value = MeOrderCounts(
            pending = 0,
            processing = 0,
            delivering = 1,
            toReview = 0
        )

        // Mock Utility Menu
        // Note: In a real app we might update "subtitle" for vouchers dynamically
        updateUtilityMenu(voucherCount = 3)
    }

    private fun updateUtilityMenu(voucherCount: Int) {
        _utilityMenu.value = listOf(
            UtilityMenuOption(
                id = "vouchers",
                title = "Mã giảm giá của tôi",
                subtitle = "Bạn có $voucherCount mã khả dụng",
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
