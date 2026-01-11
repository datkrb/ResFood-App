package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.model.CartItem
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.model.OrderItem
import com.muatrenthenang.resfood.data.repository.AuthRepository
import com.muatrenthenang.resfood.data.repository.CheckoutRepository
import com.muatrenthenang.resfood.data.repository.OrderRepository
import com.muatrenthenang.resfood.data.repository.UserRepository

enum class PaymentMethod { ZALOPAY, MOMO, COD }

class CheckoutViewModel(
    private val _repository: CheckoutRepository = CheckoutRepository(),
    private val _userRepository: UserRepository = UserRepository(),
    private val _orderRepository: OrderRepository = OrderRepository(),
    private val _authRepository: AuthRepository = AuthRepository()
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

    private val _paymentMethod = MutableStateFlow(PaymentMethod.COD)
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

    /**
     * Xác nhận đơn hàng và tạo Order trong Firebase
     */
    fun confirmPayment() {
        viewModelScope.launch {
            // Validate items
            val selectedItems = _items.value.filter { it.isSelected }
            if (selectedItems.isEmpty()) {
                _actionResult.value = "Không có món nào được chọn để thanh toán"
                return@launch
            }

            // Validate address
            val currentAddress = _address.value
            if (currentAddress.id.isBlank() || currentAddress.contactName.isBlank()) {
                _actionResult.value = "Vui lòng chọn địa chỉ giao hàng"
                return@launch
            }

            _isLoading.value = true

            try {
                // Get current user ID
                val userId = _authRepository.getCurrentUserId() ?: ""

                // Convert cart items to order items
                val orderItems = selectedItems.map { cartItem ->
                    OrderItem(
                        foodId = cartItem.food.id,
                        foodName = cartItem.food.name,
                        foodImage = cartItem.food.imageUrl,
                        quantity = cartItem.quantity,
                        price = cartItem.food.price,
                        note = null
                    )
                }

                // Create Order object
                val order = Order(
                    id = "",
                    userId = userId,
                    userName = currentAddress.contactName,
                    userPhone = currentAddress.phone,
                    address = currentAddress.getFullAddress(),
                    items = orderItems,
                    subtotal = subTotal().toInt(),
                    discount = discount().toInt(),
                    deliveryFee = _shippingFee.toInt(),
                    total = total().toInt(),
                    status = "PENDING",
                    paymentMethod = when (_paymentMethod.value) {
                        PaymentMethod.COD -> "COD"
                        PaymentMethod.MOMO -> "MOMO"
                        PaymentMethod.ZALOPAY -> "ZALOPAY"
                    },
                    createdAt = Timestamp.now()
                )

                // Save order to Firebase
                val result = _orderRepository.createOrder(order)
                
                if (result.isSuccess) {
                    // Remove selected items from cart
                    _repository.removeSelectedItems()
                    _actionResult.value = "Đặt hàng thành công"
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi tạo đơn hàng"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Đã xảy ra lỗi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult(){ _actionResult.value = null }

    // Helper
    fun formatCurrency(value: Long): String {
        val grouped = java.text.DecimalFormat("###,###").format(value)
        return "${grouped}đ"
    }
}
