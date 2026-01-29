package com.muatrenthenang.resfood.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.OrderListViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R

@Composable
fun OrderListScreen(
    status: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToReview: (String) -> Unit = {},
    viewModel: OrderListViewModel = viewModel()
) {
    // Tabs matching ReservationListScreen style
    val tabs = listOf("PENDING", "PROCESSING", "DELIVERING", "REVIEW", "COMPLETED", "CANCELLED", "ALL")
    val tabTitles = listOf(
        stringResource(R.string.status_display_pending),
        stringResource(R.string.status_display_processing),
        stringResource(R.string.status_display_delivering),
        stringResource(R.string.food_review),
        stringResource(R.string.status_display_completed),
        stringResource(R.string.status_display_cancelled),
        stringResource(R.string.common_all)
    )
    
    // Determine initial index based on passed status
    val initialIndex = when(status.uppercase()) {
        "PENDING" -> 0
        "PROCESSING" -> 1
        "DELIVERING" -> 2
        "REVIEW" -> 3
        "COMPLETED" -> 4
        "CANCELLED" -> 5
        "ALL" -> 6
        else -> 0 // Default to first if unknown or "all" passed generically
    }
    
    var selectedTabIndex by remember { mutableStateOf(initialIndex) }
    
    // Load data when tab changes
    LaunchedEffect(selectedTabIndex) {
        val currentStatus = tabs[selectedTabIndex]
        viewModel.loadOrders(if (currentStatus == "ALL") "all" else currentStatus)
    }

    val orders by viewModel.orders.collectAsState()
    var showReviewDialogForOrder by remember { mutableStateOf<Order?>(null) }
    val allOrdersList by viewModel.allOrders.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                OrderListTopBar(title = stringResource(R.string.order_history_title), onBack = onNavigateBack)
                
                // Tabs with Badge
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryColor,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = PrimaryColor
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, status ->
                        // Calculate count for this status
                        val count = when(status) {
                            "ALL" -> allOrdersList.size
                            "PENDING" -> allOrdersList.count { it.status == "PENDING" }
                            "PROCESSING" -> allOrdersList.count { it.status == "PROCESSING" }
                            "DELIVERING" -> allOrdersList.count { it.status == "DELIVERING" }
                            "COMPLETED" -> allOrdersList.count { it.status == "COMPLETED" }
                            "CANCELLED" -> allOrdersList.count { it.status == "CANCELLED" || it.status == "REJECTED" }
                            "REVIEW" -> allOrdersList.count { it.status == "COMPLETED" && !it.isReviewed }
                            else -> 0
                        }
                        
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tabTitles[index],
                                        color = if (selectedTabIndex == index) PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // Badge with count
                                    if (count > 0) {
                                        val badgeColor = when(status) {
                                            "PENDING" -> PrimaryColor
                                            "PROCESSING" -> Color(0xFFF97316)
                                            "DELIVERING" -> Color(0xFF3B82F6)
                                            "COMPLETED" -> SuccessGreen
                                            "CANCELLED" -> Color.Red
                                            "REVIEW" -> Color(0xFFF59E0B)
                                            else -> PrimaryColor
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(badgeColor.copy(alpha = if (selectedTabIndex == index) 1f else 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (count > 99) "99+" else count.toString(),
                                                color = if (selectedTabIndex == index) Color.White else badgeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.order_list_empty),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderCard(
                        order = order, 
                        viewModel = viewModel, 
                        onClick = { onNavigateToDetail(order.id) },
                        onReviewClick = { showReviewDialogForOrder = order }
                    )
                }
            }
        }
    }
    
    if (showReviewDialogForOrder != null) {
        com.muatrenthenang.resfood.ui.screens.order.ReviewSelectionDialog(
            order = showReviewDialogForOrder!!,
            onDismiss = { showReviewDialogForOrder = null },
            onItemSelect = { foodId ->
                showReviewDialogForOrder = null
                onNavigateToReview(foodId)
            }
        )
    }
}

@Composable
fun OrderListTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun OrderCard(order: Order, viewModel: OrderListViewModel, onClick: () -> Unit, onReviewClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Text
                val (statusText, statusColor) = getStatusDisplay(order.status)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Price
                val formattedPrice = stringResource(R.string.price_format_vnd, order.total)
                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            // Items List
            val displayItems = if (isExpanded) order.items else order.items.take(1)
            
            Column(modifier = Modifier.padding(16.dp)) {
                
                displayItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Food Image
                        AsyncImage(
                            model = item.foodImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray.copy(alpha = 0.1f))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.foodName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!item.selectedToppings.isNullOrEmpty()) {
                                Text(
                                    text = "+ ${item.selectedToppings.joinToString(", ") { it.name }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "x${item.quantity}       ${stringResource(R.string.price_format_vnd, item.price)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Show More / Less Button
                if (order.items.size > 1) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text(
                            text = if (isExpanded) stringResource(R.string.order_collapse) else stringResource(R.string.order_expand, order.items.size - 1),
                            fontSize = 13.sp,
                            color = PrimaryColor
                        )
                    }
                }
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.order_date_label) + dateFormat.format(order.createdAt.toDate()),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (order.status == "COMPLETED") {
                        Button(
                            onClick = onReviewClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor
                            ),
                            shape = RoundedCornerShape(50) // Pill shape
                        ) {
                            Text(stringResource(R.string.food_review), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getStatusDisplay(status: String): Pair<String, Color> {
    return when(status) {
        "PENDING" -> stringResource(R.string.status_display_pending) to PrimaryColor
        "PROCESSING" -> stringResource(R.string.status_display_processing) to Color(0xFFF97316) // Orange
        "DELIVERING" -> stringResource(R.string.status_display_delivering) to Color(0xFF3B82F6) // Blue
        "COMPLETED" -> stringResource(R.string.status_display_completed) to SuccessGreen
        "CANCELLED" -> stringResource(R.string.status_display_cancelled) to Color.Red
        "REJECTED" -> stringResource(R.string.admin_order_status_rejected) to Color(0xFFDC2626) // Red
        else -> status to Color.Gray
    }
}
