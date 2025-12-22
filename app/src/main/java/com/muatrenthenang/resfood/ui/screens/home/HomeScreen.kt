package com.muatrenthenang.resfood.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.screens.home.booking_table.BookingBanner
import com.muatrenthenang.resfood.ui.screens.home.card_food.CardFood
import com.muatrenthenang.resfood.ui.screens.home.category_food.CategoryFood
import com.muatrenthenang.resfood.ui.screens.home.header.HeaderSection
import com.muatrenthenang.resfood.ui.screens.home.search.SearchBar

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
){
    val uiState by homeViewModel.uiState.collectAsState()
    Scaffold(
        containerColor = Color(0xFF0F1923)
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    HeaderSection()
                    SearchBar()
                    BookingBanner()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        uiState.categories.forEach { category ->
                            CategoryFood(
                                imgVector = category.icon,
                                categoryFood = category.name
                            )
                        }
                    }
                }
            }

            // Layout food
            items(uiState.foods.size) { index ->
                val food = uiState.foods[index]
                CardFood(food)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}