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
                CategoryItem(icon = Icons.Default.Restaurant, name = "Món chính"),
                CategoryItem(icon = Icons.Default.Tapas, name = "Món phụ"),
                CategoryItem(icon = Icons.Default.EmojiFoodBeverage, name = "Nước uống"),
                CategoryItem(icon = Icons.Default.Fastfood, name = "Tráng miệng")
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
                    _uiState.value = _uiState.value.copy(foods = foods, isLoading = false)
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
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory
        
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
        
        _uiState.value = _uiState.value.copy(foods = filtered)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun selectCategory(category: String) {
        val currentCategory = _uiState.value.selectedCategory
        // Toggle selection: if clicking same category, deselect it (set to null)
        val newCategory = if (currentCategory == category) null else category
        
        _uiState.value = _uiState.value.copy(selectedCategory = newCategory)
        applyFilters()
    }
}
