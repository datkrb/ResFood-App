package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.viewmodel.VoucherViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VoucherScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoucherViewModel = viewModel()
) {
    val promotions by viewModel.promotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VoucherTopBar(onBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryColor
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Đã có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadPromotions() }) {
                            Text("Thử lại")
                        }
                    }
                }
                promotions.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bạn chưa có mã giảm giá nào",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(promotions) { promotion ->
                            VoucherItemCard(
                                promotion = promotion,
                                userId = viewModel.currentUserId ?: "",
                                onClick = { selectedPromotion = promotion }
                            )
                        }
                    }
                }
            }
        }

        if (selectedPromotion != null) {
            VoucherDetailDialog(
                promotion = selectedPromotion!!,
                onDismiss = { selectedPromotion = null }
            )
        }
    }
}

@Composable
fun VoucherDetailDialog(promotion: Promotion, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Chi tiết ưu đãi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = promotion.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                
                HorizontalDivider()
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Mã:", color = Color.Gray)
                    Text(promotion.code, fontWeight = FontWeight.Bold, color = PrimaryColor)
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Giảm giá:", color = Color.Gray)
                    val discountText = if(promotion.discountType == 0) "${promotion.discountValue}%" else "${promotion.discountValue/1000}k"
                    Text(discountText, fontWeight = FontWeight.Bold)
                }

                if (promotion.maxDiscountValue > 0) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Giảm tối đa:", color = Color.Gray)
                        Text("${promotion.maxDiscountValue/1000}k", fontWeight = FontWeight.Medium)
                    }
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Áp dụng:", color = Color.Gray)
                    Text(when(promotion.applyFor) {
                        "SHIP" -> "Giao hàng"
                        "FOOD_ID" -> "Tại quán"
                        else -> "Toàn hệ thống"
                    }, fontWeight = FontWeight.Medium)
                }
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val startDateStr = dateFormat.format(promotion.startDate.toDate())
                val endDateStr = dateFormat.format(promotion.endDate.toDate())
                
                 Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Hiệu lực:", color = Color.Gray)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Từ: $startDateStr", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Đến: $endDateStr", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                if (promotion.minOrderValue > 0) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Đơn tối thiểu:", color = Color.Gray)
                         Text("${promotion.minOrderValue/1000}k", fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun VoucherTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Kho Voucher của tôi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}



@Composable
fun VoucherItemCard(promotion: Promotion, userId: String, onClick: () -> Unit) {
    val (icon, color, label) = when (promotion.applyFor) {
        "SHIP" -> Triple(Icons.Default.LocalShipping, Color(0xFFF97316), "Giao hàng") // Orange
        "FOOD_ID" -> Triple(Icons.Default.Restaurant, Color(0xFF10B981), "Tại quán") // Green
        else -> Triple(Icons.Default.ConfirmationNumber, PrimaryColor, "Toàn hệ thống") // ALL
    }
    
    // Tính số lượng còn lại
    val remainingQty = promotion.getRemainingQuantity(userId)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp, // Added shadow
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Content
                Column(verticalArrangement = Arrangement.Center) {
                    // Tag
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Text(
                        text = promotion.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateStr = dateFormat.format(promotion.endDate.toDate())
                        Text(
                            text = "HSD: $dateStr",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Hiển thị số lượng còn lại nếu có giới hạn
                        if (remainingQty > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = PrimaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "x$remainingQty",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}
