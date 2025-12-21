package com.muatrenthenang.resfood.ui.screens.home

import com.muatrenthenang.resfood.data.model.CategoryItem

data class HomeUiState(
    val categories: List<CategoryItem> = emptyList()
)