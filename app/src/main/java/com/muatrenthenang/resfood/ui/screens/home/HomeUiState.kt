package com.muatrenthenang.resfood.ui.screens.home

import com.muatrenthenang.resfood.data.model.CategoryItem
import com.muatrenthenang.resfood.data.model.Food

data class HomeUiState(
    val categories: List<CategoryItem> = emptyList(),
    val foods: List<Food> = emptyList()
)