package com.muatrenthenang.resfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.Address
import com.muatrenthenang.resfood.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý địa chỉ của người dùng - kết nối Firebase
 */
class AddressViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress = _selectedAddress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val _editingAddress = MutableStateFlow<Address?>(null)
    val editingAddress = _editingAddress.asStateFlow()

    init {
        loadAddresses()
    }

    /**
     * Load danh sách địa chỉ từ Firebase
     */
    fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getAddresses()
                if (result.isSuccess) {
                    val addressList = result.getOrNull() ?: emptyList()
                    _addresses.value = addressList
                    // Auto-select default address
                    _selectedAddress.value = addressList.find { it.isDefault } ?: addressList.firstOrNull()
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi tải địa chỉ"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Chọn địa chỉ để giao hàng
     */
    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    /**
     * Thêm địa chỉ mới vào Firebase
     */
    fun addAddress(address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addAddress(address)
                if (result.isSuccess) {
                    _actionResult.value = "Đã thêm địa chỉ thành công"
                    loadAddresses() // Reload danh sách
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi thêm địa chỉ"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật địa chỉ trong Firebase
     */
    fun updateAddress(address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.updateAddress(address)
                if (result.isSuccess) {
                    _actionResult.value = "Đã cập nhật địa chỉ"
                    
                    // Cập nhật selected address nếu đang chọn địa chỉ này
                    if (_selectedAddress.value?.id == address.id) {
                        _selectedAddress.value = address
                    }
                    
                    loadAddresses() // Reload danh sách
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi cập nhật địa chỉ"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xóa địa chỉ từ Firebase
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.deleteAddress(addressId)
                if (result.isSuccess) {
                    _actionResult.value = "Đã xóa địa chỉ"
                    
                    // Nếu xóa địa chỉ đang chọn, reset selected
                    if (_selectedAddress.value?.id == addressId) {
                        _selectedAddress.value = null
                    }
                    
                    loadAddresses() // Reload danh sách
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi xóa địa chỉ"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Đặt địa chỉ làm mặc định trong Firebase
     */
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.setDefaultAddress(addressId)
                if (result.isSuccess) {
                    _actionResult.value = "Đã đặt làm địa chỉ mặc định"
                    loadAddresses() // Reload danh sách
                } else {
                    _actionResult.value = result.exceptionOrNull()?.message ?: "Lỗi đặt mặc định"
                }
            } catch (e: Exception) {
                _actionResult.value = e.message ?: "Lỗi không xác định"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set địa chỉ đang được edit
     */
    fun setEditingAddress(address: Address?) {
        _editingAddress.value = address
    }

    /**
     * Lấy địa chỉ theo ID từ cache
     */
    fun getAddressById(addressId: String): Address? {
        return _addresses.value.find { it.id == addressId }
    }

    /**
     * Clear action result
     */
    fun clearResult() {
        _actionResult.value = null
    }
}

