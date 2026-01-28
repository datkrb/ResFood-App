package com.muatrenthenang.resfood.ui.screens.me.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import com.muatrenthenang.resfood.ui.viewmodel.ReservationManagementViewModel
import com.muatrenthenang.resfood.ui.viewmodel.ReservationUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReservationDetailScreen(
    reservationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReservationManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Find the reservation in the current state
    val reservation = when(val state = uiState) {
        is ReservationUiState.Success -> state.reservations.find { it.id == reservationId }
        else -> null
    }

    if (reservation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState is ReservationUiState.Loading) {
                CircularProgressIndicator(color = PrimaryColor)
            } else {
                Text("Không tìm thấy đơn đặt bàn")
            }
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
    val timeFormatter = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
    
    val statusColor = when (reservation.status) {
        "PENDING" -> Color(0xFFF59E0B)
        "CONFIRMED" -> Color(0xFF3B82F6)
        "COMPLETED" -> SuccessGreen
        "CANCELLED" -> Color.Gray
        else -> Color.Gray
    }

    val statusText = when (reservation.status) {
        "PENDING" -> "Chờ xác nhận"
        "CONFIRMED" -> "Đã xác nhận"
        "COMPLETED" -> "Hoàn thành"
        "CANCELLED" -> "Đã hủy"
        else -> reservation.status
    }
    
    // Cancellation Dialog
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Hủy đặt bàn?") },
            text = { Text("Bạn có chắc chắn muốn hủy đơn đặt bàn này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelReservation(reservationId) {
                            showCancelDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LightRed)
                ) {
                    Text("Hủy đơn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Quay lại")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(8.dp)
                )
                Text(
                    text = "Chi tiết đặt bàn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        bottomBar = {
            if (reservation.status == "PENDING" || reservation.status == "CONFIRMED") {
                Button(
                    onClick = { showCancelDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hủy đặt bàn", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status Card
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Trạng thái",
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Section
            Text(
                text = "Thông tin đặt bàn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(Icons.Default.Store, "Chi nhánh", reservation.branchName)
            InfoRow(Icons.Default.CalendarToday, "Ngày", dateFormatter.format(reservation.timeSlot.toDate()))
            InfoRow(Icons.Default.AccessTime, "Giờ", timeFormatter.format(reservation.timeSlot.toDate()))
            InfoRow(Icons.Default.People, "Số khách", "${reservation.guestCountAdult} Người lớn, ${reservation.guestCountChild} Trẻ em")
            if (reservation.note.isNotEmpty()) {
                InfoRow(Icons.Default.Note, "Ghi chú", reservation.note)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Mã đơn: #${reservation.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
