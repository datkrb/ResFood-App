package com.muatrenthenang.resfood.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation
import com.muatrenthenang.resfood.ui.viewmodel.admin.ActivityItem
import com.muatrenthenang.resfood.ui.viewmodel.admin.ActivityType
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.DashboardUiState

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToFoodManagement: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToPromo: () -> Unit,
    onNavigateToTables: () -> Unit
) {
    val state by viewModel.dashboardUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        bottomBar = {
            AdminBottomNavigation(
                currentRoute = "admin_dashboard",
                onHomeClick = { /* Already here */ },
                onMenuClick = onNavigateToFoodManagement,
                onAnalyticsClick = onNavigateToAnalytics,
                onSettingsClick = onNavigateToSettings,
                onFabClick = onNavigateToOrders // FAB navigates to Orders
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item { TopAppBarSection() }
                item { TimeFilterSection(state.timeRange, viewModel::setTimeRange) }
                item { StatsHeroSection(state, onNavigateToAnalytics, onNavigateToOrders) }
                item { QuickActionsSection(onNavigateToFoodManagement, onNavigateToPromo, onNavigateToCustomers, onNavigateToTables) }
                item { OperationsStatusSection(state, onNavigateToOrders, onNavigateToTables, onNavigateToFoodManagement) }
                item { RecentActivitySection(state.recentActivities) }
            }
        }
    }
}

@Composable
fun TopAppBarSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCN02OkVjM1u60eYtOcHishKvFfwNDi4JLZ21f9ksJll58KkPuwLQbwwU_sZ_XyKZQPu-bI6Mno-5a_BB0JRIMJ9FL-Kx28KcFmTIcxQSaliizHyTCzenR7XYVeEQ_5rD8SDTwgQMQyo62WH09mIL4Yfuj9H9l2O3fmvghHHD95JL-NvUi2GPyn0JwZ23NlFgE8zOetYjoZSevbDPYQUOrweA8wGGdt1d5yal9fLp8GEgZPihG_5GsHGdi5IGwHCpFax5tGZ9zDQW0",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Admin User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun TimeFilterSection(selectedRange: String, onRangeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeFilterButton(text = "Today", isSelected = selectedRange == "Today", onClick = { onRangeSelected("Today") })
        TimeFilterButton(text = "This Week", isSelected = selectedRange == "This Week", onClick = { onRangeSelected("This Week") })
        TimeFilterButton(text = "This Month", isSelected = selectedRange == "This Month", onClick = { onRangeSelected("This Month") })
    }
}

@Composable
fun TimeFilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.33f)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatsHeroSection(
    state: DashboardUiState,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .clickable { onNavigateToAnalytics() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+${state.revenueGrowth}%",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Column {
                    Text(text = "Total Revenue", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = "$${state.totalRevenue}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Orders Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .clickable { onNavigateToOrders() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${state.newOrdersCount} New",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Column {
                    Text(text = "New Orders", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(
                        text = "${state.newOrders}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToFoodManagement: () -> Unit,
    onNavigateToPromo: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToTables: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // "Add Item" -> Food Management
                QuickActionButton(
                    icon = Icons.Default.AddCircle,
                    label = "Add Item",
                    color = Color(0xFF2196F3), // Blue
                    onClick = onNavigateToFoodManagement
                )
            }
            item {
                QuickActionButton(
                    icon = Icons.Default.Campaign,
                    label = "Promo",
                    color = Color(0xFF9C27B0), // Purple
                    onClick = onNavigateToPromo
                )
            }
            item {
                QuickActionButton(
                    icon = Icons.Default.Badge,
                    label = "Staff/Cust", // Changed label slightly or keep generic
                    color = Color(0xFFFF9800), // Orange
                    onClick = onNavigateToCustomers
                )
            }
            item {
                QuickActionButton(
                    icon = Icons.Default.TableBar,
                    label = "Tables",
                    color = Color(0xFF009688), // Teal
                    onClick = onNavigateToTables
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun OperationsStatusSection(
    state: DashboardUiState,
    onNavigateToOrders: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToFoodManagement: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Operations",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationCard(
                    modifier = Modifier.weight(1f).clickable { onNavigateToOrders() },
                    icon = Icons.Default.HourglassTop,
                    color = Color(0xFFFFC107), // Yellow
                    title = "Pending",
                    subtitle = "${state.pendingOrders} Orders"
                )
                OperationCard(
                    modifier = Modifier.weight(1f).clickable { onNavigateToOrders() },
                    icon = Icons.Default.Kitchen, // Skillet替代
                    color = Color(0xFF2196F3), // Blue
                    title = "Processing",
                    subtitle = "${state.processingOrders} Orders"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationCard(
                    modifier = Modifier.weight(1f).clickable { onNavigateToTables() },
                    icon = Icons.Default.EventSeat,
                    color = Color(0xFF9C27B0), // Purple
                    title = "Reservations",
                    subtitle = "${state.reservations} Tables"
                )
                OperationCard(
                    modifier = Modifier.weight(1f).clickable { onNavigateToFoodManagement() },
                    icon = Icons.Default.ProductionQuantityLimits,
                    color = Color(0xFFF44336), // Red
                    title = "Out of Stock",
                    subtitle = "${state.outOfStockItems} Items"
                )
            }
        }
    }
}

@Composable
fun OperationCard(modifier: Modifier = Modifier, icon: ImageVector, color: Color, title: String, subtitle: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<ActivityItem>) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 80.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = { }) {
                Text(text = "View All")
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activities.forEach { activity ->
                ActivityItemRow(activity)
            }
        }
    }
}

@Composable
fun ActivityItemRow(item: ActivityItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when (item.type) {
                ActivityType.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                ActivityType.PURPLE -> Icons.Default.CalendarMonth to Color(0xFF9C27B0)
                ActivityType.WARNING -> Icons.Default.ReceiptLong to Color(0xFFFFC107)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = item.value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}



@Preview
@Composable
fun AdminDashboardPreview() {
    AdminDashboardScreen(
        onNavigateToFoodManagement = {},
        onNavigateToMenu = {},
        onNavigateToAnalytics = {},
        onNavigateToSettings = {},
        onNavigateToOrders = {},
        onNavigateToCustomers = {},
        onNavigateToPromo = {},
        onNavigateToTables = {},
        viewModel = AdminViewModel()
    )
}
