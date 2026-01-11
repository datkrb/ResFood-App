package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.repository.CheckoutRepository
import com.muatrenthenang.resfood.data.repository.UserRepository

enum class PaymentMethod { ZALOPAY, MOMO, COD }

class CheckoutViewModel(
    private val _repository: CheckoutRepository = CheckoutRepository(),
    private val _userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items = _items.asStateFlow()

    // Initialize with empty address, will be loaded from Firebase
    private val _address = MutableStateFlow(Address(
        id = "",
        label = "Đang tải...",
        isDefault = true,
        addressLine = "",
        ward = "",
        district = "",
        city = "",
        contactName = "",
        phone = ""
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

    private val _shippingFee = 15000L

    init {
        loadSelectedCartItems()
        loadDefaultAddress()
    }

    /**
     * Load địa chỉ mặc định từ Firebase
     */
    fun loadDefaultAddress() {
        viewModelScope.launch {
            try {
                val result = _userRepository.getDefaultAddress()
                if (result.isSuccess) {
                    result.getOrNull()?.let { defaultAddress ->
                        _address.value = defaultAddress
                    } ?: run {
                        // Không có địa chỉ nào, hiển thị placeholder
                        _address.value = Address(
                            id = "",
                            label = "Chưa có địa chỉ",
                            isDefault = false,
                            addressLine = "Vui lòng thêm địa chỉ giao hàng",
                            ward = "",
                            district = "",
                            city = "",
                            contactName = "",
                            phone = ""
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback to placeholder if error
                _address.value = Address(
                    id = "",
                    label = "Lỗi tải địa chỉ",
                    isDefault = false,
                    addressLine = e.message ?: "Vui lòng thử lại",
                    ward = "",
                    district = "",
                    city = "",
                    contactName = "",
                    phone = ""
                )
            }
        }
    }

    fun loadSelectedCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = _repository.getSelectedCartItems()
                if (result.isSuccess) {
                    _items.value = result.getOrNull() ?: emptyList()
                } else {
                    _items.value = emptyList()
                    _actionResult.value = result.exceptionOrNull()?.localizedMessage
                }
            } catch (e: Exception) {
                _items.value = emptyList()
                _actionResult.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Lấy danh sách địa chỉ từ Firebase để tìm địa chỉ theo ID
     */
    suspend fun getAddressesFromRepo(): List<Address> {
        return try {
            _userRepository.getAddresses().getOrNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPaymentMethod(m: PaymentMethod) { _paymentMethod.value = m }
    fun setPromoInput(s: String) { _promoInput.value = s }
    fun setAddress(a: Address) { _address.value = a }

    fun subTotal(): Long = _items.value.sumOf { it.food.price.toLong() * it.quantity }
    fun discount(): Long = if (_appliedPromo.value?.trim()?.uppercase() == "NNDAI") 10000L else 0L
    fun total(): Long = subTotal() + _shippingFee - discount()

    fun applyPromo() {
        //test
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
            if (_items.value.none { it.isSelected }) {
                _actionResult.value = "Không có món nào được chọn để thanh toán"
                return@launch
            }
            _isLoading.value = true
            _repository.removeSelectedItems()
            delay(1200)
            _isLoading.value = false
            _actionResult.value = "Thanh toán thành công"
        }
    }

    fun clearResult(){ _actionResult.value = null }

    // Helper
    fun formatCurrency(value: Long): String {
        val grouped = java.text.DecimalFormat("###,###").format(value)
        return "${grouped}đ"
    }
}
