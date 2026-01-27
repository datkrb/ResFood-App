package com.muatrenthenang.resfood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminBottomNavigation(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // Height to accommodate FAB overlap
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF1E2126)) // Dark background
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon(
                icon = Icons.Default.Dashboard,
                label = "Home",
                isSelected = currentRoute == "admin_dashboard",
                onClick = onHomeClick
            )
            NavIcon(
                icon = Icons.Default.Apps,
                label = "Management",
                isSelected = currentRoute == "admin_management" || currentRoute == "admin_food_management", // Keep sub-routes highlighted if desired, or strictly check
                onClick = onMenuClick
            )
            Spacer(modifier = Modifier.width(48.dp)) // Space for FAB
            NavIcon(
                icon = Icons.Default.Assessment,
                label = "Analytics",
                isSelected = currentRoute == "admin_analytics",
                onClick = onAnalyticsClick
            )
            NavIcon(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == "admin_settings",
                onClick = onSettingsClick
            )
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp) // Adjust based on height
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFFFC107)) // Yellow/Orange accent
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Orders",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun NavIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) Color(0xFFFFC107) else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color)
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(color, CircleShape)
            )
        }
    }
}
