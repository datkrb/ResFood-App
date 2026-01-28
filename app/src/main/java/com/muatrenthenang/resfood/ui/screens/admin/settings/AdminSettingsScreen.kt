package com.muatrenthenang.resfood.ui.screens.admin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    // State
    // State
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.muatrenthenang.resfood.data.repository.SettingsRepository(context) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(settingsRepository.isNotificationsEnabled()) }
    var darkModeEnabled by remember { mutableStateOf(true) } // Default for Admin
    

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
                    containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker,
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
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Account Section
            Text("Tài khoản", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SettingsItem(
                icon = Icons.Default.Person, 
                title = "Thông tin cá nhân", 
                onClick = { showProfileDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Security, 
                title = "Đổi mật khẩu", 
                onClick = { showChangePasswordDialog = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // App Settings
            Text("Ứng dụng", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SettingsItem(
                icon = Icons.Default.Notifications, 
                title = "Thông báo", 
                onClick = { 
                    notificationsEnabled = !notificationsEnabled 
                    settingsRepository.setNotificationsEnabled(notificationsEnabled)
                },
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { 
                             notificationsEnabled = it
                             settingsRepository.setNotificationsEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor
                        )
                    )
                }
            )
            
             SettingsItem(
                icon = Icons.Default.DarkMode, 
                title = "Giao diện tối", 
                onClick = { darkModeEnabled = !darkModeEnabled },
                trailing = {
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                         colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor
                        )
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = com.muatrenthenang.resfood.ui.theme.LightRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", color = com.muatrenthenang.resfood.ui.theme.LightRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    if (showProfileDialog) {
        ProfileEditDialog(onDismiss = { showProfileDialog = false })
    }
    
    if (showChangePasswordDialog) {
        ChangePasswordDialog(onDismiss = { showChangePasswordDialog = false })
    }
}

@Composable
fun ProfileEditDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("Admin User") }
    var email by remember { mutableStateOf("admin@resfood.com") }
    var phone by remember { mutableStateOf("0909000111") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = com.muatrenthenang.resfood.ui.theme.PrimaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cập nhật thông tin")
            }
        },
        text = {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(com.muatrenthenang.resfood.ui.theme.SurfaceDarker, CircleShape)
                            .clickable { /* Pick Image */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = com.muatrenthenang.resfood.ui.theme.PrimaryColor, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ tên") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Gray,
                        disabledBorderColor = Color.DarkGray,
                        disabledLabelColor = Color.DarkGray
                    ),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor)) {
                Text("Lưu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default. Lock, contentDescription = null, tint = com.muatrenthenang.resfood.ui.theme.PrimaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đổi mật khẩu")
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mật khẩu hiện tại") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                     leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.Gray) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                     leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss, // Mock logic
                colors = ButtonDefaults.buttonColors(containerColor = com.muatrenthenang.resfood.ui.theme.PrimaryColor),
                enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword
            ) {
                Text("Đổi mật khẩu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector, 
    title: String, 
    onClick: () -> Unit,
    showBadge: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.muatrenthenang.resfood.ui.theme.SurfaceCard, RoundedCornerShape(12.dp))
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
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
