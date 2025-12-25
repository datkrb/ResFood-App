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
    
    // Lưu danh sách foods gốc (chưa filter)
    private val _allFoods = MutableStateFlow<List<Food>>(emptyList())
    val allFoods: StateFlow<List<Food>> = _allFoods.asStateFlow()

    init {
        loadCategories()
        loadFoods()
    }
    
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterFoods(query)
    }
    
    private fun filterFoods(query: String) {
        val filtered = if (query.isBlank()) {
            _allFoods.value
        } else {
            _allFoods.value.filter { food ->
                food.name.contains(query, ignoreCase = true) ||
                food.description.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(foods = filtered)
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
                    // Apply current search filter if any
                    filterFoods(_uiState.value.searchQuery)
                }, onFailure = {
                    // keep empty list on failure (you may show error later)
                    _allFoods.value = emptyList()
                    _uiState.value = _uiState.value.copy(foods = emptyList())
                })
            } catch (e: Exception) {
                _allFoods.value = emptyList()
                _uiState.value = _uiState.value.copy(foods = emptyList())
            }
        }
    }
}
