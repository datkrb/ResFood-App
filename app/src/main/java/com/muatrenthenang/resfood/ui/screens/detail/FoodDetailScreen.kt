package com.muatrenthenang.resfood.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.muatrenthenang.resfood.ui.theme.LightRed
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.muatrenthenang.resfood.ui.viewmodel.FoodDetailViewModel

@Composable
fun FoodDetailScreen(
    foodId: String,
    onNavigateBack: () -> Unit,
    onNavigateToReview: (String) -> Unit,
    viewModel: FoodDetailViewModel = viewModel()
) {
    val quantity by viewModel.quantity.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val food by viewModel.food.collectAsState()
    val allToppings by viewModel.allToppings.collectAsState()
    val selectedToppings by viewModel.selectedToppings.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadFoodDetail(foodId)
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
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
                    color = MaterialTheme.colorScheme.background
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
                        ShareAndReview(
                            onShareClick = {
                                val foodName = food?.name ?: "Món ngon"
                                val imageUrl = food?.imageUrl ?: "https://via.placeholder.com/600x400.png?text=ResFood"
                                val foodId = food?.id ?: ""
                                shareFood(context, foodName, imageUrl, foodId)
                            },
                            onReviewClick = {
                                food?.id?.let { onNavigateToReview(it) }
                            }
                        )        // btn chia se + danh gia
                        FoodDescription(food)   // mo ta mon an

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text ="Topping them",
                                color = MaterialTheme.colorScheme.onBackground,
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }

            // favorite
            val isFavorite by viewModel.isFavorite.collectAsState()
            IconButton(
                onClick = { viewModel.addToFavorites() },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) LightRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(22.dp)
        ) {
            AddToCart(
                totalPrice = totalPrice,
                quantity = quantity,
                onIncrease = { viewModel.increaseQuantity() },
                onDecrease = { viewModel.decreaseQuantity() },
                onAddToCartClick = { viewModel.addToCart() }
            )
        }
    }
}

fun shareFood(context: Context, foodName: String, imageUrl: String, foodId: String) {
    CoroutineScope(Dispatchers.IO).launch {
        // Download image using Coil
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = (loader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            // Save bitmap to cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // creation of directory
            val file = File(cachePath, "shared_food_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "Hãy thử món $foodName tại ResFood! \nNgon tuyệt cú mèo!\n\nXem chi tiết tại: resfood://food/$foodId")
                    type = "image/png"
                }
                val chooser = Intent.createChooser(shareIntent, "Chia sẻ món ăn qua")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, chooser, null)
            }
        } else {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Hãy thử món $foodName tại ResFood! Ngon tuyệt cú mèo!\n\nXem chi tiết tại: resfood://food/$foodId")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ món ăn qua")
            startActivity(context, shareIntent, null)
        }
    }
}