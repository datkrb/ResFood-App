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
import androidx.compose.ui.res.stringResource
import com.muatrenthenang.resfood.R
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
    onNavigateToChat: (String) -> Unit,
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
    var rejectionReason by remember { mutableStateOf("") }
    var rejectionError by remember { mutableStateOf<String?>(null) }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRejectDialog = false 
                rejectionReason = ""
                rejectionError = null
            },
            title = { Text("Từ chối đặt bàn", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Vui lòng nhập lý do từ chối (tối thiểu 10 ký tự):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { 
                            rejectionReason = it
                            if (it.length >= 10) rejectionError = null
                        },
                        label = { Text("Lý do") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = rejectionError != null
                    )
                    if (rejectionError != null) {
                        Text(
                            text = rejectionError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.length < 10) {
                            rejectionError = "Lý do phải có ít nhất 10 ký tự"
                        } else {
                            if (reservation != null) {
                                viewModel.rejectReservation(reservation.id, rejectionReason) {
                                    Toast.makeText(context, context.getString(R.string.table_msg_rejected), Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            }
                            showRejectDialog = false
                            rejectionReason = ""
                            rejectionError = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightRed)
                ) {
                    Text(stringResource(R.string.table_reject_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRejectDialog = false
                    rejectionReason = ""
                    rejectionError = null
                }) {
                    Text(stringResource(R.string.table_reject_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                title = { Text(stringResource(R.string.table_detail_title), fontWeight = FontWeight.Bold) },
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
                            Toast.makeText(context, context.getString(R.string.table_msg_approve_success), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onComplete = {
                        viewModel.completeReservation(reservation.id) {
                            Toast.makeText(context, context.getString(R.string.table_msg_complete_success), Toast.LENGTH_SHORT).show()
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
                    Text(stringResource(R.string.table_not_found), color = MaterialTheme.colorScheme.onBackground)
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
                // Header Status is redundant if we use StatusTimelineCard, but let's keep it simple or replace it.
                // Replaced custom header with StatusTimelineCard for better detail including rejection reason
                item {
                    StatusTimelineCard(reservation = reservation)
                }

                // Customer Information Card
                item {
                    CustomerInfoCard(customer = customer, reservation = reservation, onNavigateToChat = onNavigateToChat)
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
fun CustomerInfoCard(
    customer: com.muatrenthenang.resfood.data.model.User?, 
    reservation: com.muatrenthenang.resfood.data.model.TableReservation,
    onNavigateToChat: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.table_customer_info_title), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
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
                    Text(customer?.fullName ?: stringResource(R.string.table_customer_guest), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
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
                
                // Chat Button
                if (customer != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { onNavigateToChat(customer.id) },
                        modifier = Modifier.background(PrimaryColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = PrimaryColor)
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
                        Text(stringResource(R.string.branch_info_address), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
            Text(stringResource(R.string.table_reservation_detail_title), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow(icon = Icons.Default.Store, label = stringResource(R.string.table_label_branch), value = reservation.branchName.ifEmpty { stringResource(R.string.admin_branch_unknown) })
            InfoRow(icon = Icons.Default.CalendarToday, label = stringResource(R.string.table_label_time).substringBefore(" "), value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reservation.timeSlot.toDate()))
            InfoRow(icon = Icons.Default.AccessTime, label = stringResource(R.string.booking_hour), value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(reservation.timeSlot.toDate()))
            InfoRow(icon = Icons.Default.People, label = stringResource(R.string.table_label_guests), value = stringResource(R.string.table_guests_format, reservation.guestCountAdult, reservation.guestCountChild))
            
            if (reservation.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.Note, label = stringResource(R.string.booking_note), value = reservation.note)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.table_code_label), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#${reservation.id.takeLast(8).uppercase()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.table_time_label), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(stringResource(R.string.admin_analytics_order_status), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            val (statusColor, statusText) = when(reservation.status) {
                "PENDING" -> Color(0xFFF59E0B) to stringResource(R.string.table_status_pending_long)
                "CONFIRMED" -> Color(0xFF3B82F6) to stringResource(R.string.table_status_confirmed_long)
                "COMPLETED" -> SuccessGreen to stringResource(R.string.table_status_completed)
                "CANCELLED" -> Color.Gray to stringResource(R.string.table_status_cancelled)
                "REJECTED" -> LightRed to stringResource(R.string.table_status_rejected)
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

            if (reservation.status == "REJECTED" && !reservation.rejectionReason.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lý do từ chối:",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightRed
                )
                Text(
                    text = reservation.rejectionReason!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                    Text(stringResource(R.string.admin_order_reject_btn))
                }
                
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.admin_order_approve_btn))
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
                    Text(stringResource(R.string.table_status_completed))
                }
            }
        }
    }
}
