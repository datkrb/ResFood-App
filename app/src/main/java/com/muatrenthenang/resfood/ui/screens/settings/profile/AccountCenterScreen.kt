package com.muatrenthenang.resfood.ui.screens.settings.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Màu sắc
private val BgColor = Color(0xFF0F1923)
private val CardColor = Color(0xFF16202A)
private val GoldColor = Color(0xFFFFC107)
private val BlueAccent = Color(0xFF4FA5F5)

@Composable
fun AccountCenterScreen(
    onBack: () -> Unit,
    onNavigateToDetails: () -> Unit
) {
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
                Text(
                    text = "Trung tâm tài khoản",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Đặt món ngay để thăng hạng ->", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Info
            UserInfoHeader()

            // 2. Menu Options
            Column(modifier = Modifier.background(CardColor, RoundedCornerShape(16.dp))) {
                MenuOptionItem(icon = Icons.Default.Person, title = "Thông tin chi tiết tài khoản", onClick = onNavigateToDetails)
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                MenuOptionItem(icon = Icons.Default.ShoppingCart, title = "Thống kê chi tiêu", onClick = {})
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                MenuOptionItem(icon = Icons.Default.DateRange, title = "Lịch sử đơn hàng", onClick = {})
            }

            // 3. Rank Status Card
            RankStatusCard()

            // 4. Benefits List
            Text("Quyền lợi hạng Vàng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            BenefitItem(icon = Icons.Default.Star, title = "Tích điểm 5%", desc = "Nhận lại 5% giá trị mỗi đơn hàng vào ví.")
            BenefitItem(icon = Icons.Default.DateRange, title = "Ưu tiên đặt bàn", desc = "Được ưu tiên giữ chỗ vào giờ cao điểm.")
            BenefitItem(icon = Icons.Default.ShoppingCart, title = "Freeship dưới 5km", desc = "Miễn phí giao hàng cho mọi đơn đặt món < 5km.")

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun UserInfoHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) // Avatar placeholder

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text("Nguyễn Văn A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Thành viên Vàng", color = GoldColor, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color(0xFF1E2A38), shape = RoundedCornerShape(4.dp)) {
                    Text("1,250 điểm", color = BlueAccent, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
fun MenuOptionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun RankStatusCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CẤP BẬC HIỆN TẠI", color = Color.Gray, fontSize = 10.sp)
                    Text("Vàng", color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldColor)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Tiến độ lên Bạch Kim", color = Color.White, fontSize = 12.sp)
                Text("60%", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = GoldColor,
                trackColor = Color.DarkGray,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Bạn cần chi tiêu thêm 2.000.000đ để thăng hạng.", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(CardColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFF1E2A38), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color.Gray, fontSize = 12.sp)
        }
    }
}