package com.muatrenthenang.resfood.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.FoodFilter
import com.muatrenthenang.resfood.ui.viewmodel.admin.FoodManagementUiState
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation
import com.muatrenthenang.resfood.data.model.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String?) -> Unit, // Null for Add, ID for Edit
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val state by viewModel.foodManagementUiState.collectAsState()
    val filteredFoods = viewModel.getFilteredFoods()
    val pullRefreshState = rememberPullToRefreshState()
    
    // Dark theme colors from Theme
    val backgroundColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker
    val cardColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard
    val primaryColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor
    
    // Group foods logic can be added here if needed
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Quản lý món ăn", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .clickable { onNavigateToEdit(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        bottomBar = {
             AdminBottomNavigation(
                currentRoute = "admin_food_management",
                onHomeClick = onNavigateToHome,
                onMenuClick = { /* Already here */ },
                onAnalyticsClick = onNavigateToAnalytics,
                onSettingsClick = onNavigateToSettings,
                onFabClick = onNavigateToOrders
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Tìm kiếm món ăn...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = primaryColor
                )
            )

            // Filters
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { FilterChip("Tất cả", state.filter == FoodFilter.ALL, primaryColor) { viewModel.setFoodFilter(FoodFilter.ALL) } }
                item { FilterChip("Đang bán", state.filter == FoodFilter.AVAILABLE, primaryColor) { viewModel.setFoodFilter(FoodFilter.AVAILABLE) } }
                item { FilterChip("Hết hàng", state.filter == FoodFilter.OUT_OF_STOCK, primaryColor) { viewModel.setFoodFilter(FoodFilter.OUT_OF_STOCK) } }
                item { FilterChip("Món chính", false, primaryColor) {} } // Dummy
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "DANH SÁCH MÓN ĂN",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(filteredFoods) { food ->
                    FoodItemCard(
                        food = food,
                        cardColor = cardColor,
                        primaryColor = primaryColor,
                        onEditClick = { onNavigateToEdit(food.id) }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, primaryColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) primaryColor else Color(0xFF2A3645))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.LightGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FoodItemCard(
    food: Food,
    cardColor: Color,
    primaryColor: Color,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        Box(modifier = Modifier.size(80.dp)) {
            AsyncImage(
                model = food.imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = food.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            // Status Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (food.isAvailable) Color(0xFF00C853) else Color(0xFFD32F2F))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (food.isAvailable) "Còn hàng" else "Hết hàng",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = food.description.ifEmpty { "Không có mô tả" },
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${food.price}đ",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        // Edit Button
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
        }
    }
}
