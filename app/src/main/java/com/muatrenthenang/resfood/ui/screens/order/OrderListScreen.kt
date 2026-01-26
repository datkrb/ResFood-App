package com.muatrenthenang.resfood.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
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

@Composable
fun OrderListScreen(
    status: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: OrderListViewModel = viewModel()
) {
    LaunchedEffect(status) {
        viewModel.loadOrders(status)
    }

    val orders by viewModel.orders.collectAsState()

    // Determine title based on status
    val title = when (status) {
        "pending" -> "Chờ xác nhận"
        "processing" -> "Đang chế biến"
        "delivering" -> "Đang giao"
        "review" -> "Đánh giá"
        "history" -> "Lịch sử mua hàng"
        else -> "Đơn hàng của tôi"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OrderListTopBar(title = title, onBack = onNavigateBack)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                OrderCard(order = order, viewModel = viewModel, onClick = { onNavigateToDetail(order.id) })
            }
        }
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
fun OrderCard(order: Order, viewModel: OrderListViewModel, onClick: () -> Unit) {
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
                val formattedPrice = String.format("%,dđ", order.total).replace(',', '.')
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
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "x${item.quantity}       ${String.format("%,dđ", item.price).replace(',', '.')}",
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
                            text = if (isExpanded) "Thu gọn" else "Xem thêm ${order.items.size - 1} món khác",
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
                        text = "Ngày đặt: " + dateFormat.format(order.createdAt.toDate()),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (order.status == "COMPLETED") {
                        Button(
                            onClick = { viewModel.reviewOrder(order.id) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor
                            ),
                            shape = RoundedCornerShape(50) // Pill shape
                        ) {
                            Text("Đánh giá", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        "PENDING" -> "Chờ xác nhận" to PrimaryColor
        "PROCESSING" -> "Đang chế biến" to Color(0xFFF97316) // Orange
        "DELIVERING" -> "Đang giao" to Color(0xFF3B82F6) // Blue
        "COMPLETED" -> "Đã hoàn thành" to SuccessGreen
        "CANCELLED" -> "Đã hủy" to Color.Red
        else -> status to Color.Gray
    }
}
