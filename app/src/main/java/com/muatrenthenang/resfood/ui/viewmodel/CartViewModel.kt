package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class CartViewModel(
    private val _repository: CartRepository = CartRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val _needLogin = MutableStateFlow(false)
    val needLogin = _needLogin.asStateFlow()

    private val _promoCode = MutableStateFlow<String?>(null)
    val promoCode = _promoCode.asStateFlow()

    private val _shippingFee = 15000L

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            val result = _repository.getCartItems()
            if (result.isSuccess) {
                _items.value = result.getOrNull() ?: emptyList()
                _needLogin.value = false
            } else {
                if (result.exceptionOrNull()?.message?.contains("chưa đăng nhập", true) == true) {
                    _needLogin.value = true
                }
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun addOrUpdateCartItem(foodId: String, quantity: Int, note: String? = null) {
        viewModelScope.launch {
            val result = _repository.addOrUpdateCartItem(foodId, quantity, note)
            if (result.isSuccess) {
                _actionResult.value = "Đã thêm/cập nhật giỏ hàng"
                loadCart()
            } else {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun changeQuantity(foodId: String, delta: Int) {
        val item = _items.value.firstOrNull { it.food.id == foodId }
        if (item != null) {
            val newQty = (item.quantity + delta).coerceAtLeast(1)
            addOrUpdateCartItem(foodId, newQty, item.note)
        }
    }

    fun removeItem(foodId: String) {
        viewModelScope.launch {
            val result = _repository.removeCartItem(foodId)
            if (result.isSuccess) {
                _actionResult.value = "Đã xóa mặt hàng khỏi giỏ hàng"
                loadCart()
            } else {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val result = _repository.clearCart()
            if (result.isSuccess) {
                _actionResult.value = "Đã xóa toàn bộ giỏ hàng"
                loadCart()
            } else {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun subTotal(): Int {
        return _items.value.sumOf { it.food.price * it.quantity }
    }

    fun discount(): Long {
        return if (_promoCode.value == "NNDAI") 10000L else 0L
    }

    fun total(): Long {
        val subtotal = subTotal()
        return subtotal + _shippingFee - discount()
    }

    fun formatCurrency(value: Long): String {
        val pattern = DecimalFormat("###,###")
        return pattern.format(value) + "đ"
    }

    fun checkout() {
        viewModelScope.launch {
            if (_items.value.isEmpty()) {
                _actionResult.value = "Giỏ hàng trống"
                return@launch
            }
            _isLoading.value = true
            kotlinx.coroutines.delay(1200)
            _isLoading.value = false
            _actionResult.value = "Checkout thành công"
            clearCart()
        }
    }

    fun applyPromo(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            if (code.trim().uppercase() == "NNDAI") {
                _promoCode.value = "NNDAI"
                _actionResult.value = "Mã khuyến mãi đã được áp dụng"
            } else {
                _actionResult.value = "Mã không hợp lệ"
                _promoCode.value = null
            }
        }
    }

    fun clearResult() { _actionResult.value = null }
}

