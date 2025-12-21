package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// Mô tả đơn giản cho mặt hàng trong giỏ
data class CartItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val price: Long,
    val maxQuantity: Int = 99,
    val note: String? = null,
    val quantity: Int = 1
)

class CartViewModel : ViewModel() {
    // Danh sách mặt hàng trong giỏ
    private val _items = MutableStateFlow<List<CartItem>>(listOf(
        CartItem(
            id = "pho_bo",
            name = "Phở Bò Đặc Biệt",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDxCE3x8HloIr3YvbuS5Gpo-BQDVhq2KbDaW1iFEcZvkZtn-JgCotf9IaIFuBR9uqINAEYlQ7daUg-qptgZExZfpHV79JiX5emfjj5QDgIyf_TSSckq8xKK1IbNXQPkAQgHgzy5P6x4RihuhH0Qb_MF_uxybTe4C67FNARNo6cw8EUME8X3eYHQhUUlFYY82gFC4cnZWA19wWoK6sV3SX5IM2Y1iXEP4w83-Iv7bVeCWbwk88MKfwD7-LT1PrjcOlIg97Ub5bjekVI",
            maxQuantity = 20,
            price = 65000L,
            quantity = 1
        ),
        CartItem(
            id = "tra_dao",
            name = "Trà Đào Cam Sả",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAOZzHiENi-TX4NbTYpedEk47PMGeSWbDGfJMTw2AqbyPAwtsLzKjJrO7FdYL85gcPMHatwBFgMLY0DIwgkvwZIiYFnM-pkVMEOFjuk4jBx5zsCgepWMTXkcBfHkUvn7p-NA6-kL-yzbes59PcAPlnkQCDQDYcXOoeeM0BR_dxGx5GLuC7QGAwFhJf2cCFiArY-Ci0Lp0FgWs2dVpNrccvNEU3ihHmcMgKdV2aKDQPtwNmypWTcT0pA0AWqcNuB_9E_WjZVqWeMiio",
            maxQuantity = 20,
            price = 45000L,
            quantity = 2
        )
    ))
    val items = _items.asStateFlow()

    // Loading state khi thao tác
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Kết quả thao tác chung
    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    // Mã khuyến mãi hiện áp dụng
    private val _promoCode = MutableStateFlow<String?>(null)
    val promoCode = _promoCode.asStateFlow()

    // Phí vận chuyển mặc định
    private val shippingFee = 15000L

    // Áp dụng mã khuyến mãi đơn giản
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

    // Thay đổi số lượng cho một item
    fun changeQuantity(itemId: String, delta: Int) {
        _items.update { list ->
            list.map { it ->
                if (it.id == itemId) {
                    val newQty = (it.quantity + delta).coerceAtLeast(1)
                    if (newQty <= it.maxQuantity){
                        it.copy(quantity = newQty)
                    } else {
                        _actionResult.value = "Số lượng tối đa là ${it.maxQuantity}"
                        it
                    }
                } else it
            }
        }
    }

    // Xóa 1 item
    fun removeItem(itemId: String) {
        _items.update { list -> list.filterNot { it.id == itemId } }
        _actionResult.value = "Đã xóa mặt hàng khỏi giỏ hàng"
    }

    // Xóa toàn bộ giỏ hàng
    fun clearCart() {
        _items.value = emptyList()
        _actionResult.value = "Đã xóa toàn bộ giỏ hàng"
    }

    // Tính toán tổng tạm tính
    fun subTotal(): Long {
        return _items.value.sumOf { it.price * it.quantity }
    }

    // Tính toán khuyến mãi áp dụng
    fun discount(): Long {
        return if (_promoCode.value == "NNDAI") 10000L else 0L
    }

    // Tổng cộng sau phí và khuyến mãi
    fun total(): Long {
        val subtotal = subTotal()
        return subtotal + shippingFee - discount()
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
            // Giả lập một thao tác mạng
            kotlinx.coroutines.delay(1200)
            _isLoading.value = false
            _actionResult.value = "Checkout thành công"
            // Sau khi checkout thành công: làm rỗng giỏ hàng
            _items.value = emptyList()
        }
    }

    // Xóa thông báo
    fun clearResult() { _actionResult.value = null }
}

