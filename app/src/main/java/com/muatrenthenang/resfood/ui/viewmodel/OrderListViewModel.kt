package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.repository.BranchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.data.repository.PromotionRepository

class OrderListViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val promotionRepository: PromotionRepository = PromotionRepository(),
    private val branchRepository: BranchRepository = BranchRepository()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    private val _branchPhone = MutableStateFlow<String?>(null)
    val branchPhone: StateFlow<String?> = _branchPhone.asStateFlow()

    init {
        loadBranchPhone()
    }

    private fun loadBranchPhone() {
        viewModelScope.launch {
            branchRepository.getPrimaryBranch().onSuccess { branch ->
                _branchPhone.value = branch.phone
            }
        }
    }

    fun loadOrders(status: String) {

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                try {
                    orderRepository.getOrdersByUserId(userId).collect { fetchedOrders ->
                        _allOrders.value = fetchedOrders
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
        val all = _allOrders.value
        _orders.value = when (status.lowercase()) {
            "all" -> all
            "waiting_payment" -> all.filter { it.status == "WAITING_PAYMENT" }
            "pending" -> all.filter { it.status == "PENDING" }
            "processing" -> all.filter { it.status == "PROCESSING" }
            "delivering" -> all.filter { it.status == "DELIVERING" }
            "completed" -> all.filter { it.status == "COMPLETED" }
            "cancelled" -> all.filter { it.status == "CANCELLED" || it.status == "REJECTED" }
            "review" -> all.filter { it.status == "COMPLETED" && !it.isReviewed } 
            "history" -> all.filter { it.status == "COMPLETED" || it.status == "CANCELLED" || it.status == "REJECTED" }
            else -> all
        }
    }

    fun getOrder(orderId: String): Order? {
        return _allOrders.value.find { it.id == orderId }
    }

    fun getBranchPhone(): String {
        return _branchPhone.value ?: "0123456789"
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                // Get order details first to know about vouchers
                val order = _allOrders.value.find { it.id == orderId }
                
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

    fun markOrderAsReviewed(orderId: String) {
        viewModelScope.launch {
            try {
                orderRepository.markOrderAsReviewed(orderId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

