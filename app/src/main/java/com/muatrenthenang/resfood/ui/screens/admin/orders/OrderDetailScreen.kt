package com.muatrenthenang.resfood.ui.screens.admin.orders

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.data.model.Order
import com.muatrenthenang.resfood.data.model.OrderItem
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val order = orders.find { it.id == orderId }
    val context = LocalContext.current
    
    // State for Reject Dialog
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối đơn hàng?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn từ chối đơn hàng này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (order != null) {
                            viewModel.rejectOrder(order.id) {
                                Toast.makeText(context, "Đã từ chối đơn hàng", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed)
                ) {
                    Text("Từ chối xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy bỏ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (order != null) {
                BottomActionBar(
                    status = order.status,
                    onReject = { showRejectDialog = true },
                    onApprove = { 
                        viewModel.approveOrder(order.id) {
                            Toast.makeText(context, "Đã duyệt đơn hàng! Chuyển sang chế biến.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onStartDelivery = {
                         viewModel.startDelivery(order.id) {
                             Toast.makeText(context, "Bắt đầu giao hàng!", Toast.LENGTH_SHORT).show()
                         } 
                    },
                    onComplete = {
                        viewModel.completeOrder(order.id)
                        Toast.makeText(context, "Đơn hàng đã hoàn thành!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if(orders.isEmpty()) CircularProgressIndicator(color = PrimaryColor)
                else Text("Không tìm thấy đơn hàng", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            val customer = customers.find { it.id == order.userId }
            
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
                        SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("vi", "VN")).format(order.createdAt.toDate())
                    } else "N/A"
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Đơn hàng", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("#${order.id.takeLast(6).uppercase()}", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                        
                        StatusBadge(order.status)
                    }
                }

                // Customer Info Card
                item {
                    SectionHeader("Thông tin khách hàng")
                    Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         shape = RoundedCornerShape(12.dp),
                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                if (customer?.avatarUrl != null) {
                                     AsyncImage(
                                        model = customer.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(50.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                       modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray),
                                       contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = order.userName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.userName.ifEmpty { "Khách lẻ" }, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (order.userPhone.isNotEmpty()) {
                                        Text(order.userPhone, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                    }
                                }
                                
                                // Call Button
                                if (order.userPhone.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.userPhone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.background(SuccessGreen.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen)
                                    }
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            
                            // Address
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Địa chỉ giao hàng", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Text(order.address.getFullAddress(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            // Maps Buttons
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // View Location Button
                                Button(
                                    onClick = {
                                        val uriString = if (order.address.latitude != null && order.address.longitude != null) {
                                            "geo:${order.address.latitude},${order.address.longitude}?q=${order.address.latitude},${order.address.longitude}(${Uri.encode(order.userName)})"
                                        } else {
                                            "geo:0,0?q=${Uri.encode(order.address.getFullAddress())}"
                                        }
                                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        } else {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vị trí", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }

                                // Navigation Button
                                Button(
                                    onClick = {
                                        val uriString = if (order.address.latitude != null && order.address.longitude != null) {
                                            "google.navigation:q=${order.address.latitude},${order.address.longitude}&mode=d"
                                        } else {
                                            "google.navigation:q=${Uri.encode(order.address.getFullAddress())}&mode=d"
                                        }
                                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        } else {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Chỉ đường", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }

                // Item List
                item {
                    SectionHeader("Danh sách món (${order.items.size})")
                    Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         shape = RoundedCornerShape(12.dp),
                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            order.items.forEachIndexed { index, item ->
                                OrderItemRow(item)
                                if (index < order.items.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
                
                // Payment Info
                item {
                    SectionHeader("Thanh toán")
                    Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         shape = RoundedCornerShape(12.dp),
                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Subtotal
                            PaymentRow("Tổng tiền hàng", order.subtotal)
                            
                            // Discounts
                            if (order.productDiscount > 0) {
                                PaymentRow("Voucher giảm giá", -order.productDiscount, isDiscount = true)
                            }
                            
                            // Shipping
                            PaymentRow("Phí giao hàng", order.deliveryFee)
                            if (order.shippingDiscount > 0) {
                                PaymentRow("Voucher vận chuyển", -order.shippingDiscount, isDiscount = true)
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng thanh toán", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    formatCurrency(order.total), 
                                    color = PrimaryColor, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 18.sp
                                )
                            }
                            
                             Text(
                                if(order.paymentMethod == "COD") "Thanh toán khi nhận hàng (COD)" else "Đã thanh toán Online", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                item {
                     Spacer(modifier = Modifier.height(80.dp)) // Prevention for BottomBar overlap
                }
            }
        }
    }
}

@Composable
fun BottomActionBar(
    status: String, 
    onReject: () -> Unit, 
    onApprove: () -> Unit,
    onStartDelivery: () -> Unit,
    onComplete: () -> Unit
) {
    if (status == "CANCELLED" || status == "REJECTED" || status == "COMPLETED") return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (status == "PENDING") {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = LightRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightRed),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Từ chối")
                }
                
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Duyệt đơn")
                }
            } else if (status == "PROCESSING") {
                 Button(
                    onClick = onStartDelivery,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), // Orange for delivery
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Giao cho Shipper")
                }
            } else if (status == "DELIVERING") {
                 Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hoàn thành đơn hàng")
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
         if (item.foodImage != null) {
              AsyncImage(
                model = item.foodImage,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
         } else {
             Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
         }
         
         Spacer(modifier = Modifier.width(12.dp))
         
         Column(modifier = Modifier.weight(1f)) {
             Text(item.foodName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
             Spacer(modifier = Modifier.height(4.dp))
             Text(formatCurrency(item.price), color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
             if (item.note != null && item.note.isNotEmpty()) {
                 Spacer(modifier = Modifier.height(4.dp))
                 Text("Ghi chú: ${item.note}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
             }
             if (item.selectedToppings.isNotEmpty()) {
                 Spacer(modifier = Modifier.height(4.dp))
                 Text("Topping: ${item.selectedToppings.joinToString(", ") { it.name }}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
             }
         }
         
         Text("x${item.quantity}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun PaymentRow(label: String, value: Int, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            formatCurrency(value), 
            color = if(isDiscount) SuccessGreen else MaterialTheme.colorScheme.onSurface, 
            fontWeight = if(isDiscount) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title, 
        fontWeight = FontWeight.Bold, 
        color = MaterialTheme.colorScheme.onBackground, 
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
        fontSize = 16.sp
    )
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
         "PENDING" -> PrimaryColor to "Chờ xác nhận"
         "PROCESSING" -> Color(0xFFFF9800) to "Đang chế biến"
         "DELIVERING" -> Color(0xFF2196F3) to "Đang giao"
         "COMPLETED" -> SuccessGreen to "Hoàn thành"
         "CANCELLED" -> Color.Gray to "Đã hủy"
         "REJECTED" -> LightRed to "Đã từ chối"
         else -> Color.Gray to status
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
             Spacer(modifier = Modifier.width(6.dp))
             Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatCurrency(amount: Int): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(amount)
}
