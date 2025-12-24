package com.muatrenthenang.resfood.ui.screens.home

import NavigationBottom
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.ui.screens.home.booking_table.BookingBanner
import com.muatrenthenang.resfood.ui.screens.home.card_food.CardFood
import com.muatrenthenang.resfood.ui.screens.home.category_food.CategoryFood
import com.muatrenthenang.resfood.ui.screens.home.header.HeaderSection
import com.muatrenthenang.resfood.ui.screens.home.search.SearchBar
import com.muatrenthenang.resfood.ui.theme.BgDark
import com.muatrenthenang.resfood.ui.viewmodel.HomeViewModel
import com.muatrenthenang.resfood.ui.screens.home.footer.FooterSection

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onFoodClick: (Food) -> Unit
    onNavigateToSettings: () -> Unit
){
    val uiState by homeViewModel.uiState.collectAsState()
    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            NavigationBottom(
                onClick = {},
                currentRoute = "home",
                onNavigateToHome = {},
                onNavigateToSettings = onNavigateToSettings
            )
        }
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen(onNavigateToSettings = {})
}