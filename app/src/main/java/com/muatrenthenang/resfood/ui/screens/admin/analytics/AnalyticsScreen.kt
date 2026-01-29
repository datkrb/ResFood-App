package com.muatrenthenang.resfood.ui.screens.admin.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation
import com.muatrenthenang.resfood.ui.components.DateRangeSelector
import com.muatrenthenang.resfood.ui.components.OrderStatusPieChart
import com.muatrenthenang.resfood.ui.components.RevenueLineChart
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
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
                title = { 
                    Text(
                        stringResource(R.string.admin_analytics_title), 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
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
        containerColor = MaterialTheme.colorScheme.background
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
                            stringResource(R.string.admin_analytics_total_revenue),
                            stringResource(R.string.price_format_vnd, analyticsState.totalRevenue.toInt()),
                            "${analyticsState.totalOrders} ${stringResource(R.string.admin_analytics_orders_unit)}",
                            Color(0xFF4CAF50), // Green for revenue is standard
                            Modifier.weight(1f)
                        )
                        AnalyticSummaryCard(
                            stringResource(R.string.admin_analytics_avg_revenue),
                            stringResource(R.string.price_format_vnd, if (analyticsState.totalOrders > 0) (analyticsState.totalRevenue / analyticsState.totalOrders).toInt() else 0), 
                            stringResource(R.string.admin_analytics_orders_unit),
                            Color(0xFF2196F3), // Blue
                            Modifier.weight(1f)
                        )
                    }
                }

                // 3. Revenue Trend Chart
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Text(
                             stringResource(R.string.admin_analytics_revenue_chart), 
                             color = MaterialTheme.colorScheme.onBackground, 
                             fontWeight = FontWeight.Bold, 
                             modifier = Modifier.padding(bottom = 12.dp)
                         )
                         Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.9f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                         Text(
                             stringResource(R.string.admin_analytics_order_status), 
                             color = MaterialTheme.colorScheme.onBackground, 
                             fontWeight = FontWeight.Bold, 
                             modifier = Modifier.padding(bottom = 12.dp)
                         )
                         Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.9f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                        Text(
                            stringResource(R.string.admin_analytics_top_products), 
                            color = MaterialTheme.colorScheme.onBackground, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.9f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                if(analyticsState.topProducts.isEmpty()) {
                                    Text(
                                        stringResource(R.string.admin_analytics_no_data), 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                        modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    analyticsState.topProducts.forEachIndexed { index, item ->
                                        TopProductRow(
                                            name = "${index+1}. ${item.name}", 
                                            count = "${item.count} ${stringResource(R.string.admin_analytics_orders_unit)}", 
                                            revenue = stringResource(R.string.price_format_vnd, item.revenue.toInt())
                                        )
                                        if(index < analyticsState.topProducts.size - 1) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.55f)
        )
        
        // Column 2: Order Count
        Text(
            text = count,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.15f)
        )
        
        // Column 3: Revenue
        Text(
            text = revenue,
            color = Color(0xFF4CAF50), // Consistent green for sales money
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.3f)
        )
    }
}
