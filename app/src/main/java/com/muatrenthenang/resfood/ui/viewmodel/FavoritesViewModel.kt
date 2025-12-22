package com.muatrenthenang.resfood.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.FavoriteItem as RepoFavoriteItem
import com.muatrenthenang.resfood.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.muatrenthenang.resfood.data.repository.CartRepository

class FavoritesViewModel(
    private val _repository: FavoritesRepository = FavoritesRepository(),
    private val _cartRepository: CartRepository = CartRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<RepoFavoriteItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult = _actionResult.asStateFlow()

    private val _needLogin = MutableStateFlow(false)
    val needLogin = _needLogin.asStateFlow()

    private val _tag = "FavoritesViewModel"

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        Log.d(_tag, "Loading favorites")
        viewModelScope.launch {
            val result = _repository.getFavorites()
            if (result.isSuccess) {
                _items.value = result.getOrNull() ?: emptyList()
                _needLogin.value = false
                Log.d(_tag, "Loaded ${_items.value.size} favorite items")
            } else {
                if (result.exceptionOrNull()?.message?.contains("chưa đăng nhập", true) == true) {
                    Log.e(_tag, "User not logged in")
                    _needLogin.value = true
                }
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            val result = _repository.removeFavorite(id)
            if (result.isSuccess) {
                _actionResult.value = "Đã xóa khỏi danh sách yêu thích"
                loadFavorites()
            } else {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun addToCart(id: String) {
        val item = _items.value.firstOrNull { it.food.id == id }
        viewModelScope.launch {
            if (item == null) return@launch
            if (!item.food.isAvailable) {
                _actionResult.value = "Sản phẩm hiện không có"
                return@launch
            }
            val result = _cartRepository.addOrUpdateCartItem(item.food.id, 1)
            if (result.isSuccess) {
                _actionResult.value = "Đã thêm \"${item.food.name}\" vào giỏ hàng"
            } else {
                _actionResult.value = result.exceptionOrNull()?.localizedMessage ?: "Lỗi khi thêm vào giỏ hàng"
            }
        }
    }

    fun formatCurrency(value: Int): String {
        return java.text.DecimalFormat("###,###").format(value) + "đ"
    }

    fun clearResult() { _actionResult.value = null }
}