package com.muatrenthenang.resfood.ui.screens.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.data.model.OrderItem
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.viewmodel.FoodDetailViewModel
import com.muatrenthenang.resfood.ui.viewmodel.OrderListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderReviewScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    foodDetailViewModel: FoodDetailViewModel = viewModel(),
    orderViewModel: OrderListViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Ensure order data is loaded
    LaunchedEffect(Unit) {
        if (orderViewModel.orders.value.isEmpty()) {
            orderViewModel.loadOrders("all")
        }
    }
    
    val orders by orderViewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }
    
    // State to hold reviews for each item
    // Map<FoodId, Pair<Rating, Comment>>
    val reviewState = remember { mutableStateMapOf<String, Pair<Int, String>>() }
    var isSubmitting by remember { mutableStateOf(false) }

    // Initialize state when order loads
    LaunchedEffect(order) {
        order?.items?.forEach { item ->
            if (!reviewState.containsKey(item.foodId)) {
                reviewState[item.foodId] = 5 to "" // Default 5 stars, empty comment
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.order_review_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        if (order != null && !isSubmitting) {
                            scope.launch {
                                isSubmitting = true
                                var successCount = 0
                                var failCount = 0
                                
                                // Submit reviews for each item
                                order.items.forEach { item ->
                                    val (rating, comment) = reviewState[item.foodId] ?: (5 to "")
                                    
                                    // Only submit if user explicitly interacted or we want to submit defaults
                                    val result = foodDetailViewModel.submitReviewForFood(
                                        foodId = item.foodId,
                                        review = Review(
                                            star = rating,
                                            comment = comment,
                                            userId = order.userId,
                                            userName = order.userName,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    
                                    if (result.isSuccess) successCount++ else failCount++
                                }
                                
                                // Mark order as reviewed
                                orderViewModel.markOrderAsReviewed(orderId)
                                
                                isSubmitting = false
                                val successMsg = context.getString(R.string.order_review_success)
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.order_review_submit_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.order_review_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                items(order.items) { item ->
                    val currentState = reviewState[item.foodId] ?: (5 to "")
                    
                    OrderItemReviewCard(
                        item = item,
                        rating = currentState.first,
                        comment = currentState.second,
                        onRatingChange = { newRating -> 
                            reviewState[item.foodId] = newRating to currentState.second 
                        },
                        onCommentChange = { newComment -> 
                            reviewState[item.foodId] = currentState.first to newComment 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemReviewCard(
    item: OrderItem,
    rating: Int,
    comment: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Food Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.foodImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.foodName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.selectedToppings.isNullOrEmpty()) {
                        Text(
                            text = item.selectedToppings.joinToString(", ") { it.name },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            // Rating
            Text(stringResource(R.string.order_review_quality_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { onRatingChange(star) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Rate $star stars",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Comment
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text(stringResource(R.string.order_review_comment_label)) },
                placeholder = { Text(stringResource(R.string.order_review_comment_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
    }
}
