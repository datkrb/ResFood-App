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
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.data.repository.PromotionRepository

enum class PaymentMethod { ZALOPAY, MOMO, COD }

class CheckoutViewModel(
    private val _repository: CheckoutRepository = CheckoutRepository(),
    private val _userRepository: UserRepository = UserRepository(),
    private val _orderRepository: OrderRepository = OrderRepository(),
    private val _authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _promotionRepository: PromotionRepository = PromotionRepository()

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

    // --- VOUCHER STATES ---
    private val _availablePromotions = MutableStateFlow<List<Promotion>>(emptyList())
    val availablePromotions = _availablePromotions.asStateFlow()

    private val _selectedProductVoucher = MutableStateFlow<Promotion?>(null)
    val selectedProductVoucher = _selectedProductVoucher.asStateFlow()

    private val _selectedShippingVoucher = MutableStateFlow<Promotion?>(null)
    val selectedShippingVoucher = _selectedShippingVoucher.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val _shippingFee = 15000L

    init {
        loadSelectedCartItems()
        loadDefaultAddress()
        loadPromotions()
    }

    /**
     * Load promotions user can use
     */
    fun loadPromotions() {
        viewModelScope.launch {
            val userId = _authRepository.getCurrentUserId() ?: return@launch
            val result = _promotionRepository.getPromotionsForUser(userId)
            _availablePromotions.value = result.getOrNull() ?: emptyList()
        }
    }

    /**
     * Set selected vouchers
     */
    fun setVouchers(productVoucher: Promotion?, shippingVoucher: Promotion?) {
        _selectedProductVoucher.value = productVoucher
        _selectedShippingVoucher.value = shippingVoucher
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
    fun setAddress(a: Address) { _address.value = a }

    fun subTotal(): Long = _items.value.sumOf { it.food.price.toLong() * it.quantity }

    // Tính toán discount từ Product Voucher
    fun productDiscount(): Long {
        val voucher = _selectedProductVoucher.value ?: return 0L
        val subtotal = subTotal()

        if (subtotal < voucher.minOrderValue) return 0L

        var discount = if (voucher.discountType == 1) { // Amount
            voucher.discountValue.toLong()
        } else { // Percent check (assuming 0 is percent)
            // Logic percent here if supported, current logic mainly supports amount based on previous code
            // Assuming discountValue is percent if type 0? Or maybe fixed amount only for now?
            // Existing model says: discountType: Int = 0, // 0: %, 1: Amount
            if (voucher.discountType == 0) {
                (subtotal * voucher.discountValue) / 100
            } else {
                voucher.discountValue.toLong()
            }
        }
        
        // Cap at max discount if set
        if (voucher.maxDiscountValue > 0 && discount > voucher.maxDiscountValue) {
            discount = voucher.maxDiscountValue.toLong()
        }

        return discount
    }

    // Tính toán discount từ Shipping Voucher
    fun shippingDiscount(): Long {
        val voucher = _selectedShippingVoucher.value ?: return 0L
        val subtotal = subTotal()
        
        if (subtotal < voucher.minOrderValue) return 0L

        var discount = if (voucher.discountType == 1) {
             voucher.discountValue.toLong()
        } else {
            // Percent of shipping fee
             if (voucher.discountType == 0) {
                (_shippingFee * voucher.discountValue) / 100
            } else {
                voucher.discountValue.toLong()
            }
        }
        
        if (voucher.maxDiscountValue > 0 && discount > voucher.maxDiscountValue) {
            discount = voucher.maxDiscountValue.toLong()
        }
        
        // Cannot exceed shipping fee
        return minOf(discount, _shippingFee)
    }

    fun totalDiscount(): Long = productDiscount() + shippingDiscount()

    fun total(): Long = maxOf(0L, subTotal() + _shippingFee - totalDiscount())

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

                // Create Order object with Vouchers
                val order = Order(
                    id = "",
                    userId = userId,
                    userName = currentAddress.contactName,
                    userPhone = currentAddress.phone,
                    address = currentAddress,
                    items = orderItems,
                    subtotal = subTotal().toInt(),
                    discount = totalDiscount().toInt(),
                    deliveryFee = _shippingFee.toInt(),
                    total = total().toInt(),
                    status = "PENDING",
                    paymentMethod = when (_paymentMethod.value) {
                        PaymentMethod.COD -> "COD"
                        PaymentMethod.MOMO -> "MOMO"
                        PaymentMethod.ZALOPAY -> "ZALOPAY"
                    },
                    createdAt = Timestamp.now(),
                    
                    // Voucher info
                    productVoucherCode = _selectedProductVoucher.value?.code,
                    productVoucherId = _selectedProductVoucher.value?.id,
                    shippingVoucherCode = _selectedShippingVoucher.value?.code,
                    shippingVoucherId = _selectedShippingVoucher.value?.id,
                    productDiscount = productDiscount().toInt(),
                    shippingDiscount = shippingDiscount().toInt()
                )

                // Save order to Firebase
                val result = _orderRepository.createOrder(order)
                
                if (result.isSuccess) {
                    // Use vouchers (deduct quantity)
                    _selectedProductVoucher.value?.let { 
                        _promotionRepository.useVoucher(it.id, userId)
                    }
                    _selectedShippingVoucher.value?.let {
                        _promotionRepository.useVoucher(it.id, userId)
                    }

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

    /**
     * Create Order for ZaloPay and get Token
     */
    fun createZaloPayOrder(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            // Validate items
            val selectedItems = _items.value.filter { it.isSelected }
            if (selectedItems.isEmpty()) {
                _actionResult.value = "Không có món nào được chọn"
                onResult(null)
                return@launch
            }
            
            val currentAddress = _address.value
            if (currentAddress.id.isBlank()) {
                _actionResult.value = "Vui lòng chọn địa chỉ"
                onResult(null)
                return@launch
            }

            _isLoading.value = true
            try {
                val userId = _authRepository.getCurrentUserId() ?: ""
                 // Convert items
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

                val order = Order(
                    id = "",
                    userId = userId,
                    userName = currentAddress.contactName,
                    userPhone = currentAddress.phone,
                    address = currentAddress,
                    items = orderItems,
                    subtotal = subTotal().toInt(),
                    discount = totalDiscount().toInt(),
                    deliveryFee = _shippingFee.toInt(),
                    total = total().toInt(),
                    status = "PENDING", // Initial status
                    paymentMethod = "ZALOPAY",
                    createdAt = Timestamp.now(),
                    productVoucherCode = _selectedProductVoucher.value?.code,
                    productVoucherId = _selectedProductVoucher.value?.id,
                    shippingVoucherCode = _selectedShippingVoucher.value?.code,
                    shippingVoucherId = _selectedShippingVoucher.value?.id,
                    productDiscount = productDiscount().toInt(),
                    shippingDiscount = shippingDiscount().toInt()
                )

                val createResult = _orderRepository.createOrder(order)
                if (createResult.isSuccess) {
                    val orderId = createResult.getOrNull()
                    if (orderId != null) {
                         // Call ZaloPay API
                         try {
                              val request = com.muatrenthenang.resfood.data.api.CreatePaymentRequest(orderId)
                              val response = com.muatrenthenang.resfood.data.api.ZaloPayClient.api.createPayment(request)
                              if (response.return_code == 1 && response.zp_trans_token != null) {
                                  _currentOrderId.value = orderId
                                  onResult(response.zp_trans_token)
                              } else {
                                  _actionResult.value = "Lỗi ZaloPay: ${response.return_message}"
                                  onResult(null)
                              }
                         } catch (e: Exception) {
                              _actionResult.value = "Lỗi kết nối ZaloPay: ${e.message}"
                              onResult(null)
                         }
                    }
                } else {
                    _actionResult.value = "Lỗi tạo đơn hàng: ${createResult.exceptionOrNull()?.message}"
                    onResult(null)
                }
            } catch (e: Exception) {
                _actionResult.value = "Lỗi: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Store current processing order ID
    private val _currentOrderId = MutableStateFlow<String?>(null)

    fun checkZaloPayStatus(appTransID: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = com.muatrenthenang.resfood.data.api.CheckStatusRequest(appTransID)
                val response = com.muatrenthenang.resfood.data.api.ZaloPayClient.api.checkStatus(request)
                
                if (response.return_code == 1) {
                     val userId = _authRepository.getCurrentUserId()
                     if (userId != null) {
                        _selectedProductVoucher.value?.let { _promotionRepository.useVoucher(it.id, userId) }
                        _selectedShippingVoucher.value?.let { _promotionRepository.useVoucher(it.id, userId) }
                     }

                     _repository.removeSelectedItems()
                     _actionResult.value = "Thanh toán thành công!"
                     // Order status in local repository might need update if we are observing it?
                     // But here we just navigate away or show success.
                } else if (response.return_code == 3) {
                     _actionResult.value = "Đang xử lý thanh toán..."
                } else {
                     _actionResult.value = "Thanh toán thất bại: ${response.return_message}"
                     _orderRepository.updateOrderStatus(_currentOrderId.value ?: "", "CANCELLED")
                }
            } catch (e: Exception) {
                _actionResult.value = "Lỗi kiểm tra trạng thái: ${e.message}"
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
