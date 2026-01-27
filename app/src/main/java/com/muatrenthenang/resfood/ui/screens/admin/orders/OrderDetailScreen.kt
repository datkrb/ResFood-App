package com.muatrenthenang.resfood.ui.screens.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.data.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    // In a real app, you might want to observe specific order flow, 
    // but here we find it from the loaded list to keep it simple.
    // If AdminViewModel is scoped correctly, checks list. If new instance, it loads list then finds.
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }

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
                    containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceDarker,
        bottomBar = {
            if (order != null && order.status == "PENDING") {
                BottomActionBar(
                    onReject = { 
                        viewModel.rejectOrder(order.id) 
                        onNavigateBack() // Or stay?
                    },
                    onApprove = { 
                        viewModel.approveOrder(order.id)
                        onNavigateBack()
                    }
                )
            } else if (order != null && order.status == "PROCESSING") {
                  Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { 
                            viewModel.completeOrder(order.id)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Hoàn thành đơn")
                    }
                }
            }
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if(orders.isEmpty()) CircularProgressIndicator() // Loading
                else Text("Không tìm thấy đơn hàng", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Info: ID & Time
                item {
                    val dateStr = if (order.createdAt != null) {
                        SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(order.createdAt.toDate())
                    } else "N/A"
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("#${order.id.takeLast(6).uppercase()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(dateStr, color = Color.Gray)
                        }
                        
                        val statusColor = when (order.status) {
                             "PENDING" -> Color(0xFFFF9800)
                             "PROCESSING" -> Color(0xFF2196F3)
                             "COMPLETED" -> Color(0xFF4CAF50)
                             else -> Color.Red
                        }
                        val statusText = when (order.status) {
                             "PENDING" -> "Chờ xác nhận"
                             "PROCESSING" -> "Đang chuẩn bị"
                             "COMPLETED" -> "Hoàn thành"
                             "CANCELLED" -> "Đã hủy"
                             "REJECTED" -> "Từ chối"
                             else -> order.status
                        }
                        
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = statusColor.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(statusText, color = statusColor, fontSize = 12.sp)
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
                                Text(order.userName.ifEmpty { "Khách lẻ" }, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(order.address.getFullAddress(), color = Color.Gray, fontSize = 12.sp)
                                if (order.userPhone.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { /* Handle call action */ },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Gọi điện: ${order.userPhone}", color = Color(0xFF2196F3))
                                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF2196F3))
                                    }
                                }
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
                    Text("Danh sách món (${order.items.size})", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        order.items.forEach { cartItem ->
                            OrderItemRow(
                                name = cartItem.foodName, 
                                price = "${cartItem.price}đ", 
                                qty = "x${cartItem.quantity}", 
                                note = cartItem.note
                            )
                        }
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
                            // Assuming total is final. If we had subtotal logic we could show it.
                            // For now just Total.
                            PaymentRow("Tổng tiền hàng", "${order.total}đ")
                            // PaymentRow("Phí giao hàng", "0đ") // Mock or from order if existed
                            // PaymentRow("Khuyến mãi", "-0đ", isDiscount = true)
                            Divider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(vertical = 12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng cộng", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${order.total}đ", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar(onReject: () -> Unit, onApprove: () -> Unit) {
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
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3038), contentColor = Color(0xFFFF5252)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Từ chối")
            }
            
            Button(
                onClick = onApprove,
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
         colors = CardDefaults.cardColors(containerColor = com.muatrenthenang.resfood.ui.theme.SurfaceCard),
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
                 if (note != null && note.isNotEmpty()) {
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
