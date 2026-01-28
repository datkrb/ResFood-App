package com.muatrenthenang.resfood.ui.screens.admin.tables

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.layout.ContentScale
import com.muatrenthenang.resfood.ui.viewmodel.admin.AdminViewModel
import com.muatrenthenang.resfood.ui.theme.LightRed
import com.muatrenthenang.resfood.ui.theme.PrimaryColor
import com.muatrenthenang.resfood.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableReservationDetailScreen(
    reservationId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val reservations by viewModel.reservations.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val reservation = reservations.find { it.id == reservationId }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (reservations.isEmpty()) {
            viewModel.loadReservations()
        }
        if (customers.isEmpty()) {
            viewModel.loadCustomers()
        }
    }
    
    // State for Reject Dialog
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối đơn đặt bàn?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn từ chối đơn đặt bàn này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (reservation != null) {
                            viewModel.rejectReservation(reservation.id) {
                                Toast.makeText(context, "Đã từ chối đơn đặt bàn", Toast.LENGTH_SHORT).show()
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
                title = { Text("Chi tiết đặt bàn", fontWeight = FontWeight.Bold) },
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
            if (reservation != null) {
                BottomActionBar(
                    status = reservation.status,
                    onReject = { showRejectDialog = true },
                    onApprove = { 
                        viewModel.approveReservation(reservation.id) {
                            Toast.makeText(context, "Đã duyệt đơn đặt bàn!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onComplete = {
                        viewModel.completeReservation(reservation.id) {
                            Toast.makeText(context, "Đơn đặt bàn đã hoàn thành!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (reservation == null) {
            if (reservations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy đơn đặt bàn", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        } else {
            val customer = customers.find { it.id == reservation.userId }
            
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Status
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Đơn đặt bàn", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("#${reservation.id.takeLast(6).uppercase()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        
                        val (statusColor, statusText) = when(reservation.status) {
                            "PENDING" -> Color(0xFFF59E0B) to "Chờ xác nhận"
                            "CONFIRMED" -> Color(0xFF3B82F6) to "Đã xác nhận"
                            "COMPLETED" -> SuccessGreen to "Hoàn thành"
                            "CANCELLED" -> Color.Gray to "Đã hủy"
                            "REJECTED" -> LightRed to "Đã từ chối"
                            else -> Color.Gray to reservation.status
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Customer Information Card
                item {
                    CustomerInfoCard(customer = customer, reservation = reservation)
                }
                
                // Reservation Details Card
                item {
                    ReservationDetailsCard(reservation = reservation)
                }

                item {
                     Spacer(modifier = Modifier.height(80.dp)) // Prevention for BottomBar overlap
                }
            }
        }
    }
}

@Composable
fun CustomerInfoCard(customer: com.muatrenthenang.resfood.data.model.User?, reservation: com.muatrenthenang.resfood.data.model.TableReservation) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Thông tin khách hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            text = (customer?.fullName?.firstOrNull() ?: "K").toString().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer?.fullName ?: "Khách", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (customer?.phone != null) {
                        Text(customer.phone, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
                        
                // Call Button
                if (customer?.phone != null) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.background(SuccessGreen.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen)
                    }
                } else if (reservation.note.contains("SDT:")) {
                   // Fallback: Try to extract phone from note if not available in User profile (Legacy support)
                   val phoneInNote = reservation.note.substringAfter("SDT:").trim().takeWhile { it.isDigit() }
                   if(phoneInNote.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneInNote"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.background(SuccessGreen.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen)
                        }
                   }
                }
            }

            if (customer?.phone != null) {
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                     Spacer(modifier = Modifier.width(8.dp))
                     Text(customer.phone, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                 }
            } else if (reservation.note.contains("SDT:")) {
                 val phoneInNote = reservation.note.substringAfter("SDT:").trim().takeWhile { it.isDigit() }
                 if(phoneInNote.isNotEmpty()) {
                     Spacer(modifier = Modifier.height(8.dp))
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(phoneInNote, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                     }
                 }
            }

            if (customer?.addresses?.isNotEmpty() == true) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Địa chỉ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        val address = customer.addresses.firstOrNull { it.isDefault } ?: customer.addresses.first()
                        Text(address.getFullAddress(), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationDetailsCard(reservation: com.muatrenthenang.resfood.data.model.TableReservation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chi tiết đặt bàn", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(icon = Icons.Default.Store, label = "Chi nhánh", value = reservation.branchName.ifEmpty { "Chưa xác định" })
            InfoRow(icon = Icons.Default.CalendarToday, label = "Ngày", value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reservation.timeSlot.toDate()))
            InfoRow(icon = Icons.Default.AccessTime, label = "Giờ", value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(reservation.timeSlot.toDate()))
            InfoRow(icon = Icons.Default.People, label = "Số khách", value = "${reservation.guestCountAdult} Người lớn, ${reservation.guestCountChild} Trẻ em")
            
            if (reservation.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.Note, label = "Ghi chú", value = reservation.note)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mã đặt bàn", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#${reservation.id.takeLast(8).uppercase()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Thời gian đặt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                val createdTime = if (reservation.createdAt != null) {
                    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(reservation.createdAt.toDate())
                } else "N/A"
                Text(createdTime, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatusTimelineCard(reservation: com.muatrenthenang.resfood.data.model.TableReservation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trạng thái", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            val (statusColor, statusText) = when(reservation.status) {
                "PENDING" -> Color(0xFFF59E0B) to "Chờ xác nhận"
                "CONFIRMED" -> Color(0xFF3B82F6) to "Đã xác nhận"
                "COMPLETED" -> SuccessGreen to "Hoàn thành"
                "CANCELLED" -> Color.Gray to "Đã hủy"
                "REJECTED" -> LightRed to "Đã từ chối"
                else -> Color.Gray to reservation.status
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                     Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                     Spacer(modifier = Modifier.width(8.dp))
                     Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            } else if (status == "CONFIRMED") {
                 Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hoàn thành")
                }
            }
        }
    }
}
