package com.muatrenthenang.resfood.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tapas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.CategoryItem
import com.muatrenthenang.resfood.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.muatrenthenang.resfood.ui.screens.home.HomeUiState
import com.muatrenthenang.resfood.data.repository.FoodRepository
import com.muatrenthenang.resfood.R

class HomeViewModel (
    private val _foodRepository: FoodRepository = FoodRepository()
)   : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _allFoods = MutableStateFlow<List<Food>>(emptyList())

    init {
        loadCategories()
        loadFoods()
    }

    private fun loadCategories() {
        viewModelScope.launch {

            // danh mục món ăn
            val categoryData = listOf(
                CategoryItem(icon = Icons.Default.Restaurant, name = "Món chính", nameRes = R.string.main_dish),
                CategoryItem(icon = Icons.Default.Tapas, name = "Món phụ", nameRes = R.string.side_dish),
                CategoryItem(icon = Icons.Default.EmojiFoodBeverage, name = "Nước uống", nameRes = R.string.beverage),
                CategoryItem(icon = Icons.Default.Fastfood, name = "Tráng miệng", nameRes = R.string.dessert)
            )

            // keep UI categories; foods will be loaded from repository
            _uiState.value = _uiState.value.copy(categories = categoryData)
        }
    }

    private fun loadFoods() {
        viewModelScope.launch {
            try {
                val result = _foodRepository.getFoods()
                result.fold(onSuccess = { foods ->
                    _allFoods.value = foods
                    applyFilters() // Apply existing filters to new data
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }, onFailure = {
                    _allFoods.value = emptyList()
                    _uiState.value = _uiState.value.copy(foods = emptyList(), isLoading = false)
                })
            } catch (e: Exception) {
                _allFoods.value = emptyList()
                _uiState.value = _uiState.value.copy(foods = emptyList(), isLoading = false)
            }
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val query = currentState.searchQuery
        val category = currentState.selectedCategory
        val minPrice = currentState.minPrice
        val maxPrice = currentState.maxPrice
        val minRating = currentState.minRating
        
        var filtered = _allFoods.value
        
        // Filter by category if selected
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }
        
        // Filter by search query if not blank
        if (query.isNotBlank()) {
            filtered = filtered.filter { food ->
                food.name.contains(query, ignoreCase = true)
            }
        }

        // Filter by price
        if (minPrice != null) {
            filtered = filtered.filter { it.price >= minPrice }
        }
        if (maxPrice != null) {
            filtered = filtered.filter { it.price <= maxPrice }
        }

        // Filter by rating
        if (minRating != null) {
             filtered = filtered.filter { it.rating >= minRating }
        }
        
        _uiState.value = _uiState.value.copy(foods = filtered)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun setFilters(minPrice: Int?, maxPrice: Int?, minRating: Float?, category: String?) {
        _uiState.value = _uiState.value.copy(
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            selectedCategory = category
        )
        applyFilters()
    }

    fun selectCategory(category: String) {
        val currentCategory = _uiState.value.selectedCategory
        // Toggle selection: if clicking same category, deselect it (set to null)
        val newCategory = if (currentCategory == category) null else category
        
        _uiState.value = _uiState.value.copy(selectedCategory = newCategory)
        applyFilters()
    }

    // ---- Add to Cart ----
    private val _cartRepository = com.muatrenthenang.resfood.data.repository.CartRepository()
    
    private val _addToCartResult = MutableStateFlow<String?>(null)
    val addToCartResult: StateFlow<String?> = _addToCartResult.asStateFlow()

    fun addToCart(foodId: String) {
        viewModelScope.launch {
            val result = _cartRepository.addOrUpdateCartItem(foodId, 1, null, isAccumulate = true)
            if (result.isSuccess) {
                _addToCartResult.value = "Đã thêm vào giỏ hàng"
            } else {
                _addToCartResult.value = result.exceptionOrNull()?.message ?: "Lỗi thêm vào giỏ hàng"
            }
        }
    }

    fun clearAddToCartResult() {
        _addToCartResult.value = null
    }
}
