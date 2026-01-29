package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.muatrenthenang.resfood.data.repository.BranchRepository
import com.muatrenthenang.resfood.util.CurrencyHelper

class CartViewModel(
    private val _repository: CartRepository = CartRepository(),
    private val _branchRepository: BranchRepository = BranchRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val _needLogin = MutableStateFlow(false)
    val needLogin = _needLogin.asStateFlow()

    // Dynamic shipping fee loaded from Branch
    private val _shippingFee = MutableStateFlow(15000L)
    val shippingFee = _shippingFee.asStateFlow()

    init {
        _isLoading.value = true
        loadCart()
        loadShippingFee()
    }

    private fun loadShippingFee() {
        viewModelScope.launch {
            _branchRepository.getPrimaryBranch().onSuccess { branch ->
                _shippingFee.value = branch.shippingFee
            }
        }
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
            _isLoading.value = false
        }
    }

    fun changeQuantity(cartItemId: String, delta: Int) {
        val item = _items.value.firstOrNull { it.id == cartItemId } ?: return
        val newQty = (item.quantity + delta).coerceAtLeast(1)

        _items.value = _items.value.map {
            if (it.id == cartItemId) it.copy(quantity = newQty) else it
        }

        viewModelScope.launch {
            val result = _repository.addOrUpdateCartItem(
                foodId = item.food.id,
                quantity = newQty,
                note = item.note,
                isSelected = item.isSelected,
                toppings = item.toppings
            )
            if (!result.isSuccess) {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
                loadCart()
            }
        }
    }

    fun setItemSelected(cartItemId: String, selected: Boolean) {
        val item = _items.value.firstOrNull { it.id == cartItemId } ?: return

        // Optimistic update
        _items.value = _items.value.map {
            if (it.id == cartItemId) it.copy(isSelected = selected) else it
        }

        viewModelScope.launch {
            val result = _repository.addOrUpdateCartItem(
                foodId = item.food.id,
                quantity = item.quantity,
                note = item.note,
                isSelected = selected,
                toppings = item.toppings
            )
            if (!result.isSuccess) {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
                // Re-sync from backend on failure
                loadCart()
            }
        }
    }

    fun removeItem(cartItemId: String) {
        viewModelScope.launch {
            val result = _repository.removeCartItem(cartItemId)
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
        // Only include selected items in subtotal
        return _items.value.filter { it.isSelected }.sumOf { (it.food.price + it.toppings.sumOf { t -> t.price }) * it.quantity }
    }

    fun total(): Long {
        val subtotal = subTotal()
        return if (subtotal > 0) subtotal + _shippingFee.value else 0L
    }

    fun formatCurrency(value: Long): String {
        return CurrencyHelper.format(value)
    }

    fun canCheckout(): Boolean {
        val items = _items.value

        if (items.isEmpty()) {
            _actionResult.value = "Giỏ hàng trống"
            return false
        }

        if (!items.any { it.isSelected }) {
            _actionResult.value = "Vui lòng chọn ít nhất một món"
            return false
        }

        return true
    }

    fun clearResult() { _actionResult.value = null }
}

