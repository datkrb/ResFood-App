package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.muatrenthenang.resfood.data.model.Promotion
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date

class VoucherViewModel : ViewModel() {

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30) // Expires in 30 days
        val endDate = Timestamp(calendar.time)

        val mockList = listOf(
            Promotion(
                id = "1",
                name = "Giảm 50k cho đơn từ 200k",
                code = "GIAM50K",
                discountType = 1, // Amount
                discountValue = 50000,
                minOrderValue = 200000,
                startDate = Timestamp.now(),
                endDate = endDate,
                isActive = true,
                applyFor = "SHIP"
            ),
            Promotion(
                id = "2",
                name = "Miễn phí vận chuyển",
                code = "FREESHIP",
                discountType = 1,
                discountValue = 15000,
                minOrderValue = 0,
                startDate = Timestamp.now(),
                endDate = endDate,
                isActive = true,
                applyFor = "SHIP"
            ),
            Promotion(
                id = "3",
                name = "Giảm 20% toàn hệ thống",
                code = "ALL20",
                discountType = 0, // Percent
                discountValue = 20,
                minOrderValue = 100000,
                maxDiscountValue = 50000,
                startDate = Timestamp.now(),
                endDate = endDate,
                isActive = true,
                applyFor = "ALL"
            ),
             Promotion(
                id = "4",
                name = "Giảm 15% tại quán Coffee House",
                code = "COFFEE15",
                discountType = 0, // Percent
                discountValue = 15,
                minOrderValue = 50000,
                maxDiscountValue = 30000,
                startDate = Timestamp.now(),
                endDate = endDate,
                isActive = true,
                applyFor = "FOOD_ID" 
            )
        )
        _promotions.value = mockList
    }

}
