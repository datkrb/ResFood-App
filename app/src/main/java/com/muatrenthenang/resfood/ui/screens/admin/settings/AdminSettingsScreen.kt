package com.muatrenthenang.resfood.ui.screens.admin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

import com.muatrenthenang.resfood.ui.components.AdminBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
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
                currentRoute = "admin_settings",
                onHomeClick = onNavigateToHome,
                onMenuClick = onNavigateToMenu,
                onAnalyticsClick = onNavigateToAnalytics,
                onSettingsClick = { /* Already here */ },
                onFabClick = onNavigateToOrders
            )
        },
        containerColor = Color(0xFF1E2126)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // Account Section
            Text("Tài khoản", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SettingsItem(
                icon = Icons.Default.Person, 
                title = "Thông tin cá nhân", 
                onClick = { 
                     android.widget.Toast.makeText(context, "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show() 
                }
            )
            SettingsItem(
                icon = Icons.Default.Security, 
                title = "Đổi mật khẩu", 
                onClick = { 
                    android.widget.Toast.makeText(context, "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show() 
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // App Settings
            Text("Ứng dụng", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SettingsItem(
                icon = Icons.Default.Notifications, 
                title = "Thông báo", 
                onClick = { 
                    android.widget.Toast.makeText(context, "Chức năng đang phát triển", android.widget.Toast.LENGTH_SHORT).show() 
                }, 
                showBadge = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3038)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector, 
    title: String, 
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C3038), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBadge) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, androidx.compose.foundation.shape.CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
