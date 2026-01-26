package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReferralHistoryItem(
    val name: String,
    val date: String,
    val status: String,
    val reward: String
)

class ReferralViewModel : ViewModel() {

    private val _referralCode = MutableStateFlow("RESFOOD99")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()

    private val _referralHistory = MutableStateFlow<List<ReferralHistoryItem>>(emptyList())
    val referralHistory: StateFlow<List<ReferralHistoryItem>> = _referralHistory.asStateFlow()

    private val _totalInvites = MutableStateFlow(0)
    val totalInvites: StateFlow<Int> = _totalInvites.asStateFlow()

    private val _totalReward = MutableStateFlow("0đ")
    val totalReward: StateFlow<String> = _totalReward.asStateFlow()

    private val _referralSteps = MutableStateFlow<List<ReferralStep>>(emptyList())
    val referralSteps: StateFlow<List<ReferralStep>> = _referralSteps.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        // Mock Data
        val mockList = listOf(
            ReferralHistoryItem("Nguyễn Văn A", "20/05/2024", "Hoàn thành", "+50.000đ"),
            ReferralHistoryItem("Trần Thị B", "18/05/2024", "Đang chờ", "Chưa nhận"),
            ReferralHistoryItem("Lê Văn C", "15/05/2024", "Hoàn thành", "+50.000đ"),
            ReferralHistoryItem("Phạm Thị D", "10/05/2024", "Hoàn thành", "+50.000đ")
        )
        
        _referralHistory.value = mockList
        _totalInvites.value = mockList.size
        _totalReward.value = "150.000đ" // Mock sum based on "Hoàn thành" items

        _referralSteps.value = listOf(
            ReferralStep("1", "Mời bạn bè", "Gửi mã giới thiệu cá nhân cho người thân qua các ứng dụng mạng xã hội."),
            ReferralStep("2", "Bạn bè đặt đơn đầu tiên", "Người được giới thiệu nhập mã và hoàn tất đơn hàng đầu tiên trên ResFood."),
            ReferralStep("3", "Cả hai cùng nhận Voucher", "Hệ thống sẽ tự động gửi Voucher giảm giá 50.000đ vào ví của cả hai bạn.")
        )
    }
}

data class ReferralStep(
    val step: String,
    val title: String,
    val description: String
)
