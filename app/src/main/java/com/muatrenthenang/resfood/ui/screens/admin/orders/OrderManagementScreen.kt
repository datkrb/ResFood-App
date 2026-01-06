package com.muatrenthenang.resfood.ui.screens.admin.orders

import com.muatrenthenang.resfood.data.model.Order
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf("Tất cả") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126), // Dark bg match dashboard
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E2126)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            SearchBar()
            
            // Filters
            FilterTabs(selectedFilter) { selectedFilter = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Order List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header for Section
                item {
                   Row(
                       modifier = Modifier.fillMaxWidth(), 
                       horizontalArrangement = Arrangement.SpaceBetween,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text("Đơn mới cần xử lý", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                       Text("${orders.size} Đơn", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                   }
                }

                items(orders) { order ->
                    OrderItem(order, onClick = { onNavigateToDetail(order.id) })
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp)
            .background(Color(0xFF2C3038), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Tìm mã đơn, tên khách, SĐT...", color = Color.Gray)
    }
}

@Composable
fun FilterTabs(selected: String, onSelect: (String) -> Unit) {
    val filters = listOf("Tất cả", "Mới", "Chờ duyệt", "Đang giao")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            val bgColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF2C3038)
            val textColor = if (isSelected) Color.White else Color.Gray
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(filter, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    // Avatar Placeholder
                    Box(
                       modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(order.userName.ifEmpty { "Khách lẻ" }, color = Color.White, fontWeight = FontWeight.Bold)
                        val date = if(order.createdAt != null) {
                             java.text.SimpleDateFormat("HH:mm").format(order.createdAt.toDate())
                        } else "Vừa xong"
                        Text(date + " • #" + order.id.takeLast(6), color = Color.Gray, fontSize = 12.sp)
                    }
                }
                
                // Status Badge
                val (badgeBg, badgeText) = when(order.status) {
                    "PENDING" -> Color(0xFF2196F3).copy(alpha = 0.2f) to Color(0xFF2196F3)
                    "PROCESSING" -> Color(0xFFFF9800).copy(alpha = 0.2f) to Color(0xFFFF9800)
                    "COMPLETED" -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
                    "REJECTED", "CANCELLED" -> Color(0xFFF44336).copy(alpha = 0.2f) to Color(0xFFF44336)
                    else -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(order.status, color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Items - Assuming all are food items for now
            order.items.take(2).forEach { item ->
                Text("${item.quantity}x ${item.foodName}", color = Color.LightGray, fontSize = 14.sp)
            }
            if(order.items.size > 2) {
                Text("+ ${order.items.size - 2} món khác", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer: Button Action or Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng tiền", color = Color.Gray, fontSize = 12.sp)
                    Text("${order.total}đ", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (order.status == "PENDING") {
                   Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                       // Reject Button (Icon only or small text)
                       Box(
                           modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                           contentAlignment = Alignment.Center
                       ) {
                           Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.LightGray)
                       }
                       
                       // Approve Button
                       Button(
                           onClick = { },
                           colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                           shape = RoundedCornerShape(20.dp)
                       ) {
                           Text("Duyệt")
                       }
                   }
                }
            }
        }
    }
}
