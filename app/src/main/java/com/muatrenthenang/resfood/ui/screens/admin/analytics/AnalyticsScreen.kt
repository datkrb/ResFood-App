package com.muatrenthenang.resfood.ui.screens.admin.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation
import com.muatrenthenang.resfood.ui.viewmodel.admin.AnalyticsUiState
import com.muatrenthenang.resfood.ui.viewmodel.admin.TopProductItem
import com.muatrenthenang.resfood.ui.components.DateRangeSelector
import com.muatrenthenang.resfood.ui.components.RevenueLineChart
import com.muatrenthenang.resfood.ui.components.OrderStatusPieChart

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val analyticsState by viewModel.analyticsUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê & Báo cáo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
             AdminBottomNavigation(
                currentRoute = "admin_analytics",
                onHomeClick = onNavigateToHome,
                onMenuClick = onNavigateToMenu,
                onAnalyticsClick = { /* Already here */ },
                onSettingsClick = onNavigateToSettings,
                onFabClick = onNavigateToOrders
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // 1. Date Range Filter
                item {
                    DateRangeSelector(
                        selectedType = analyticsState.filterType,
                        startDate = analyticsState.startDate,
                        endDate = analyticsState.endDate,
                        onTypeSelected = { type -> viewModel.setAnalyticsFilter(type = type) },
                        onDateRangeSelected = { start, end -> viewModel.setAnalyticsFilter(analyticsState.filterType, start, end) }
                    )
                }

                // 2. Summary Cards
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticSummaryCard(
                            "Tổng Doanh thu",
                            "${analyticsState.totalRevenue.toInt()}đ",
                            "${analyticsState.totalOrders} Đơn",
                            Color(0xFF4CAF50),
                            Modifier.weight(1f)
                        )
                        AnalyticSummaryCard(
                            "Doanh thu trung bình",
                            if (analyticsState.totalOrders > 0) "${(analyticsState.totalRevenue / analyticsState.totalOrders).toInt()}đ" else "0đ", 
                            "Đơn",
                            Color(0xFF2196F3),
                            Modifier.weight(1f)
                        )
                    }
                }

                // 3. Revenue Trend Chart
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Text("Biểu đồ doanh thu", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                         Card(
                            colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(300.dp)
                        ) {
                             RevenueLineChart(
                                 data = analyticsState.revenueChartData,
                                 modifier = Modifier.fillMaxSize()
                             )
                        }
                    }
                }
                
                // 4. Order Status Distribution (Pie Chart)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                         Text("Trạng thái đơn hàng", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                         Card(
                            colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                             OrderStatusPieChart(
                                 data = analyticsState.orderStatusData,
                                 modifier = Modifier.fillMaxWidth().padding(16.dp)
                             )
                        }
                    }
                }

                // 5. Top Products
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Món ăn bán chạy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038))) {
                            Column {
                                if(analyticsState.topProducts.isEmpty()) {
                                    Text("Chưa có dữ liệu", color = Color.Gray, modifier = Modifier.padding(16.dp))
                                } else {
                                    analyticsState.topProducts.forEachIndexed { index, item ->
                                        TopProductRow(
                                            name = "${index+1}. ${item.name}", 
                                            count = "${item.count} đơn", 
                                            revenue = "${item.revenue.toInt()}đ"
                                        )
                                        if(index < analyticsState.topProducts.size - 1) {
                                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticSummaryCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(subtitle, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TopProductRow(name: String, count: String, revenue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: Name (Priority, flexible width)
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.55f)
        )
        
        // Column 2: Order Count
        Text(
            text = count,
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(0.15f)
        )
        
        // Column 3: Revenue
        Text(
            text = revenue,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(0.3f)
        )
    }
}
