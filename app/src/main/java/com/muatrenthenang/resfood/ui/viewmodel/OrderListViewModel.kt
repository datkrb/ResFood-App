package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.data.repository.PromotionRepository

class OrderListViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val promotionRepository: PromotionRepository = PromotionRepository()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private var allOrders: List<Order> = emptyList()

    fun loadOrders(status: String) {

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                try {
                    orderRepository.getOrdersByUserId(userId).collect { fetchedOrders ->
                        allOrders = fetchedOrders
                        filterOrders(status)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                _orders.value = emptyList()
            }
        }
    }

    private fun filterOrders(status: String) {
        _orders.value = when (status.lowercase()) {
            "all" -> allOrders
            "waiting_payment" -> allOrders.filter { it.status == "WAITING_PAYMENT" }
            "pending" -> allOrders.filter { it.status == "PENDING" }
            "processing" -> allOrders.filter { it.status == "PROCESSING" }
            "delivering" -> allOrders.filter { it.status == "DELIVERING" }
            "completed" -> allOrders.filter { it.status == "COMPLETED" }
            "cancelled" -> allOrders.filter { it.status == "CANCELLED" || it.status == "REJECTED" }
            "review" -> allOrders.filter { it.status == "COMPLETED" && !it.isReviewed } 
            "history" -> allOrders.filter { it.status == "COMPLETED" || it.status == "CANCELLED" || it.status == "REJECTED" }
            else -> allOrders
        }
    }

    fun getOrder(orderId: String): Order? {
        return allOrders.find { it.id == orderId }
    }

    fun callRestaurant(orderId: String) {
        // Logic to make a call
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                // Get order details first to know about vouchers
                val order = allOrders.find { it.id == orderId }
                
                // Update status to CANCELLED in Firestore
                val result = orderRepository.updateOrderStatus(orderId, "CANCELLED")
                
                if (result.isSuccess) {
                    // If cancellation successful, refund vouchers if any
                    order?.let { ord ->
                        val userId = ord.userId
                        
                         // Refund Product Voucher
                        if (!ord.productVoucherId.isNullOrEmpty()) {
                            promotionRepository.restoreVoucher(ord.productVoucherId, userId)
                        }
                        
                        // Refund Shipping Voucher
                        if (!ord.shippingVoucherId.isNullOrEmpty()) {
                            promotionRepository.restoreVoucher(ord.shippingVoucherId, userId)
                        }
                    }
                }

                // Refresh local list happens via Flow automatically
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reviewOrder(orderId: String) {
        // Logic to review
    }
    
    fun reOrder(orderId: String) {
        // Logic to reorder
    }
}

