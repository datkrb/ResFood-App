package com.muatrenthenang.resfood.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.ui.screens.home.booking_table.BookingBanner
import com.muatrenthenang.resfood.ui.screens.home.card_food.CardFood
import com.muatrenthenang.resfood.ui.screens.home.category_food.CategoryFood
import com.muatrenthenang.resfood.ui.screens.home.header.HeaderSection
import com.muatrenthenang.resfood.ui.screens.home.search.SearchBar
import com.muatrenthenang.resfood.ui.viewmodel.HomeViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel
import com.muatrenthenang.resfood.ui.components.FoodItemSkeleton

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onFoodClick: (Food) -> Unit,
    paddingValues: PaddingValues = PaddingValues()
){
    val uiState by homeViewModel.uiState.collectAsState()
    val userState by userViewModel.userState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    HeaderSection(userState)
                    SearchBar(
                        searchText = uiState.searchQuery,
                        onSearchTextChanged = { homeViewModel.setSearchQuery(it) }
                    )
                    BookingBanner(onClick = {})
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        uiState.categories.forEach { category ->
                            CategoryFood(
                                imgVector = category.icon,
                                categoryFood = category.name,
                                onClick = {}
                            )
                        }
                    }
                }
            }

            // Layout food
            items(uiState.foods.size) { index ->
                val food = uiState.foods[index]
                CardFood(
                    food,
                    onClickFood = { food ->
                        onFoodClick(food)
                    },
                    onClickAdd = {}
                )
            }
        }
    }
}