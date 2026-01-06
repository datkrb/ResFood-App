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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê & Báo cáo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticSummaryCard(
                        "Doanh thu ngày",
                        "12.500.000đ",
                        "+15%",
                        Color(0xFF4CAF50),
                        Modifier.weight(1f)
                    )
                    AnalyticSummaryCard(
                        "Chi phí",
                        "4.200.000đ",
                        "-5%",
                        Color(0xFFFF5252),
                        Modifier.weight(1f)
                    )
                }
            }

            // Chart Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Text("Biểu đồ doanh thu tuần", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                         SimpleBarChart()
                    }
                }
            }

            // Top Products
            item {
                Text("Món ăn bán chạy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038))) {
                    Column {
                        TopProductRow("1. Beefsteak sốt tiêu đen", "150 đơn", "45.000.000đ")
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        TopProductRow("2. Pizza Hải sản", "120 đơn", "24.000.000đ")
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        TopProductRow("3. Mì Ý Carbonara", "98 đơn", "11.760.000đ")
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticSummaryCard(title: String, value: String, growth: String, growthColor: Color, modifier: Modifier = Modifier) {
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
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = growthColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(growth, color = growthColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SimpleBarChart() {
    // Mock data: 7 days
    val data = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.7f)
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / (data.size * 2)
        val spacing = barWidth
        val maxBarHeight = size.height

        data.forEachIndexed { index, value ->
            val barHeight = maxBarHeight * value
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(
                    x = index * (barWidth + spacing) + spacing / 2,
                    y = maxBarHeight - barHeight
                ),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun TopProductRow(name: String, count: String, revenue: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = Color.White, fontWeight = FontWeight.Medium)
        Row {
             Text(count, color = Color.Gray, modifier = Modifier.padding(end = 12.dp))
             Text(revenue, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}
