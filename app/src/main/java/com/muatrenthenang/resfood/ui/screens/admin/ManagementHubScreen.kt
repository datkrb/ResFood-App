package com.muatrenthenang.resfood.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementHubScreen(
    onNavigateToFoodManagement: () -> Unit,
    onNavigateToCategory: () -> Unit, // Potentially same as Food or separate
    onNavigateToPromo: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToTopping: () -> Unit,
    onNavigateToBranch: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReviews: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trung tâm quản lý", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AdminBottomNavigation(
                currentRoute = "admin_management",
                onHomeClick = onNavigateToHome,
                onMenuClick = { /* Already here */ },
                onAnalyticsClick = onNavigateToAnalytics,
                onSettingsClick = onNavigateToSettings,
                onFabClick = onNavigateToOrders
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Data Management (Promo, Cust, Food, Cat)
            Text(
                text = "Quản lý dữ liệu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ManagementHubItem(
                        title = "Khuyến mãi",
                        icon = Icons.Default.Campaign,
                        color = Color(0xFF9C27B0), // Purple
                        onClick = onNavigateToPromo
                    )
                }
                item {
                    ManagementHubItem(
                        title = "Khách hàng",
                        icon = Icons.Default.People,
                        color = Color(0xFFFF9800), // Orange
                        onClick = onNavigateToCustomers
                    )
                }
                item {
                    ManagementHubItem(
                        title = "Món ăn",
                        icon = Icons.Default.Fastfood,
                        color = Color(0xFF4CAF50), // Green
                        onClick = onNavigateToFoodManagement
                    )
                }
                item {
                    ManagementHubItem(
                        title = "Danh mục",
                        icon = Icons.Default.Category,
                        color = Color(0xFF2196F3), // Blue
                        onClick = onNavigateToCategory
                    )
                }
                item {
                     ManagementHubItem(
                        title = "Topping",
                        icon = Icons.Default.Icecream,
                        color = Color(0xFFE91E63), // Pink
                        onClick = onNavigateToTopping
                    )
                }
                item {
                     ManagementHubItem(
                        title = "Chi nhánh",
                        icon = Icons.Default.Store,
                        color = Color(0xFF795548), // Brown
                        onClick = onNavigateToBranch
                    )
                }

                item {
                    ManagementHubItem(
                        title = "Đánh giá",
                        icon = Icons.Default.Star,
                        color = Color(0xFFFFC107), // Amber
                        onClick = onNavigateToReviews
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Section 2: Operations (Orders, Tables)
            Text(
                text = "Vận hành",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ManagementHubLargeItem(
                    modifier = Modifier.weight(1f),
                    title = "Đơn hàng",
                    icon = Icons.Default.ReceiptLong,
                    color = Color(0xFFFFC107), // Amber
                    onClick = onNavigateToOrders
                )
                ManagementHubLargeItem(
                    modifier = Modifier.weight(1f),
                    title = "Đặt bàn",
                    icon = Icons.Default.TableRestaurant,
                    color = Color(0xFF009688), // Teal
                    onClick = onNavigateToTables
                )
            }
        }
    }
}

@Composable
fun ManagementHubItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ManagementHubLargeItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
