package com.muatrenthenang.resfood.ui.screens.settings.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.User
import com.muatrenthenang.resfood.ui.viewmodel.UserViewModel

private val BgColor = Color(0xFF0F1923)
private val CardColor = Color(0xFF16202A)
private val BlueAccent = Color(0xFF4FA5F5)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    userViewModel: UserViewModel
) {
    val userState by userViewModel.userState.collectAsState()
    val user = userState ?: User()
    Scaffold(
        containerColor = BgColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Hồ sơ cá nhân", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { /* Save action */ }) {
                    Text("Lưu", color = BlueAccent)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Gray))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(BlueAccent)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Change Avatar", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thành viên ${user.rank}", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin cá nhân
            SectionTitle("Thông tin cá nhân")
            ProfileTextField(label = "Họ và tên", value = user.fullName, icon = Icons.Default.Person)
            ProfileTextField(label = "Số điện thoại", value = user.phone ?: "Chưa cập nhật", icon = Icons.Default.Phone)
            ProfileTextField(label = "Email", value = user.email, icon = Icons.Default.Email)
            ProfileTextField(label = "Địa chỉ giao hàng", value = user.address ?: "Chưa cập nhật", icon = Icons.Default.LocationOn)

            Spacer(modifier = Modifier.height(24.dp))

            // Cài đặt tài khoản
            SectionTitle("Cài đặt tài khoản")
            Column(modifier = Modifier.background(CardColor, RoundedCornerShape(12.dp))) {
                SettingRow(icon = Icons.Default.Lock, title = "Đổi mật khẩu")
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                SettingRow(icon = Icons.Default.ShoppingCart, title = "Ví Voucher")
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                SettingRow(icon = Icons.Default.LocationOn, title = "Quản lý địa chỉ")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            OutlinedButton(
                onClick = { /* Logout Logic Here */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    /* Delete Account Logic */
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF4B4B), containerColor = Color(0xFF1E0F0F))
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xóa tài khoản")
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun ProfileTextField(label: String, value: String, icon: ImageVector) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(value, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun SettingRow(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp).background(Color(0xFF23303E), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}