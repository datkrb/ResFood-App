package com.muatrenthenang.resfood.ui.screens.favorites

import com.muatrenthenang.resfood.data.model.FavoriteItem

data class FavoritesUIState(
    val isLoading: Boolean = true,
    val listFavoriteFood: List<FavoriteItem> = emptyList(),
    val searchQuery: String = ""
)