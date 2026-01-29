package com.muatrenthenang.resfood.ui.screens.admin.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.Food
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.foodManagementUiState.collectAsState()
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // List Filters
    var selectedCategory by remember { mutableStateOf("All") }
    var sortDescending by remember { mutableStateOf(true) } // True: High to Low, False: Low to High

    // Review Detail Filter
    var selectedRatingFilter by remember { mutableStateOf<Int?>(null) } // Null = All

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_mgmt_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (selectedFood == null) {
            // == FOOD LIST VIEW ==
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCategory == "All") stringResource(R.string.review_filter_all_cats) else selectedCategory,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(if (category == "All") stringResource(R.string.review_filter_all_cats) else category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sort Button
                    IconButton(
                        onClick = { sortDescending = !sortDescending },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (sortDescending) Icons.Default.VerticalAlignTop else Icons.Default.VerticalAlignBottom,
                            contentDescription = "Sort Rating"
                        )
                    }
                }

                val displayedFoods = remember(state.foods, selectedCategory, sortDescending) {
                    state.foods
                        .filter { selectedCategory == "All" || it.category == selectedCategory }
                        .sortedBy { if (sortDescending) -(it.rating ?: 0f) else (it.rating ?: 0f) }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(displayedFoods) { food ->
                        FoodReviewCard(
                            food = food,
                            onClick = { 
                                selectedFood = food 
                                selectedRatingFilter = null // Reset filter when opening new food
                            }
                        )
                    }
                }
            }
        } else {
            // == REVIEW DETAIL VIEW ==
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header with back button to food list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    Row(
                       verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedFood = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to list")
                        }
                        Text(
                            text = stringResource(R.string.review_detail_header, selectedFood?.name ?: ""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Rating Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedRatingFilter == null,
                                    onClick = { selectedRatingFilter = null },
                                    label = { Text(stringResource(R.string.filter_all)) }
                                )
                            }
                            items((5 downTo 1).toList()) { star ->
                                FilterChip(
                                    selected = selectedRatingFilter == star,
                                    onClick = { selectedRatingFilter = star },
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$star")
                                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Filter Reviews
                val filteredReviews = remember(selectedFood, selectedRatingFilter) {
                    val reviews = selectedFood!!.reviews
                    if (selectedRatingFilter == null) reviews else reviews.filter { it.star == selectedRatingFilter }
                }

                if (filteredReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.review_empty_filtered), color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredReviews) { review ->
                            AdminReviewItem(
                                review = review,
                                onDelete = {
                                    reviewToDelete = review
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
        
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.review_confirm_delete_title)) },
                text = { Text(stringResource(R.string.review_confirm_delete_msg, reviewToDelete?.userName ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reviewToDelete != null && selectedFood != null) {
                                viewModel.deleteReview(selectedFood!!.id, reviewToDelete!!)
                                selectedFood = state.foods.find { it.id == selectedFood?.id }
                                showDeleteConfirm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
        
        // Update selected food when list changes (after delete)
        LaunchedEffect(state.foods) {
            if (selectedFood != null) {
                val updated = state.foods.find { it.id == selectedFood?.id }
                if (updated != null) {
                    selectedFood = updated
                }
            }
        }
    }
}

@Composable
fun FoodReviewCard(food: Food, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(text = stringResource(R.string.review_food_stats, food.rating.toString(), food.reviews.size), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AdminReviewItem(review: Review, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.userName.ifEmpty { stringResource(R.string.review_anonymous) },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(review.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.star) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                }
            }
            if (review.comment.isNotEmpty()) {
                Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
