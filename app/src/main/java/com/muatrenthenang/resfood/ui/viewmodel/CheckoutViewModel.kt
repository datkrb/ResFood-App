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
import com.muatrenthenang.resfood.data.repository.PromotionRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.muatrenthenang.resfood.data.repository.BranchRepository
import com.muatrenthenang.resfood.data.model.Promotion

enum class PaymentMethod { SEPAY, COD }

class CheckoutViewModel(
    private val _repository: CheckoutRepository = CheckoutRepository(),
    private val _userRepository: UserRepository = UserRepository(),
    private val _orderRepository: OrderRepository = OrderRepository(),
    private val _authRepository: AuthRepository = AuthRepository(),
    private val _branchRepository: BranchRepository = BranchRepository()
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

    // SEPay QR Code URL
    private val _paymentQrUrl = MutableStateFlow<String?>(null)
    val paymentQrUrl = _paymentQrUrl.asStateFlow()

    private val _currentOrderId = MutableStateFlow<String?>(null)
    
    // Payment Success Trigger
    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess = _paymentSuccess.asStateFlow()

    private val _shippingFee = 15000L

    init {
        loadSelectedCartItems()
        loadDefaultAddress()
        loadPromotions()
        loadShippingFee()
    }

    /**
     * Load shipping fee from primary branch
     */
    private fun loadShippingFee() {
        viewModelScope.launch {
            _branchRepository.getPrimaryBranch().onSuccess { branch ->
                _shippingFee.value = branch.shippingFee
            }
        }
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

    val subTotal = _items.map { list ->
        list.sumOf { it.food.price.toLong() * it.quantity }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    // Tính toán discount từ Product Voucher
    val productDiscount = kotlinx.coroutines.flow.combine(_selectedProductVoucher, subTotal) { voucher, sub ->
        if (voucher == null) return@combine 0L
        if (sub < voucher.minOrderValue) return@combine 0L

        var discount = if (voucher.discountType == 1) { // Amount
            voucher.discountValue.toLong()
        } else { // Percent
            if (voucher.discountType == 0) {
                (sub * voucher.discountValue) / 100
            } else {
                voucher.discountValue.toLong()
            }
        }
        
        if (voucher.maxDiscountValue > 0 && discount > voucher.maxDiscountValue) {
            discount = voucher.maxDiscountValue.toLong()
        }
        discount
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    // Tính toán discount từ Shipping Voucher
    val shippingDiscount = kotlinx.coroutines.flow.combine(_selectedShippingVoucher, subTotal) { voucher, sub ->
        if (voucher == null) return@combine 0L
        if (sub < voucher.minOrderValue) return@combine 0L

        var discount = if (voucher.discountType == 1) {
             voucher.discountValue.toLong()
        } else {
             if (voucher.discountType == 0) {
                (_shippingFee.value * voucher.discountValue) / 100
            } else {
                voucher.discountValue.toLong()
            }
        }
        
        if (voucher.maxDiscountValue > 0 && discount > voucher.maxDiscountValue) {
            discount = voucher.maxDiscountValue.toLong()
        }
        // Cannot exceed shipping fee
        minOf(discount, _shippingFee)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    val totalDiscount = kotlinx.coroutines.flow.combine(productDiscount, shippingDiscount) { p, s -> p + s }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    val total = kotlinx.coroutines.flow.combine(subTotal, totalDiscount) { sub, disc ->
        maxOf(0L, sub + _shippingFee - disc)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

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
                    subtotal = subTotal.value.toInt(),
                    discount = totalDiscount.value.toInt(),
                    deliveryFee = _shippingFee.toInt(),
                    total = total.value.toInt(),
                    status = "PENDING",
                    paymentMethod = when (_paymentMethod.value) {
                        PaymentMethod.COD -> "COD"
                        PaymentMethod.SEPAY -> "SEPAY"
                    },
                    createdAt = Timestamp.now(),
                    
                    // Voucher info
                    productVoucherCode = _selectedProductVoucher.value?.code,
                    productVoucherId = _selectedProductVoucher.value?.id,
                    shippingVoucherCode = _selectedShippingVoucher.value?.code,
                    shippingVoucherId = _selectedShippingVoucher.value?.id,
                    productDiscount = productDiscount.value.toInt(),
                    shippingDiscount = shippingDiscount.value.toInt()
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
     * Create Order for SEPay
     */
    fun createSepayOrder() {
        viewModelScope.launch {
            // Validate items
            val selectedItems = _items.value.filter { it.isSelected }
            if (selectedItems.isEmpty()) {
                _actionResult.value = "Không có món nào được chọn"
                return@launch
            }
            
            val currentAddress = _address.value
            if (currentAddress.id.isBlank()) {
                _actionResult.value = "Vui lòng chọn địa chỉ"
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
                    subtotal = subTotal.value.toInt(),
                    discount = totalDiscount.value.toInt(),
                    deliveryFee = _shippingFee.toInt(),
                    total = total.value.toInt(),
                    status = "WAITING_PAYMENT", // Changed from PENDING to hide from Order List until paid
                    paymentMethod = "SEPAY",
                    createdAt = Timestamp.now(),
                    productVoucherCode = _selectedProductVoucher.value?.code,
                    productVoucherId = _selectedProductVoucher.value?.id,
                    shippingVoucherCode = _selectedShippingVoucher.value?.code,
                    shippingVoucherId = _selectedShippingVoucher.value?.id,
                    productDiscount = productDiscount.value.toInt(),
                    shippingDiscount = shippingDiscount.value.toInt()
                )

                val createResult = _orderRepository.createOrder(order)
                
                if (createResult.isSuccess) {
                    val orderId = createResult.getOrNull()
                    _currentOrderId.value = orderId
                    if (orderId != null) {
                         // Call SEPay API
                         try {
                              val request = com.muatrenthenang.resfood.data.api.CreateSepayPaymentRequest(orderId)
                              val response = com.muatrenthenang.resfood.data.api.ResFoodPaymentClient.api.createSepayPayment(request)
                              
                              // Success, show QR
                              _paymentQrUrl.value = response.qrUrl
                              
                              // Use vouchers
                                _selectedProductVoucher.value?.let { _promotionRepository.useVoucher(it.id, userId) }
                                _selectedShippingVoucher.value?.let { _promotionRepository.useVoucher(it.id, userId) }
                                
                                // Reset payment success state
                                _paymentSuccess.value = false
                                
                                // Start listening for status change
                                launch {
                                    _orderRepository.getOrderByIdFlow(orderId).collect { ord ->
                                        if (ord != null && ord.status == "PENDING") {
                                            // Payment confirmed!
                                            _paymentSuccess.value = true
                                            _repository.removeSelectedItems() // Clear cart only now or when confirmed
                                            _currentOrderId.value = null 
                                        }
                                    }
                                }

                                // _repository.removeSelectedItems()

                         } catch (e: Exception) {
                              _actionResult.value = "Lỗi kết nối SEPay: ${e.message}"
                         }
                    }
                } else {
                    _actionResult.value = "Lỗi tạo đơn hàng: ${createResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _actionResult.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPaymentQr() {
        val orderId = _currentOrderId.value
        if (orderId != null) {
            viewModelScope.launch {
                val orderResult = _orderRepository.getOrderById(orderId)
                if (orderResult.isSuccess) {
                    val order = orderResult.getOrNull()
                    // Allow deleting WAITING_PAYMENT if user cancels
                    if (order != null && order.status == "WAITING_PAYMENT") {
                         _orderRepository.deleteOrder(orderId)
                    }
                }
                _currentOrderId.value = null
            }
        }
        _paymentQrUrl.value = null
        _paymentSuccess.value = false
    }

    fun clearResult(){ _actionResult.value = null }

    // Helper
    fun formatCurrency(value: Long): String {
        val grouped = java.text.DecimalFormat("###,###").format(value)
        return "${grouped}đ"
    }
}
