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
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
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
    onNavigateToReview: (String) -> Unit,
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
    var showCancelDialog by remember { mutableStateOf(false) }
    var showReviewSelection by remember { mutableStateOf(false) }

    if (order == null) {
        // Loading or not found state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Xác nhận hủy đơn") },
            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelOrder(order.id)
                        showCancelDialog = false
                    }
                ) {
                    Text("Đồng ý", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Hủy bỏ", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
    
    if (showReviewSelection) {
        ReviewSelectionDialog(
            order = order,
            onDismiss = { showReviewSelection = false },
            onItemSelect = { foodId ->
                showReviewSelection = false
                onNavigateToReview(foodId)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OrderDetailTopBar(onBack = onNavigateBack)
        },
        bottomBar = {
            OrderDetailBottomBar(
                order = order, 
                viewModel = viewModel,
                onReviewClick = { showReviewSelection = true }
            )
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
                    onClick = { showCancelDialog = true },
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
                    val (statusText, statusColor) = getStatusDisplay(order)
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Địa chỉ nhận hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (order.address.label.isNotEmpty()) order.address.label else "Nhà riêng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (order.address.getFullAddress().isNotEmpty()) order.address.getFullAddress() else "123 Đường Nguyễn Huệ, Quận 1, TP.HCM",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Contact Info Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (order.userName.isNotEmpty()) order.userName else "Nguyễn Văn A",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(24.dp))

                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (order.userPhone.isNotEmpty()) order.userPhone else "0987654321",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
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
            if (!item.selectedToppings.isNullOrEmpty()) {
                Text(
                    "Topping: ${item.selectedToppings.joinToString(", ") { it.name }}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
            
            if (order.productDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Giảm giá món", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                     }
                     Text("-${String.format("%,dđ", order.productDiscount).replace(',', '.')}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
                }
            }

            if (order.shippingDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Giảm phí ship", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0097A7), modifier = Modifier.size(14.dp))
                     }
                     Text("-${String.format("%,dđ", order.shippingDiscount).replace(',', '.')}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0097A7))
                }
            }
            
            // Legacy fall back if individual discounts are 0 but total discount > 0 (for old orders)
            if (order.productDiscount == 0 && order.shippingDiscount == 0 && order.discount > 0) {
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
                // Determine icon based on payment method
                when (order.paymentMethod) {
                    "ZALOPAY" -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0068FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Zalo\nPay", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    "MOMO" -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFA50064),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Mo\nMo", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    "SEPAY" -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(40.dp),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Using helper text until R.drawable is confirmed available and compiled
                                Text("SE\nPay", fontSize = 10.sp, color = Color(0xFF0068FF), fontWeight = FontWeight.Bold, lineHeight = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                    else -> { // COD or Default
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text("Phương thức thanh toán", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val methodName = when(order.paymentMethod) {
                        "ZALOPAY" -> "Ví ZaloPay"
                        "MOMO" -> "Ví MoMo"
                        "SEPAY" -> "Chuyển khoản SEPay"
                        else -> "Tiền mặt khi nhận hàng"
                    }
                    Text(methodName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OrderDetailBottomBar(order: Order, viewModel: OrderListViewModel, onReviewClick: () -> Unit) {
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
                        onClick = onReviewClick,
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

@Composable
fun ReviewSelectionDialog(
    order: Order,
    onDismiss: () -> Unit,
    onItemSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đánh giá sản phẩm") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Chọn sản phẩm bạn muốn đánh giá:")
                order.items.forEach { item ->
                    Surface(
                        onClick = { onItemSelect(item.foodId) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            AsyncImage(
                                model = item.foodImage,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(item.foodName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.rotate(180f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
