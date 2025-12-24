package com.muatrenthenang.resfood.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.screens.detail.components.AddToCart
import com.muatrenthenang.resfood.ui.screens.detail.components.FoodDescription
import com.muatrenthenang.resfood.ui.screens.detail.components.FoodStats
import com.muatrenthenang.resfood.ui.screens.detail.components.NameAndPriceFood
import com.muatrenthenang.resfood.ui.screens.detail.components.ShareAndReview
import com.muatrenthenang.resfood.ui.screens.detail.components.ToppingBonusCard
import com.muatrenthenang.resfood.ui.viewmodel.FoodDetailViewModel

@Composable
fun FoodDetailScreen(
    foodId: String,
    onNavigateBack: () -> Unit,
    viewModel: FoodDetailViewModel = viewModel()
) {
    val quantity by viewModel.quantity.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val food by viewModel.food.collectAsState()
    val allToppings by viewModel.allToppings.collectAsState()
    val selectedToppings by viewModel.selectedToppings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFoodDetail(foodId)
    }

    val imageFoodHeight = 400.dp
    val contentOverlap = 20.dp

    Box(modifier = Modifier.fillMaxSize()){

        // image food
        AsyncImage(
            model = food?.imageUrl
                ?: "https://via.placeholder.com/600x400.png?text=ResFood",
            contentDescription = "food image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageFoodHeight),
            alignment = Alignment.TopCenter
        )

        // information of food (scroll)
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ){
            // space of image
            item {
                Spacer(modifier = Modifier.height(imageFoodHeight - contentOverlap))
            }

            // information of food
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color(0xFF0F172A)
                ){
                    Column(
                        modifier = Modifier
                            .padding(start = 22.dp, bottom = 22.dp, end = 22.dp, top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ){
                        // handle small bar
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(100.dp)
                                .background(Color.Gray.copy(alpha = 0.5f))
                                .align(Alignment.CenterHorizontally)
                        )

                        NameAndPriceFood(food)  // ten mon an + gia tien
                        FoodStats(food)         // thong so mon an
                        ShareAndReview()        // btn chia se + danh gia
                        FoodDescription(food)   // mo ta mon an

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text ="Topping them",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            allToppings?.forEach { topping ->
                                ToppingBonusCard(
                                    topping = topping,
                                    isSelected = selectedToppings.contains(topping),
                                    onSelect = {
                                        val currentlySelected = selectedToppings.contains(topping)
                                        viewModel.onToppingSelected(topping, !currentlySelected)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        // btn Back va btn add favorite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // back
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // favorite
            IconButton(
                onClick = {},
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(22.dp)
        ) {
            AddToCart(
                totalPrice = totalPrice,
                quantity = quantity,
                onIncrease = { viewModel.increaseQuantity() },
                onDecrease = { viewModel.decreaseQuantity() },
                onAddToCartClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Test(){
    FoodDetailScreen("2", {})
}