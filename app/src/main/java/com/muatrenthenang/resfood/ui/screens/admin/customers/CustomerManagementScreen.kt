package com.muatrenthenang.resfood.ui.screens.admin.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.data.model.User
import coil.compose.AsyncImage

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Filtering Logic
    val filteredCustomers = customers.filter { user ->
        val query = searchQuery.trim().lowercase()
        user.fullName.lowercase().contains(query) || 
        (user.phone?.contains(query) == true)
    }

    // Stats Logic
    val totalCustomers = customers.size
    // Roughly estimate new customers this month (mock logic if date parsing is complex or just check if recent)
    // For now assuming 10% are new if no date field easily accessible or verify createdAt
    val newCustomers = customers.filter { 
        // Simple check: Created within last 30 days. 
        // createdAt is Long timestamp
        val diff = System.currentTimeMillis() - it.createdAt
        diff < 30L * 24 * 60 * 60 * 1000
    }.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý khách hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}, modifier = Modifier.background(Color(0xFF2196F3), CircleShape)) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E2126)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column {
            // Stats Header
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Tổng khách",
                    value = "$totalCustomers",
                    badge = "Thành viên",
                    icon = Icons.Default.Person,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Tháng này",
                    value = "+$newCustomers",
                    badge = "Mới",
                    icon = Icons.Default.Add,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search & Filter
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(Color(0xFF2C3038), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                         Spacer(modifier = Modifier.width(8.dp))
                         androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Tìm tên hoặc số điện thoại...", color = Color.Gray)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(text = "Tất cả", isSelected = true)
                Chip(text = "VIP Gold", isSelected = false)
                Chip(text = "VIP Silver", isSelected = false)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // List Header
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Danh sách ($totalCustomers)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Sắp xếp", color = Color(0xFF2196F3))
            }

            // List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCustomers) { customer ->
                    CustomerItem(customer)
                }
            }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, badge: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(40.dp).background(color.copy(alpha=0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Text(badge, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha=0.2f), RoundedCornerShape(8.dp)).padding(horizontal=6.dp, vertical=2.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.Gray, fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Chip(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .background(if(isSelected) Color.White else Color(0xFF2C3038), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = if(isSelected) Color.Black else Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomerItem(customer: User) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(30.dp) // High rounded corners as per mock
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                // Mock Avatar
                AsyncImage(model = customer.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Text(customer.fullName, color = Color.White, fontWeight = FontWeight.Bold)
                     Text("${customer.points} Điểm", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold) 
                }
                Text(customer.phone ?: "Chưa có SĐT", color = Color.Gray, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    val badgeColor = when(customer.rank) {
                        "VIP GOLD" -> Color(0xFFFFC107)
                        "SILVER" -> Color(0xFF9E9E9E)
                        "THÀNH VIÊN" -> Color(0xFF607D8B)
                        else -> Color(0xFF4CAF50)
                    }
                    Text(customer.rank, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(badgeColor.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• Tham gia: ${java.text.SimpleDateFormat("MM/yyyy").format(java.util.Date(customer.createdAt))}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }
        }
    }
}
