package com.muatrenthenang.resfood.ui.screens.me

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.ui.viewmodel.VoucherViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.muatrenthenang.resfood.util.CurrencyHelper

@Composable
fun VoucherScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoucherViewModel = viewModel()
) {
    val promotions by viewModel.promotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedPromotion by remember { mutableStateOf<Promotion?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val discountVouchers = promotions.filter { it.applyFor == "ALL" }
    val shippingVouchers = promotions.filter { it.applyFor == "SHIP" }

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
            Column(modifier = Modifier.fillMaxSize()) {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.voucher_tab_discount), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.voucher_tab_shipping), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = error ?: stringResource(R.string.common_error), color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.loadPromotions() }) { Text(stringResource(R.string.common_ok)) } // Using OK if no specific retry string but lets add common_retry later if needed or just use common_ok for now or Thử lại -> common_ok is not ideal. Let's use stringResource(R.string.common_error) for error msg.
                        }
                    }
                } else {
                    val currentList = if (selectedTab == 0) discountVouchers else shippingVouchers
                    
                    if (currentList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.voucher_empty),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentList) { promotion ->
                                VoucherItemCard(
                                    promotion = promotion,
                                    onClick = { selectedPromotion = promotion }
                                )
                            }
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
fun VoucherItemCard(promotion: Promotion, onClick: () -> Unit) {
    val isShip = promotion.applyFor == "SHIP"
    val cardColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    // Theme-aware colors
    val iconContainerColor = if (isShip) 
        Color(0xFFE3F2FD)
    else 
        Color(0xFFFFF7ED)
        
    val iconColor = if (isShip)
        Color(0xFF2196F3)
    else 
        Color(0xFFF97316)

    // Logic hiển thị badge số lượng
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isPrivate = !promotion.isPublic()
    val quantity = if (isPrivate) promotion.getRemainingQuantity(userId) else 0

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // Left Side (Icon/Image)
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isShip) Icons.Default.LocalShipping else Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isShip) stringResource(R.string.voucher_freeship) else stringResource(R.string.voucher_resfood),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
            }

            // Right Side (Details)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = promotion.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (promotion.minOrderValue > 0) {
                    Text(
                        text = stringResource(R.string.voucher_min_order, formatK(promotion.minOrderValue)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.voucher_expiry, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(promotion.endDate.toDate())),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.voucher_details),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

        // Badge số lượng
        var q = 1
        if (isPrivate && quantity > 0) {
            q = quantity
        }

        Surface(
            color = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = "x$q",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun VoucherDetailDialog(promotion: Promotion, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.voucher_dialog_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = promotion.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                
                HorizontalDivider()
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.voucher_dialog_code), color = Color.Gray)
                    Text(promotion.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.voucher_dialog_discount), color = Color.Gray)
                    val discountText = if(promotion.discountType == 0) "${promotion.discountValue}%" else "${promotion.discountValue/1000}k"
                    Text(discountText, fontWeight = FontWeight.Bold)
                }

                if (promotion.maxDiscountValue > 0) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.voucher_dialog_max_discount), color = Color.Gray)
                        Text("${promotion.maxDiscountValue/1000}k", fontWeight = FontWeight.Medium)
                    }
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.voucher_dialog_apply), color = Color.Gray)
                    Text(when(promotion.applyFor) {
                        "SHIP" -> stringResource(R.string.voucher_apply_ship)
                        else -> stringResource(R.string.voucher_apply_all)
                    }, fontWeight = FontWeight.Medium)
                }
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val startDateStr = dateFormat.format(promotion.startDate.toDate())
                val endDateStr = dateFormat.format(promotion.endDate.toDate())
                
                 Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.voucher_dialog_validity), color = Color.Gray)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.voucher_valid_from, startDateStr), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.voucher_valid_to, endDateStr), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                if (promotion.minOrderValue > 0) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.voucher_dialog_min_order), color = Color.Gray)
                         Text("${promotion.minOrderValue/1000}k", fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_ok))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun VoucherTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
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
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.voucher_topbar_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatK(value: Int): String {
    return CurrencyHelper.format(value)
}
