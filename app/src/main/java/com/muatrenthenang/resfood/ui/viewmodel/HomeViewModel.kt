package com.muatrenthenang.resfood.ui.screens.home

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
import com.muatrenthenang.resfood.R

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
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

            // danh sách món ăn (chưa phân theo danh mục)
            val foodData = listOf(
                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
                Food(name = "Khô gà đè tem", price = 100000, imageUrl = "https://cleverads.vn/wp-content/uploads/2023/10/thi-truong-healthy-food-1.jpg"),
                )

            _uiState.value = HomeUiState(
                categories = categoryData,
                foods = foodData
            )
        }
    }
}
