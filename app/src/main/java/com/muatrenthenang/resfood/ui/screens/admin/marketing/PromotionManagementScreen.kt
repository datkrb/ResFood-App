package com.muatrenthenang.resfood.ui.screens.admin.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Promotion) -> Unit
) {
    val promos by viewModel.promotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    
    // Deletion State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var promotionToDelete by remember { mutableStateOf<Promotion?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    if (showDeleteDialog && promotionToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                promotionToDelete = null
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa khuyến mãi '${promotionToDelete?.name}'? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        promotionToDelete?.let { viewModel.deletePromotion(it.id) }
                        showDeleteDialog = false
                        promotionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false 
                    promotionToDelete = null
                }) {
                    Text("Hủy")
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
                title = { Text("Quản lý khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refreshData() },
            state = pullRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (promos.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalActivity, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chưa có chương trình khuyến mãi nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                     items(promos) { promo ->
                         PromotionItem(
                             promo = promo,
                             onEdit = { onNavigateToEdit(promo) },
                             onDelete = { 
                                 promotionToDelete = promo
                                 showDeleteDialog = true 
                             }
                         )
                     }
                }
            }
        }
    }
}

@Composable
fun PromotionItem(
    promo: Promotion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isExpired = try { promo.endDate.toDate().time < now } catch (e: Exception) { false }
    val isActive = promo.isActive && !isExpired
    val isShip = promo.applyFor == "SHIP"

    // Quantity Logic
    val quantityText = remember(promo) {
        if (!promo.isPublic()) {
            val remaining = promo.userQuantities.values.sum()
            "$remaining/${promo.totalQuantity} mã"
        } else if (promo.totalQuantity > 0) {
            val used = promo.usedByUserIds.size
            val remaining = (promo.totalQuantity - used).coerceAtLeast(0)
            "$remaining/${promo.totalQuantity} mã"
        } else {
            "Không giới hạn"
        }
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon + Basic Info
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if(isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                             imageVector = if(isShip) Icons.Default.LocalShipping else Icons.Default.ConfirmationNumber,
                             contentDescription = null,
                             tint = if(isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                             modifier = Modifier.size(24.dp)
                         )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = promo.name, 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Status & Code
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = promo.code,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            val statusText = when {
                                !promo.isActive -> "Đã tắt"
                                isExpired -> "Hết hạn"
                                else -> "Đang chạy"
                            }
                            
                            val statusColor = when {
                                !promo.isActive -> MaterialTheme.colorScheme.outline
                                isExpired -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                            
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor
                            )
                        }
                    }
                }
                
                // Actions (Edit/Delete)
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Column {
                     Text("Giảm giá", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Text(
                         text = if(promo.discountType == 0) "${promo.discountValue}%" else "${promo.discountValue}đ",
                         style = MaterialTheme.typography.bodyMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = MaterialTheme.colorScheme.onSurface
                     )
                 }

                 Column {
                     Text("Số lượng", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Text(
                         text = quantityText,
                         style = MaterialTheme.typography.bodyMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = MaterialTheme.colorScheme.onSurface
                     )
                 }
                 
                 Column {
                     Text("Thời hạn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                     val endStr = try { dateFormat.format(promo.endDate.toDate()) } catch (e:Exception) {"N/A"}
                     Text(
                         text = endStr,
                         style = MaterialTheme.typography.bodyMedium,
                         fontWeight = FontWeight.SemiBold,
                         color = MaterialTheme.colorScheme.onSurface
                     )
                 }
                 
                 IconButton(
                     onClick = onDelete,
                     modifier = Modifier.size(32.dp)
                 ) {
                     Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                 }
            }
        }
    }
}
