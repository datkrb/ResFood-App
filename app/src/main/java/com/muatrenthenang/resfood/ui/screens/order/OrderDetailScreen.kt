package com.muatrenthenang.resfood.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.model.OrderItem
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.OrderListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserOrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderListViewModel = viewModel()
) {
    // Ensure mock data is loaded (simulating loading from shared ViewModel/Repository)
    LaunchedEffect(Unit) {
        if (viewModel.orders.value.isEmpty()) {
            viewModel.loadOrders("all")
        }
    }

    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId }

    if (order == null) {
        // Loading or not found state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OrderDetailTopBar(onBack = onNavigateBack)
        },
        bottomBar = {
            OrderDetailBottomBar(order = order, viewModel = viewModel)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            OrderStatusCard(order = order)

            // Delivery Info
            DeliveryInfoCard(order = order)

            // Order Items
            OrderItemsList(order = order)

            // Payment Details
            PaymentDetailsCard(order = order)

            // Payment Method
            PaymentMethodCard(order = order)
            
            if (order.status == "PENDING") {
                 OutlinedButton(
                    onClick = { viewModel.cancelOrder(order.id) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Hủy đơn hàng", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Spacing for bottom bar
        }
    }
}

@Composable
fun OrderDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Chi tiết đơn hàng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        // Placeholder to balance the back button
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun OrderStatusCard(order: Order) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val (statusText, statusColor) = getStatusDisplay(order.status)
                    Text(
                        text = statusText.uppercase(),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "#${order.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(order.createdAt.toDate()),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = PrimaryColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(
                            imageVector = when(order.status) {
                                "DELIVERING" -> Icons.Default.LocalShipping
                                "COMPLETED" -> Icons.Default.CheckCircle
                                "CANCELLED" -> Icons.Default.Cancel
                                else -> Icons.Default.Restaurant
                            },
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Progress Indicator (Mock)
            if (order.status == "DELIVERING" || order.status == "PROCESSING" || order.status == "PENDING") {
                val steps = listOf("Chờ xác nhận", "Đang chế biến", "Đang giao", "Hoàn tất")
                val currentStep = when(order.status) {
                    "PENDING" -> 0
                    "PROCESSING" -> 1
                    "DELIVERING" -> 2
                    else -> 3
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEachIndexed { index, label ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (index <= currentStep) PrimaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (index <= currentStep) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == currentStep) PrimaryColor else if (index < currentStep) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryInfoCard(order: Order) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp) // Extended height for more content
                    .background(PrimaryColor.copy(alpha = 0.5f))
            )
             Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(2.dp, PrimaryColor, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
             Text(
                "Nhà hàng",
                fontSize = 12.sp, 
                color = Color.Gray,
                fontWeight = FontWeight.Bold
             )
             Text(
                 "ResFood Restaurant",
                 fontWeight = FontWeight.Bold,
                 fontSize = 14.sp
             )
             Text(
                 "12 Đường Chùa Bộc, Đống Đa, Hà Nội",
                 fontSize = 13.sp,
                 color = MaterialTheme.colorScheme.onSurfaceVariant
             )
             
             Spacer(modifier = Modifier.height(16.dp))
             
             Text(
                "Địa chỉ nhận hàng",
                fontSize = 12.sp, 
                color = Color.Gray,
                fontWeight = FontWeight.Bold
             )
             // User Info row
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Text(
                     if (order.userName.isNotEmpty()) order.userName else "Nguyễn Văn A", // Mock if empty
                     fontWeight = FontWeight.Bold,
                     fontSize = 14.sp
                 )
                 Text(
                     " • " + (if (order.userPhone.isNotEmpty()) order.userPhone else "0987654321"), // Mock if empty
                     fontSize = 14.sp,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
             }
             Text(
                 if (order.address.isNotEmpty()) order.address else "123 Đường Nguyễn Huệ, Quận 1, TP.HCM",
                 fontSize = 13.sp,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 lineHeight = 18.sp
             )
        }
    }
}



@Composable
fun OrderItemsList(order: Order) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            order.items.forEachIndexed { index, item ->
                OrderItemRow(item)
                if (index < order.items.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         AsyncImage(
            model = item.foodImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.foodName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.note.isNullOrEmpty()) {
                Text(
                    item.note,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
             Text(
                text = "x${item.quantity}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                String.format("%,dđ", item.price * item.quantity).replace(',', '.'),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            // Mock original price slightly higher
             Text(
                String.format("%,dđ", (item.price * item.quantity * 1.2).toInt()).replace(',', '.'),
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PaymentDetailsCard(order: Order) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Transparent-ish background
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chi tiết thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            PaymentRow("Tổng tiền món (${order.items.size} món)", order.subtotal)
            PaymentRow("Phí giao hàng", order.deliveryFee)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Giảm giá", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = PrimaryColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("VOUCHER", fontSize = 10.sp, color = PrimaryColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                 }
                 Text("-${String.format("%,dđ", order.discount).replace(',', '.')}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryColor)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thành tiền", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        String.format("%,dđ", order.total).replace(',', '.'),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Text("Đã bao gồm VAT", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(String.format("%,dđ", value).replace(',', '.'), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentMethodCard(order: Order) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mock Payment Icon (ZaloPay usually blue)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0068FF),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Zalo\nPay", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Phương thức thanh toán", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ví ZaloPay", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OrderDetailBottomBar(order: Order, viewModel: OrderListViewModel) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
         shadowElevation = 16.dp // Top shadow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Action Buttons
            if (order.status == "COMPLETED") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.reviewOrder(order.id) },
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Text("Đánh giá")
                    }
                    Button(
                        onClick = { viewModel.reOrder(order.id) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Mua lại lần nữa", fontWeight = FontWeight.Bold)
                    }
                }
                 Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Support Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* Chat logic */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat")
                }
                
                Button(
                    onClick = { viewModel.callRestaurant(order.id) },
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gọi nhà hàng", color = Color.White)
                }
            }
        }
    }
}
