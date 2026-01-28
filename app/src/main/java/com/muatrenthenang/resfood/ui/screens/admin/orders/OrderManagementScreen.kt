package com.muatrenthenang.resfood.ui.screens.admin.orders

import com.muatrenthenang.resfood.data.model.Order
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.theme.LightRed

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    // Tabs matching User Order Screen style but for Admin
    val tabs = listOf("PENDING", "PROCESSING", "DELIVERING", "COMPLETED", "CANCELLED", "REJECTED", "ALL")
    val tabTitles = listOf("Chờ duyệt", "Đang chế biến", "Đang giao", "Hoàn thành", "Đã hủy", "Đã từ chối", "Tất cả")
    
    var selectedTabIndex by remember { mutableStateOf(0) } // Default to PENDING
    var selectedDateFilter by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }

    // Filter Logic
    val filteredOrders = orders.filter { order ->
        val selectedStatus = tabs[selectedTabIndex]
        val matchesStatus = when(selectedStatus) {
             "ALL" -> true
             else -> order.status == selectedStatus
        }
        
        val matchesDate = when(selectedDateFilter) {
            "Hôm nay" -> {
                val diff = System.currentTimeMillis() - (order.createdAt?.toDate()?.time ?: 0)
                diff < 24 * 60 * 60 * 1000
            }
            "Tuần này" -> {
                val diff = System.currentTimeMillis() - (order.createdAt?.toDate()?.time ?: 0)
                diff < 7 * 24 * 60 * 60 * 1000
            }
            else -> true
        }

        val matchesSearch = if(searchQuery.isBlank()) true else {
            order.id.contains(searchQuery, ignoreCase = true) ||
            order.userName.contains(searchQuery, ignoreCase = true) ||
            order.userPhone.contains(searchQuery)
        }
        matchesStatus && matchesDate && matchesSearch
    }.sortedByDescending { it.createdAt }

    // State for Reject Dialog
    var showRejectDialog by remember { mutableStateOf(false) }
    var orderToReject by remember { mutableStateOf<Order?>(null) }
    val context = LocalContext.current

    if (showRejectDialog && orderToReject != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối đơn hàng?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn từ chối đơn hàng #${orderToReject?.id?.takeLast(5)?.uppercase()} không?") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToReject?.let { order ->
                            viewModel.rejectOrder(order.id) {
                                Toast.makeText(context, "Đã từ chối đơn hàng thành công", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showRejectDialog = false
                        orderToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed)
                ) {
                    Text("Từ chối", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            
            Column {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // Date Filter Chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tất cả", "Hôm nay", "Tuần này").forEach { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { selectedDateFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
            
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
                        "ALL" -> orders.size
                        else -> orders.count { it.status == status }
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
                                    color = if (selectedTabIndex == index) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                                // Badge with count
                                if (count > 0) {
                                    val badgeColor = when(status) {
                                        "PENDING" -> PrimaryColor
                                        "PROCESSING" -> Color(0xFFFF9800)
                                        "DELIVERING" -> Color(0xFF2196F3)
                                        "COMPLETED" -> SuccessGreen
                                        "CANCELLED", "REJECTED" -> LightRed
                                        else -> MaterialTheme.colorScheme.primary
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
                       val headerTitle = tabTitles[selectedTabIndex]
                       Text(headerTitle, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                       Text("${filteredOrders.size} Đơn", color = LightRed, fontWeight = FontWeight.Bold)
                   }
                }

                if (filteredOrders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Không có đơn hàng nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(filteredOrders) { order ->
                    val customer = customers.find { it.id == order.userId }
                    OrderItem(
                        order = order, 
                        userAvatar = customer?.avatarUrl,
                        onClick = { onNavigateToDetail(order.id) },
                        onAccept = { 
                            viewModel.approveOrder(order.id) {
                                Toast.makeText(context, "Đã duyệt đơn hàng thành công", Toast.LENGTH_SHORT).show()
                            } 
                        },
                        onReject = { 
                            orderToReject = order
                            showRejectDialog = true
                        }
                    )
                }
            }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        // Replaced custom text with standard BasicTextField or TextField for input
        // Since original was custom, let's use transparent TextField
        androidx.compose.foundation.text.BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text("Tìm mã đơn, tên khách, SĐT...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun OrderItem(
    order: Order, 
    userAvatar: String? = null,
    onClick: () -> Unit, 
    onAccept: () -> Unit, 
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    // Avatar
                    if (userAvatar != null) {
                         AsyncImage(
                            model = userAvatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                           modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                           contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = order.userName.firstOrNull()?.toString()?.uppercase() ?: "K",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(order.userName.ifEmpty { "Khách lẻ" }, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        val date = if(order.createdAt != null) {
                             java.text.SimpleDateFormat("HH:mm").format(order.createdAt.toDate())
                        } else "Vừa xong"
                        Text(date + " • #" + order.id.takeLast(6).uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                
                // Status Badge
                val (badgeBg, badgeText) = when(order.status) {
                    "PENDING" -> PrimaryColor.copy(alpha = 0.2f) to PrimaryColor
                    "PROCESSING" -> Color(0xFFFF9800).copy(alpha = 0.2f) to Color(0xFFFF9800)
                    "DELIVERING" -> Color(0xFF2196F3).copy(alpha = 0.2f) to Color(0xFF2196F3)
                    "COMPLETED" -> SuccessGreen.copy(alpha = 0.2f) to SuccessGreen
                    "REJECTED", "CANCELLED" -> LightRed.copy(alpha = 0.2f) to LightRed
                    else -> SuccessGreen.copy(alpha = 0.2f) to SuccessGreen
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val statusLabel = when(order.status) {
                        "PENDING" -> "Mới"
                        "PROCESSING" -> "Đang làm"
                        "DELIVERING" -> "Đang giao"
                        "COMPLETED" -> "Xong"
                        "CANCELLED" -> "Hủy"
                        "REJECTED" -> "Từ chối"
                        else -> order.status
                    }
                    Text(statusLabel, color = badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Items - Assuming all are food items for now
            order.items.take(2).forEach { item ->
                Text("${item.quantity}x ${item.foodName}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            }
            if(order.items.size > 2) {
                Text("+ ${order.items.size - 2} món khác", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer: Button Action or Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng tiền", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    val formattedTotal = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")).format(order.total)
                    Text(formattedTotal, color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (order.status == "PENDING") {
                   Row(
                       horizontalArrangement = Arrangement.spacedBy(12.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       // Reject Button (Icon only or small text)
                       Box(
                           modifier = Modifier
                               .size(40.dp)
                               .clip(CircleShape)
                               .background(Color.White.copy(alpha = 0.1f))
                               .clickable { onReject() },
                           contentAlignment = Alignment.Center
                       ) {
                           Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.LightGray)
                       }
                       
                       // Approve Button
                       Button(
                           onClick = onAccept,
                           colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
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
