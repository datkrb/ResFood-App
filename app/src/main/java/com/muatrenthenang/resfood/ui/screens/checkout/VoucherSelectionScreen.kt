package com.muatrenthenang.resfood.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Promotion
import java.text.SimpleDateFormat
import java.util.Locale
import com.muatrenthenang.resfood.util.CurrencyHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSelectionScreen(
    currentProductVoucher: Promotion?,
    currentShippingVoucher: Promotion?,
    promotions: List<Promotion>,
    onApplyVouchers: (Promotion?, Promotion?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Product, 1: Shipping
    var inputCode by remember { mutableStateOf("") }
    
    // Local state for selections (confirmed only on "Apply")
    var tempProductVoucher by remember { mutableStateOf(currentProductVoucher) }
    var tempShippingVoucher by remember { mutableStateOf(currentShippingVoucher) }

    // Filter promotions
    val productPromotions = promotions.filter { it.applyFor == "ALL" }
    val shippingPromotions = promotions.filter { it.applyFor == "SHIP" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.voucher_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { onApplyVouchers(tempProductVoucher, tempShippingVoucher) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.voucher_apply), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Input Code Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    placeholder = { Text(stringResource(R.string.voucher_input_hint)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { /* Handle manual code apply */ },
                    enabled = inputCode.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(stringResource(R.string.voucher_apply))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            // List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val currentList = if (selectedTab == 0) productPromotions else shippingPromotions
                
                if (currentList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.voucher_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(currentList) { promo ->
                        val isSelected = if (selectedTab == 0) {
                            tempProductVoucher?.id == promo.id
                        } else {
                            tempShippingVoucher?.id == promo.id
                        }

                        VoucherSelectionCard(
                            promotion = promo,
                            isSelected = isSelected,
                            onSelect = {
                                if (selectedTab == 0) {
                                    // Toggle selection
                                    tempProductVoucher = if (isSelected) null else promo
                                } else {
                                    tempShippingVoucher = if (isSelected) null else promo
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoucherSelectionCard(
    promotion: Promotion,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isShip = promotion.applyFor == "SHIP"
    val cardColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    // Theme-aware colors
    val iconContainerColor = if (isShip) 
        Color(0xFFE3F2FD) // Light Blue
    else 
        Color(0xFFFFF7ED) // Warm Orange Background
        
    val iconColor = if (isShip)
        Color(0xFF2196F3) // Blue Icon
    else 
        Color(0xFFF97316) // Vibrant Orange Icon

    // Logic hiển thị badge số lượng
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isPrivate = !promotion.isPublic()
    var quantity = if (isPrivate) promotion.getRemainingQuantity(userId) else 0

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
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

            // Middle (Details)
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
                
                Text(
                    text = stringResource(R.string.voucher_expiry, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(promotion.endDate.toDate())),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

                // Right Side (Radio Button Centered)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
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

private fun formatK(value: Int): String {
    return CurrencyHelper.format(value)
}
