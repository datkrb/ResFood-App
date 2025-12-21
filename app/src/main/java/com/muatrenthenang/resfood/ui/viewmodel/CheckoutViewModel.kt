package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// Class data test, sẽ chuyển vào model chung sau
data class Address(
    val label: String,
    val isDefault: Boolean,
    val addressLine: String,
    val contactName: String,
    val phone: String
)

enum class PaymentMethod { ZALOPAY, MOMO, COD }

class CheckoutViewModel : ViewModel() {
    // Helper
    fun formatCurrency(value: Long): String {
        val grouped = java.text.DecimalFormat("###,###").format(value)
        return "${grouped}đ"
    }
    // Sample order items
    private val _items = MutableStateFlow(
        listOf(
            CartItem(
                id = "pho_special",
                name = "Phở Đặc Biệt (Tái, Nạm, Gầu)",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA0mI2k-FUn7ejRNWeJcaUpVMB10SrNP4lOCJF2zrJhrZliICyfkomgd3nr8VNT5icIEvsfRKabU1xh4XVncw71TPsFORF1FcJk53Ghqei0EB6gwk5xygTiJD5jXfUOjq1PSoVE07ec-aHphE3fqvIeUZ4w5veHo1Guxrx_PYuNIE50Nivso1EXAFGclqfwM-1vAPBn6_bfxoi_CPvl17g9AAX1atGp9UPcy2-G3K-h2uHCQRskaRLt0rzyTk1_VKTlhjEhdBKzaRc",
                price = 150000L,
                quantity = 2
            ),
            CartItem(
                id = "pho_special",
                name = "Phở Đặc Biệt (Tái, Nạm, Gầu)",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA0mI2k-FUn7ejRNWeJcaUpVMB10SrNP4lOCJF2zrJhrZliICyfkomgd3nr8VNT5icIEvsfRKabU1xh4XVncw71TPsFORF1FcJk53Ghqei0EB6gwk5xygTiJD5jXfUOjq1PSoVE07ec-aHphE3fqvIeUZ4w5veHo1Guxrx_PYuNIE50Nivso1EXAFGclqfwM-1vAPBn6_bfxoi_CPvl17g9AAX1atGp9UPcy2-G3K-h2uHCQRskaRLt0rzyTk1_VKTlhjEhdBKzaRc",
                price = 150000L,
                quantity = 1
            )
        )
    )
    val items = _items.asStateFlow()

    private val _address = MutableStateFlow(Address(
        label = "Nhà riêng",
        isDefault = true,
        addressLine = "123 Đường Nguyễn Huệ, P. Bến Nghé, Quận 1, TP. Hồ Chí Minh",
        contactName = "Nguyễn Văn A",
        phone = "+84 901 234 567"
    ))
    val address = _address.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.ZALOPAY)
    val paymentMethod = _paymentMethod.asStateFlow()

    private val _promoInput = MutableStateFlow("")
    val promoInput = _promoInput.asStateFlow()

    // Applied promo code
    private val _appliedPromo = MutableStateFlow<String?>(null)
    val appliedPromo = _appliedPromo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val shippingFee = 15000L

    fun setPaymentMethod(m: PaymentMethod) { _paymentMethod.value = m }
    fun setPromoInput(s: String) { _promoInput.value = s }

    fun subTotal(): Long = _items.value.sumOf { it.price * it.quantity }
    fun discount(): Long = if (_appliedPromo.value?.trim()?.uppercase() == "NNDAI") 10000L else 0L
    fun total(): Long = subTotal() + shippingFee - discount()

    fun applyPromo() {
        viewModelScope.launch {
            val code = _promoInput.value.trim().uppercase()
            if (code.isEmpty()) {
                _actionResult.value = "Vui lòng nhập mã"
                return@launch
            }

            if (code == "NNDAI") {
                _appliedPromo.value = code
                _actionResult.value = "Mã khuyến mãi đã được áp dụng"
            } else {
                _appliedPromo.value = null
                _actionResult.value = "Mã không hợp lệ"
            }
        }
    }

    fun confirmPayment() {
        viewModelScope.launch {
            if (_items.value.isEmpty()) {
                _actionResult.value = "Giỏ hàng trống"
                return@launch
            }

            _isLoading.value = true
            delay(1200)
            _isLoading.value = false
            _actionResult.value = "Thanh toán thành công"
        }
    }

    fun clearResult(){ _actionResult.value = null }
}
