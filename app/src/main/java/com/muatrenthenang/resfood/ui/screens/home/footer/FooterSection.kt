package com.muatrenthenang.resfood.ui.screens.home.footer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun FooterSection(
    onNavigateToSettings: () -> Unit,
    currentRoute: String = "home"
) {
    NavigationBar(
        containerColor = Color(0xFF16202A), // Màu tối cho footer phù hợp với theme
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") },
            selected = currentRoute == "home",
            onClick = { /* Xử lý chuyển trang chủ */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Đơn hàng") },
            label = { Text("Đơn hàng") },
            selected = false,
            onClick = { /* Xử lý chuyển trang đơn hàng */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Yêu thích") },
            label = { Text("Yêu thích") },
            selected = false,
            onClick = { /* Xử lý chuyển trang yêu thích */ }
        )
        // Nút Cài đặt thay thế cho nút Tài khoản
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
            label = { Text("Cài đặt") },
            selected = false,
            onClick = { onNavigateToSettings() } // Gọi lambda để điều hướng sang trang cài đặt
        )
    }
}