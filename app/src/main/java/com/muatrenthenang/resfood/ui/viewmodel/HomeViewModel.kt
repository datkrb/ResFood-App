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

class HomeViewModel : ViewModel() {
    private val _foodRepository = com.muatrenthenang.resfood.data.repository.FoodRepository()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                    _uiState.value = _uiState.value.copy(foods = foods)
                }, onFailure = {
                    // keep empty list on failure (you may show error later)
                    _uiState.value = _uiState.value.copy(foods = emptyList())
                })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(foods = emptyList())
            }
        }
    }
}
