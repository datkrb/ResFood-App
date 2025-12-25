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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.ui.screens.home.booking_table.BookingBanner
import com.muatrenthenang.resfood.ui.screens.home.card_food.CardFood
import com.muatrenthenang.resfood.ui.screens.home.category_food.CategoryFood
import com.muatrenthenang.resfood.ui.screens.home.header.HeaderSection
import com.muatrenthenang.resfood.ui.screens.home.search.SearchBar
import com.muatrenthenang.resfood.ui.viewmodel.HomeViewModel
import androidx.compose.foundation.layout.PaddingValues
import com.muatrenthenang.resfood.ui.theme.BgDark
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onFoodClick: (Food) -> Unit,
    paddingValues: PaddingValues = PaddingValues()
){
    val uiState by homeViewModel.uiState.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    val gridState = rememberLazyGridState()

    // Scroll về đầu một cách smooth khi search query thay đổi
    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isNotEmpty()) {
            gridState.animateScrollToItem(0)
        }
    }

    // Box với background để fill hết màn hình
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
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
                    onSearchTextChange = { query ->
                        homeViewModel.setSearchQuery(query)
                    }
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

        // Layout food với key để tránh recompose
        items(
            count = uiState.foods.size,
            key = { index -> 
                if (index < uiState.foods.size) {
                    uiState.foods[index].id
                } else {
                    "food_$index"
                }
            }
        ) { index ->
            val food = uiState.foods[index]
            CardFood(
                food,
                onClickFood = { food ->
                    onFoodClick(food)
                },
                onClickAdd = {}
            )
        }
        
        // Empty state khi không có kết quả tìm kiếm
        if (uiState.searchQuery.isNotEmpty() && uiState.foods.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy món ăn nào",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        }
    }
}