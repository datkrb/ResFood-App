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
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
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
                Text(stringResource(R.string.res_not_found))
            }
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val statusColor = when (reservation.status) {
        "PENDING" -> Color(0xFFF59E0B)
        "CONFIRMED" -> Color(0xFF3B82F6)
        "COMPLETED" -> SuccessGreen
        "CANCELLED" -> Color.Gray
        "REJECTED" -> LightRed
        else -> Color.Gray
    }

    val statusText = when (reservation.status) {
        "PENDING" -> stringResource(R.string.me_status_pending)
        "CONFIRMED" -> stringResource(R.string.me_status_confirmed)
        "COMPLETED" -> stringResource(R.string.me_status_completed)
        "CANCELLED" -> stringResource(R.string.order_status_cancelled)
        "REJECTED" -> stringResource(R.string.table_status_rejected)
        else -> reservation.status
    }
    
    // Cancellation Dialog
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.res_cancel_title)) },
            text = { Text(stringResource(R.string.res_cancel_msg)) },
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
                    Text(stringResource(R.string.res_cancel_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.common_back))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(8.dp)
                )
                Text(
                    text = stringResource(R.string.res_title_detail),
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
                    Text(stringResource(R.string.res_cancel_btn), fontWeight = FontWeight.Bold)
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
                            text = stringResource(R.string.common_status),
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
            
            if (reservation.status == "REJECTED" && !reservation.rejectionReason.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = LightRed.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightRed.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Text(
                            text = "Lý do từ chối",
                            style = MaterialTheme.typography.labelMedium,
                            color = LightRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reservation.rejectionReason!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Section
            Text(
                text = stringResource(R.string.res_info_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(Icons.Default.Store, stringResource(R.string.res_branch), reservation.branchName)
            InfoRow(Icons.Default.CalendarToday, stringResource(R.string.res_date), dateFormatter.format(reservation.timeSlot.toDate()))
            InfoRow(Icons.Default.AccessTime, stringResource(R.string.res_time), timeFormatter.format(reservation.timeSlot.toDate()))
            InfoRow(Icons.Default.People, stringResource(R.string.res_guests), stringResource(R.string.res_guests_label, reservation.guestCountAdult, reservation.guestCountChild))
            if (reservation.note.isNotEmpty()) {
                InfoRow(Icons.Default.Note, stringResource(R.string.res_note), reservation.note)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.res_order_id, reservation.id),
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
