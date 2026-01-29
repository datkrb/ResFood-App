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
import com.muatrenthenang.resfood.util.CurrencyHelper
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R

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
    var rejectionReason by remember { mutableStateOf("") }
    var rejectionError by remember { mutableStateOf<String?>(null) }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRejectDialog = false
                rejectionReason = ""
                rejectionError = null
            },
            title = { Text(stringResource(R.string.admin_order_reject_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vui lòng nhập lý do từ chối đơn hàng này:")
                    
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { 
                            rejectionReason = it
                            rejectionError = null
                        },
                        label = { Text("Lý do từ chối") },
                        placeholder = { Text("Ví dụ: Sản phẩm tạm hết hàng, không thể giao đến địa chỉ này...") },
                        isError = rejectionError != null,
                        supportingText = {
                            if (rejectionError != null) {
                                Text(rejectionError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Tối thiểu 10 ký tự", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            rejectionReason.isBlank() -> {
                                rejectionError = "Vui lòng nhập lý do từ chối"
                            }
                            rejectionReason.length < 10 -> {
                                rejectionError = "Lý do phải có ít nhất 10 ký tự"
                            }
                            else -> {
                                if (order != null) {
                                    viewModel.rejectOrder(order.id, rejectionReason) {
                                        Toast.makeText(context, context.getString(R.string.admin_order_msg_rejected), Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                }
                                showRejectDialog = false
                                rejectionReason = ""
                                rejectionError = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed)
                ) {
                    Text(stringResource(R.string.admin_order_reject_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRejectDialog = false
                    rejectionReason = ""
                    rejectionError = null
                }) {
                    Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                title = { Text(stringResource(R.string.admin_order_detail_title), fontWeight = FontWeight.Bold) },
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
                            Toast.makeText(context, context.getString(R.string.admin_order_msg_approved_long), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onStartDelivery = {
                         viewModel.startDelivery(order.id) {
                             Toast.makeText(context, context.getString(R.string.admin_order_msg_delivering), Toast.LENGTH_SHORT).show()
                         }  
                    },
                    onComplete = {
                        viewModel.completeOrder(order.id)
                        Toast.makeText(context, context.getString(R.string.admin_order_msg_completed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) { padding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if(orders.isEmpty()) CircularProgressIndicator(color = PrimaryColor)
                else Text(stringResource(R.string.admin_order_not_found), color = MaterialTheme.colorScheme.onBackground)
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
                            Text(stringResource(R.string.admin_order_label_order_id), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("#${order.id.takeLast(6).uppercase()}", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                        
                        StatusBadge(order.status)
                    }
                }

                // Rejection Reason Card (if rejected)
                if (order.status == "REJECTED" && !order.rejectionReason.isNullOrBlank()) {
                    item {
                        AdminRejectionReasonCard(order = order)
                    }
                }

                // Customer Info Card
                item {
                    SectionHeader(stringResource(R.string.admin_order_customer_info))
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
                                    Text(order.userName.ifEmpty { stringResource(R.string.admin_order_guest) }, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                    Text(stringResource(R.string.admin_order_delivery_address), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    Text(order.address.getFullAddress(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    if (order.distanceText != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Khoảng cách: ${order.distanceText}",
                                            color = PrimaryColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
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
                                    Text(stringResource(R.string.admin_order_location), color = MaterialTheme.colorScheme.onSecondaryContainer)
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
                                    Text(stringResource(R.string.admin_order_directions), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }

                // Item List
                item {
                    SectionHeader("${stringResource(R.string.admin_order_item_list)} (${order.items.size})")
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
                    SectionHeader(stringResource(R.string.admin_order_payment))
                    Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         shape = RoundedCornerShape(12.dp),
                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Subtotal
                            PaymentRow(stringResource(R.string.admin_order_payment_subtotal), order.subtotal)
                            
                            // Discounts
                            if (order.productDiscount > 0) {
                                PaymentRow(stringResource(R.string.admin_order_payment_voucher), -order.productDiscount, isDiscount = true)
                            }
                            
                            // Shipping
                            PaymentRow(stringResource(R.string.admin_order_payment_shipping) + if (order.distanceText != null) " (${order.distanceText})" else "", order.deliveryFee)
                            if (order.shippingDiscount > 0) {
                                PaymentRow(stringResource(R.string.admin_order_payment_ship_voucher), -order.shippingDiscount, isDiscount = true)
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.admin_order_payment_total), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    formatCurrency(order.total), 
                                    color = PrimaryColor, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 18.sp
                                )
                            }
                            
                             Text(
                                if(order.paymentMethod == "COD") stringResource(R.string.admin_order_payment_cod) else stringResource(R.string.admin_order_payment_online), 
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
                    Text(stringResource(R.string.admin_order_reject_btn))
                }
                
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.admin_order_action_approve))
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
                    Text(stringResource(R.string.admin_order_action_ship))
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
                    Text(stringResource(R.string.admin_order_action_complete))
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
                  Text("${stringResource(R.string.admin_order_note_prefix)} ${item.note}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
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
         "PENDING" -> PrimaryColor to stringResource(R.string.admin_order_status_pending)
         "PROCESSING" -> Color(0xFFFF9800) to stringResource(R.string.admin_order_status_processing)
         "DELIVERING" -> Color(0xFF2196F3) to stringResource(R.string.admin_order_status_delivering)
         "COMPLETED" -> SuccessGreen to stringResource(R.string.admin_order_status_completed)
         "CANCELLED" -> Color.Gray to stringResource(R.string.admin_order_status_cancelled)
         "REJECTED" -> LightRed to stringResource(R.string.admin_order_status_rejected)
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

@Composable
fun AdminRejectionReasonCard(order: Order) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightRed.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = LightRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Lý do từ chối",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightRed
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = order.rejectionReason ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            
            if (order.rejectedAt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale("vi", "VN"))
                Text(
                    text = "Thời gian từ chối: ${dateFormat.format(order.rejectedAt.toDate())}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

fun formatCurrency(amount: Int): String {
    return CurrencyHelper.format(amount)
}
