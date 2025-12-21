package com.muatrenthenang.resfood.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tapas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muatrenthenang.resfood.data.model.CategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categoryData = listOf(
                CategoryItem(icon = Icons.Default.Restaurant, name = "Món chính"),
                CategoryItem(icon = Icons.Default.Tapas, name = "Món phụ"),
                CategoryItem(icon = Icons.Default.EmojiFoodBeverage, name = "Nước uống"),
                CategoryItem(icon = Icons.Default.Fastfood, name = "Tráng miệng")
            )
            _uiState.value = HomeUiState(categories = categoryData)
        }
    }
}
