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
import com.muatrenthenang.resfood.ui.screens.favorites.FavoritesUIState
import kotlinx.coroutines.flow.update

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uiState = MutableStateFlow(FavoritesUIState())
    val uiState = _uiState.asStateFlow()

    private var _allFavFoods = listOf<RepoFavoriteItem>()

    private val _tag = "FavoritesViewModel"

    init {
        _isLoading.value = true
        loadFavorites()
    }

    fun loadFavorites() {
        Log.d(_tag, "Loading favorites")
        viewModelScope.launch {
            try {
                val result = _repository.getFavorites()
                if (result.isSuccess) {
                    _allFavFoods = result.getOrNull() ?: emptyList()
                    _items.value = _allFavFoods
                    _needLogin.value = false
                    Log.d(_tag, "Loaded ${_allFavFoods.size} favorite items")
                    
                    // Re-apply filter if there is an existing query
                    filterFavoriteFood(_uiState.value.searchQuery)
                } else {
                    if (result.exceptionOrNull()?.message?.contains("chưa đăng nhập", true) == true) {
                        Log.e(_tag, "User not logged in")
                        _needLogin.value = true
                    }
                    _actionResult.value = result.exceptionOrNull()?.localizedMessage
                }
            } catch (e: Exception) {
                _actionResult.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            val result = _repository.removeFavorite(id)
            if (result.isSuccess) {
                _actionResult.value = "Đã xóa khỏi danh sách yêu thích"
                // Remove from local master list immediately for UI responsiveness
                _allFavFoods = _allFavFoods.filter { it.food.id != id }
                // Re-apply filter
                filterFavoriteFood(_uiState.value.searchQuery)
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
            try {
                val result = _cartRepository.addOrUpdateCartItem(item.food.id, 1)
                if (result.isSuccess) {
                    _actionResult.value = "Đã thêm \"${item.food.name}\" vào giỏ hàng"
                } else {
                    _actionResult.value = result.exceptionOrNull()?.localizedMessage ?: "Lỗi khi thêm vào giỏ hàng"
                }
            } catch (e: Exception) {
                _actionResult.value = e.localizedMessage
            }
        }
    }

    fun formatCurrency(value: Int): String {
        return java.text.DecimalFormat("###,###").format(value) + "đ"
    }

    fun clearResult() { _actionResult.value = null }

    fun onSearchTextChanged(query: String){
        _uiState.update { it.copy(searchQuery = query) }
        filterFavoriteFood(query)
    }

    fun filterFavoriteFood(query: String){
        val filterFavFood = if (query.isBlank()) {
            _allFavFoods
        }
        else {
            _allFavFoods.filter { favFoods ->
                favFoods.food.name.contains(query, ignoreCase = true)
            }
        }
        _items.value = filterFavFood
    }

}