package com.muatrenthenang.resfood.ui.screens.admin.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.data.model.Promotion
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    // Note: Assuming viewModel has promotions flow. If not, will add placeholder or update VM.
    // For now, using mock list inside screen or check if VM has it. AdminViewModel doesn't expose promos yet.
    // I will add promos to AdminViewModel in next step.
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khuyến mãi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E2126)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Mock List for now until VM updated
            val promos = listOf(
                Promotion(name = "Giảm giá khai trương", code = "OPEN50", discountValue = 50, discountType = 0),
                Promotion(name = "Freeship đơn 200k", code = "FREESHIP", discountValue = 15000, discountType = 1),
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                 items(promos) { promo ->
                     PromotionItem(promo)
                 }
            }
        }
    }
}

@Composable
fun PromotionItem(promo: Promotion) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Discount, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(promo.name, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Code: ${promo.code}", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
            }
            Text(if(promo.discountType == 0) "-${promo.discountValue}%" else "-${promo.discountValue/1000}k", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}
