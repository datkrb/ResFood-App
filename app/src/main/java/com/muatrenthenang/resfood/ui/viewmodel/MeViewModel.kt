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
