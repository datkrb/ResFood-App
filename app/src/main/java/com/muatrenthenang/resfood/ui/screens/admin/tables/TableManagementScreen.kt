package com.muatrenthenang.resfood.ui.screens.admin.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel

data class MockTable(
    val id: String,
    val name: String,
    val status: TableStatus,
    val seats: Int
)

enum class TableStatus {
    EMPTY, OCCUPIED, RESERVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    // Mock Data - In real app, move to ViewModel
    val tables = remember {
        listOf(
            MockTable("1", "Bàn 01", TableStatus.OCCUPIED, 4),
            MockTable("2", "Bàn 02", TableStatus.EMPTY, 2),
            MockTable("3", "Bàn 03", TableStatus.RESERVED, 6),
            MockTable("4", "Bàn 04", TableStatus.EMPTY, 4),
            MockTable("5", "Bàn 05", TableStatus.OCCUPIED, 8),
            MockTable("6", "Bàn 06", TableStatus.EMPTY, 2),
            MockTable("7", "Bàn 07", TableStatus.EMPTY, 4),
            MockTable("8", "Bàn 08", TableStatus.RESERVED, 4),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý bàn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2126),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E2126)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Legend
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatusLegend(Color(0xFF4CAF50), "Trống")
                StatusLegend(Color(0xFFF44336), "Đang dùng")
                StatusLegend(Color(0xFFFF9800), "Đặt trước")
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tables) { table ->
                    TableItem(table)
                }
            }
        }
    }
}

@Composable
fun StatusLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun TableItem(table: MockTable) {
    val bgColor = when (table.status) {
        TableStatus.EMPTY -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        TableStatus.OCCUPIED -> Color(0xFFF44336).copy(alpha = 0.2f)
        TableStatus.RESERVED -> Color(0xFFFF9800).copy(alpha = 0.2f)
    }
    val contentColor = when (table.status) {
        TableStatus.EMPTY -> Color(0xFF4CAF50)
        TableStatus.OCCUPIED -> Color(0xFFF44336)
        TableStatus.RESERVED -> Color(0xFFFF9800)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3038)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.aspectRatio(1f).clickable { /* Open detail/action */ }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = contentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(table.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${table.seats} ghế", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.background(bgColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    when(table.status){
                        TableStatus.EMPTY -> "Trống"
                        TableStatus.OCCUPIED -> "Có khách"
                        TableStatus.RESERVED -> "Đã đặt"
                    },
                    color = contentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
