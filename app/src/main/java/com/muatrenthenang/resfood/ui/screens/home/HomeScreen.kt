package com.muatrenthenang.resfood.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.muatrenthenang.resfood.ui.screens.home.booking_table.BookingBanner
import com.muatrenthenang.resfood.ui.screens.home.header.HeaderSection
import com.muatrenthenang.resfood.ui.screens.home.search.SearchBar
import com.muatrenthenang.resfood.ui.screens.home.footer.FooterSection

@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit){
    Scaffold(
        containerColor = Color(0xFF0F1923),
        bottomBar = {
            // Gọi FooterSection đã tách riêng
            FooterSection(onNavigateToSettings = onNavigateToSettings)
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    HeaderSection()
                    SearchBar()
                    BookingBanner()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen(onNavigateToSettings = {})
}