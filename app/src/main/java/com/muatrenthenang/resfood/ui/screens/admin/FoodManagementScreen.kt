package com.muatrenthenang.resfood.ui.screens.admin

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.FoodStatus
import com.muatrenthenang.resfood.ui.viewmodel.admin.FoodManagementUiState
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation
import com.muatrenthenang.resfood.R
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.data.model.Food
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String?) -> Unit, // Null for Add, ID for Edit
    onNavigateToHome: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val state by viewModel.foodManagementUiState.collectAsState()
    val filteredFoods = state.filteredFoods
    val pullRefreshState = rememberPullToRefreshState()
    
    // Delete Confirmation State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.food_mgmt_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                onMenuClick = onNavigateToMenu,
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.error != null) {
                    Text(
                        text = "Lỗi: ${state.error}",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.table_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Reusing generic search hint or create new
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
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

            // Filters Row - Only Category and Status (removed Branch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                 // Category Filter
                 Box(modifier = Modifier.weight(1f)) {
                     var expanded by remember { mutableStateOf(false) }
                     ExposedDropdownMenuBox(
                         expanded = expanded,
                         onExpandedChange = { expanded = !expanded }
                     ) {
                         OutlinedTextField(
                             value = if(state.selectedCategory == "All") stringResource(R.string.filter_all) else state.selectedCategory,
                             onValueChange = {},
                             readOnly = true,
                             label = { Text(stringResource(R.string.food_label_category), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                             trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                             modifier = Modifier.menuAnchor().fillMaxWidth(),
                             colors = OutlinedTextFieldDefaults.colors(
                                 focusedContainerColor = cardColor,
                                 unfocusedContainerColor = cardColor,
                                 focusedBorderColor = Color.Transparent,
                                 unfocusedBorderColor = Color.Transparent,
                                 focusedLabelColor = primaryColor,
                                 unfocusedLabelColor = Color.Gray
                             ),
                             singleLine = true
                         )
                         ExposedDropdownMenu(
                             expanded = expanded,
                             onDismissRequest = { expanded = false },
                             modifier = Modifier.background(cardColor)
                         ) {
                             state.categories.forEach { category ->
                                 DropdownMenuItem(
                                     text = { Text(if(category == "All") stringResource(R.string.filter_all) else category, color = MaterialTheme.colorScheme.onSurface) },
                                     onClick = {
                                         viewModel.setCategoryFilter(category)
                                         expanded = false
                                     },
                                     contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                 )
                             }
                         }
                     }
                 }

                 // Status Filter
                 Box(modifier = Modifier.weight(1f)) {
                     var expanded by remember { mutableStateOf(false) }
                     ExposedDropdownMenuBox(
                         expanded = expanded,
                         onExpandedChange = { expanded = !expanded }
                     ) {
                         OutlinedTextField(
                             value = state.selectedStatus.displayName,
                             onValueChange = {},
                             readOnly = true,
                             label = { Text(stringResource(R.string.admin_analytics_order_status), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                             trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                             modifier = Modifier.menuAnchor().fillMaxWidth(),
                             colors = OutlinedTextFieldDefaults.colors(
                                 focusedContainerColor = cardColor,
                                 unfocusedContainerColor = cardColor,
                                 focusedBorderColor = Color.Transparent,
                                 unfocusedBorderColor = Color.Transparent,
                                 focusedLabelColor = primaryColor,
                                 unfocusedLabelColor = Color.Gray
                             ),
                             singleLine = true
                         )
                         ExposedDropdownMenu(
                             expanded = expanded,
                             onDismissRequest = { expanded = false },
                             modifier = Modifier.background(cardColor)
                         ) {
                             FoodStatus.values().forEach { status ->
                                 DropdownMenuItem(
                                     text = { Text(status.displayName, color = MaterialTheme.colorScheme.onSurface) },
                                     onClick = {
                                         viewModel.setStatusFilter(status)
                                         expanded = false
                                     },
                                     contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                 )
                             }
                         }
                     }
                 }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.food_list_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        onEditClick = { onNavigateToEdit(food.id) },
                        onDeleteClick = {
                            foodToDelete = food
                            showDeleteDialog = true
                        }
                    )
                }
            }
            } // End Column

            if(state.isLoading) {
                 Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .background(Color.Black.copy(alpha = 0.3f))
                         .clickable(enabled = false) {}, // Block clicks
                     contentAlignment = Alignment.Center
                 ) {
                     CircularProgressIndicator(color = primaryColor)
                 }
            }
        }
    } // End PullToRefreshBox
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && foodToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                foodToDelete = null
            },
            title = { Text(text = stringResource(R.string.food_confirm_delete)) },
            text = { Text(text = stringResource(R.string.food_delete_msg, foodToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodToDelete?.let { food ->
                            viewModel.deleteFood(food.id)
                        }
                        showDeleteDialog = false
                        foodToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        foodToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    } // End Scaffold
} // End Screen

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
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FoodItemCard(
    food: Food,
    cardColor: Color,
    primaryColor: Color,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                    .background(if (food.isAvailable) com.muatrenthenang.resfood.ui.theme.SuccessGreen else com.muatrenthenang.resfood.ui.theme.LightRed)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (food.isAvailable) stringResource(R.string.food_stock_in) else stringResource(R.string.food_stock_out),
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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = food.description.ifEmpty { "Không có mô tả" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        
        Column {
            // Edit Button
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Delete Button
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
