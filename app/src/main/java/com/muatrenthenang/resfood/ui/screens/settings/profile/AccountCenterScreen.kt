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

// Màu sắc
private val BgColor = Color(0xFF0F1923)
private val CardColor = Color(0xFF16202A)
private val GoldColor = Color(0xFFFFC107)
private val BlueAccent = Color(0xFF4FA5F5)

@Composable
fun AccountCenterScreen(
    onBack: () -> Unit,
    onNavigateToDetails: () -> Unit,
    userViewModel: UserViewModel
) {
    val userState by userViewModel.userState.collectAsState()
    val user = userState ?: User(fullName = "...", points = 0, rank = "...")
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Trung tâm tài khoản",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
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
            UserInfoHeader(user = user)

            // 2. Menu Options
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))) {
                MenuOptionItem(icon = Icons.Default.Person, title = "Thông tin chi tiết tài khoản", onClick = onNavigateToDetails)
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                MenuOptionItem(icon = Icons.Default.ShoppingCart, title = "Thống kê chi tiêu", onClick = {})
                HorizontalDivider(thickness = 1.dp, color = BgColor)
                MenuOptionItem(icon = Icons.Default.DateRange, title = "Lịch sử đơn hàng", onClick = {})
            }

            // 3. Rank Status Card
            RankStatusCard(user = user)

            // 4. Benefits List
            Text("Quyền lợi hạng ${user.rank}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            BenefitItem(icon = Icons.Default.Star, title = "Tích điểm 5%", desc = "Nhận lại 5% giá trị mỗi đơn hàng vào ví.")
            BenefitItem(icon = Icons.Default.DateRange, title = "Ưu tiên đặt bàn", desc = "Được ưu tiên giữ chỗ vào giờ cao điểm.")
            BenefitItem(icon = Icons.Default.ShoppingCart, title = "Freeship dưới 5km", desc = "Miễn phí giao hàng cho mọi đơn đặt món < 5km.")

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun UserInfoHeader(user: User) {
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
            Text(user.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Thành viên ${user.rank}", color = GoldColor, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color(0xFF1E2A38), shape = RoundedCornerShape(4.dp)) {
                    Text("${user.points} điểm", color = BlueAccent, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
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

// Hàm bổ trợ để lấy thông tin hiển thị theo Rank
@Composable
fun getRankDisplayInfo(rank: String): RankInfo {
    return when (rank) {
        "Kim Cương" -> RankInfo(
            color = Color(0xFFB9F2FF),
            nextRank = "Tối đa",
            targetPoints = 0
        )
        "Vàng" -> RankInfo(
            color = Color(0xFFFFC107),
            nextRank = "Kim Cương",
            targetPoints = 3000
        )
        else -> RankInfo( // Mặc định là Bạc
            color = Color(0xFFC0C0C0),
            nextRank = "Vàng",
            targetPoints = 1000
        )
    }
}

data class RankInfo(val color: Color, val nextRank: String, val targetPoints: Int)
@Composable
fun RankStatusCard(user: User) {
    // Logic tính toán đơn giản (Ví dụ)
    //val nextRankPoints = 3000
    //val progress = if(nextRankPoints > 0) user.points.toFloat() / nextRankPoints else 0f

    val info = getRankDisplayInfo(user.rank)
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CẤP BẬC HIỆN TẠI", color = Color.Gray, fontSize = 10.sp)
                    Text(user.rank, color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldColor)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Hiển thị tiến độ
            if (user.rank != "Kim Cương") {
                val progress = (user.points.toFloat() / info.targetPoints).coerceIn(0f, 1f)

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Tiến độ lên ${info.nextRank}", color = Color.White, fontSize = 12.sp)
                    Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = info.color,
                    trackColor = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(12.dp))
                val pointsNeeded = info.targetPoints - user.points
                Text("Bạn cần thêm $pointsNeeded điểm để thăng hạng.", color = Color.LightGray, fontSize = 12.sp)
            } else {
                Text("Bạn đã đạt cấp bậc cao nhất!", color = info.color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
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