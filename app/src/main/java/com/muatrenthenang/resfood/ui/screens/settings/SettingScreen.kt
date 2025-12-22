package com.muatrenthenang.resfood.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.R
import com.muatrenthenang.resfood.ui.viewmodel.SettingsViewModel


// Màu sắc định nghĩa theo hình ảnh
private val BackgroundColor = Color(0xFF0F1923)
private val CardBackgroundColor = Color(0xFF16202A)
private val TextColorPrimary = Color.White
private val TextColorSecondary = Color.Gray
private val RedColor = Color(0xFFFF4B4B)


@Composable
fun SettingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit, // Chức năng xem hồ sơ
    viewModel: SettingsViewModel = viewModel()
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            SettingsTopBar(onBack = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Trung tâm tài khoản (Account Center)
            AccountCenterCard(
                name = viewModel.userName,
                rank = viewModel.userRank,
                onProfileClick = onNavigateToProfile
            )

            // 2. Các mục giao diện tĩnh (Hiển thị & Ngôn ngữ) - Visual only
            SectionHeader(title = "HIỂN THỊ & NGÔN NGỮ")
            SettingItemRow(title = "Ngôn ngữ", subtitle = "Tiếng Việt", showArrow = true)
            SettingToggleRow(title = "Chế độ tối", subtitle = "Giảm mỏi mắt vào ban đêm", checked = true)

            // 3. Thông báo - Visual only
            SectionHeader(title = "THÔNG BÁO")
            SettingToggleRow(title = "Thông báo đẩy", checked = false)
            SettingToggleRow(title = "Cập nhật đơn hàng", checked = true)
            SettingToggleRow(title = "Khuyến mãi & Ưu đãi", checked = false)

            // 4. Sức khỏe - Visual only
            SectionHeader(title = "SỨC KHỎE", badge = "MỚI")
            SettingToggleRow(title = "Nhắc nhở ăn uống", subtitle = "Giúp duy trì thói quen lành mạnh", checked = true)

            // Slider giả lập cho giống hình
            Slider(value = 0.3f, onValueChange = {}, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1 GIỜ", color = TextColorSecondary, fontSize = 12.sp)
                Text("12 GIỜ", color = TextColorSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Nút Đăng xuất
            Button(
                onClick = {
                    viewModel.logout(onLogoutSuccess = onNavigateToLogin)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2933)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedColor.copy(alpha = 0.5f))
            ) {
                Text(text = "Đăng xuất", color = RedColor, fontWeight = FontWeight.Bold)
            }

            // Footer Version
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ResFood cho Android", color = TextColorSecondary, fontSize = 12.sp)
                Text("Phiên bản 0.0.0 (Build 2025)", color = TextColorSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cài đặt & Tùy chỉnh",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AccountCenterCard(name: String, rank: String, onProfileClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Placeholder)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // Thay bằng Image thật khi có resource
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Dùng tạm icon mặc định
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = TextColorPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = rank, color = TextColorSecondary, fontSize = 14.sp)
            }

            // Nút Hồ sơ
            Button(
                onClick = onProfileClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3A47)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Hồ sơ", color = Color(0xFF4FA5F5), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, badge: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(text = title, color = TextColorSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFF1B5E20),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badge,
                    color = Color(0xFF4CAF50),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SettingItemRow(title: String, subtitle: String? = null, showArrow: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, color = TextColorPrimary, fontSize = 16.sp)
                if (subtitle != null) {
                    Text(text = subtitle, color = TextColorSecondary, fontSize = 14.sp)
                }
            }
            if (showArrow) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextColorSecondary)
            }
        }
    }
}

@Composable
fun SettingToggleRow(title: String, subtitle: String? = null, checked: Boolean) {
    var isChecked by remember { mutableStateOf(checked) }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextColorPrimary, fontSize = 16.sp)
                if (subtitle != null) {
                    Text(text = subtitle, color = TextColorSecondary, fontSize = 12.sp)
                }
            }
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4FA5F5),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}