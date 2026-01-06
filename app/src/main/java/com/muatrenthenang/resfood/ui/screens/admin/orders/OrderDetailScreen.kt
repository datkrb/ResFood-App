package com.muatrenthenang.resfood.ui.screens.admin.orders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.R // Ensure appropriate resource imports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFF1E2126),
        bottomBar = {
            BottomActionBar()
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info: ID & Time
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("#RF-2938", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("10:30, 24 Tháng 10, 2023", color = Color.Gray)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chờ xác nhận", color = Color(0xFFFF9800), fontSize = 12.sp)
                    }
                }
            }

            // Customer Info Card
            item {
                Text("Thông tin khách hàng", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nguyễn Văn A", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("123 Đường Lê Lợi, Quận 1, TP.HCM", color = Color.Gray, fontSize = 12.sp)
                            Text("★ 4.8 (12 đơn hàng)", color = Color.Gray, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier.background(Color(0xFF2196F3).copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF2196F3))
                        }
                    }
                }
            }

            // Item List
            item {
                Text("Danh sách món (3)", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrderItemRow(name = "Phở Bò Đặc Biệt", price = "75.000đ", qty = "x1", note = "Không hành")
                    OrderItemRow(name = "Trà Đá", price = "10.000đ", qty = "x2", note = null)
                }
            }
            
            // Payment Info
            item {
                Text("Chi tiết thanh toán", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                 Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PaymentRow("Tạm tính", "95.000đ")
                        PaymentRow("Phí giao hàng (2.5km)", "15.000đ")
                        PaymentRow("Khuyến mãi", "-25.000đ", isDiscount = true)
                        Divider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng cộng", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("85.000đ", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3038), contentColor = Color(0xFFFF5252)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Từ chối")
            }
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Duyệt đơn")
            }
        }
    }
}

@Composable
fun OrderItemRow(name: String, price: String, qty: String, note: String?) {
    Card(
         colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
         shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.DarkGray))
             Spacer(modifier = Modifier.width(12.dp))
             Column(modifier = Modifier.weight(1f)) {
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                     Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                     Text(price, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                 }
                 if (note != null) {
                     Text(note, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.background(Color.White.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp))
                 }
                 Text("Số lượng: $qty", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
             }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, color = if(isDiscount) Color(0xFF4CAF50) else Color.White, fontWeight = FontWeight.Medium)
    }
}
